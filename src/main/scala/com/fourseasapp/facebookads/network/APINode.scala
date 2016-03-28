package com.fourseasapp.facebookads.network

import java.io.File
import java.util.Locale
import javax.inject.Inject

import com.fourseasapp.facebookads._
import enumeratum.EnumEntry
import play.api.libs.json.{Format, JsValue}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.runtime.universe._

/**
  * Created by hailegia on 3/12/2016.
  */
trait APINode[T <: APINode[T]] { self: T =>
  type Fields <: EnumEntry

  private var _context: APIContext = null
  private var _parentId: String = null

  private[network] var props: PropsContainer = null

  @Inject() private var _apiRequestFactory: APIRequestFactory = null

  def id: String

  def parentId: String = _parentId

  def parentId_=(value: String) = _parentId = value

  def apiContext = _context

  def apiContext_=(context: APIContext) = _context = context

  def apiRequestFactory_=(value: APIRequestFactory) = _apiRequestFactory = value

  def apiRequestFactory = _apiRequestFactory

  def set(fields: Map[Fields, Any])(implicit m: Mappable[T]): T = {
    if (props == null) {
      props = new PropsContainer(APINode.mapify(this))
    }
    fields.foreach(entry => {
      props.set(entry._1.toString, entry._2)
    })
    val newInstance = APINode.materialize[T](props.data)
    newInstance.props = props
    newInstance.apiRequestFactory = this.apiRequestFactory
    newInstance.apiContext = this.apiContext
    newInstance.parentId = this.parentId
    newInstance
  }

  def set(fields: (Fields, Any)*)(implicit m: Mappable[T]): T = {
    if (props == null) {
      props = new PropsContainer(APINode.mapify(this))
    }
    fields.foreach(entry => {
      props.set(entry._1.toString, entry._2)
    })
    val newInstance = APINode.materialize[T](props.data)
    newInstance.props = props
    newInstance.apiRequestFactory = this.apiRequestFactory
    newInstance.apiContext = this.apiContext
    newInstance.parentId = this.parentId
    newInstance
  }

  def create(batchAPIRequest: BatchAPIRequest = null, params: Map[String, Any] = Map(), files: Map[String, File] = Map(), validating: Boolean = false)
            (implicit format: Format[T], typeTag: TypeTag[T], m: Mappable[T], ec: ExecutionContext): Future[Option[T]] = {
    if (id != null) {
      return Future.failed(new CRUDException(s"$this has been created before."))
    }
    if (parentId == null) {
      return Future.failed(new CRUDException(s"$this cannot be created as its parent is undefined."))
    }

    var sentParams: Map[String, Any] = Map()
    if (params != null) {
      sentParams ++= params
    }
    sentParams ++= this.exportAllData
    val request = apiRequestFactory
                    .createAPIRequest(apiContext, parentId, companion.END_POINT, APIRequest.METHOD_POST, companion.allFields, sentParams, files)
    if (batchAPIRequest != null) {
      batchAPIRequest.addRequest(request)
    } else {
      request.execute[CUDResponse]() map {
        case Left(x) => throw new APIException("Cannot read return object from " + x)
        case Right(v) => if (validating) {
          Some(this)
        } else {
          v.id map {id =>
            injectObjectId(id)
          }
        }
      }
    }
  }

  def read(batchAPIRequest: BatchAPIRequest = null, readFields: Seq[String] = List(), params: Map[String, Any] = Map())
          (implicit format: Format[T], typeTag: TypeTag[T], ec: ExecutionContext): Future[T] = {
    val sentFields = if (readFields == null || readFields.isEmpty) companion.defaultReadFields else readFields
    val request = apiRequestFactory.createAPIRequest(apiContext, id, null, APIRequest.METHOD_GET, sentFields, params, Map())
    if (batchAPIRequest != null) {
      batchAPIRequest.addRequest(request)
    } else {
      request.execute[T]() map {
        case Left(x) => throw new APIException("Cannot read return object from " + x)
        case Right(v) => v
      }
    }
  }

  def update(batchAPIRequest: BatchAPIRequest = null, params: Map[String, Any] = Map(), files: Map[String, File] = Map(), validating: Boolean = false)
            (implicit format: Format[T], typeTag: TypeTag[T], m: Mappable[T], ec: ExecutionContext): Future[Option[T]] = {
    if (id == null) {
      return Future.failed(new CRUDException(s"$this object need to be created first."))
    }

    var sentParams: Map[String, Any] = Map()
    if (params != null) {
      sentParams ++= params
    }
    sentParams ++= this.exportData

    val request = apiRequestFactory
      .createAPIRequest(apiContext, id, null, APIRequest.METHOD_POST, companion.allFields, sentParams, files)
    if (batchAPIRequest != null) {
      batchAPIRequest.addRequest(request)
    } else {
      request.execute[CUDResponse]() map {
        case Left(x) => throw new APIException("Cannot read return object from " + x)
        case Right(v) => v.success match {
          case Some(true) =>
            if (!validating) {
              this.props.clearHistory()
            }
            Some(this)
          case _ => None
        }
      }
    }
  }

  def delete(batchAPIRequest: BatchAPIRequest = null, params: Map[String, Any] = Map())
          (implicit format: Format[T], typeTag: TypeTag[T], ec: ExecutionContext): Future[Option[Boolean]] = {
    if (id == null) {
      return Future.failed(new CRUDException(s"$this object need to be created first."))
    }
    val request = apiRequestFactory.createAPIRequest(apiContext, id, null, APIRequest.METHOD_DELETE, List(), params, Map())
    if (batchAPIRequest != null) {
      batchAPIRequest.addRequest(request)
    } else {
      request.execute[CUDResponse]() map {
        case Left(x) => throw new APIException("Cannot read return object from " + x)
        case Right(v) => v.success
      }
    }
  }

  def save(batchAPIRequest: BatchAPIRequest = null, params: Map[String, Any] = Map(), files: Map[String, File] = Map(), validating: Boolean = false)
          (implicit format: Format[T], typeTag: TypeTag[T], m: Mappable[T], ec: ExecutionContext): Future[Option[T]] = {
    if (id != null) {
      update(batchAPIRequest, params, files, validating)
    } else {
      create(batchAPIRequest, params, files, validating)
    }
  }

  def fetchConnections[U <: APINode[U]](companion: APINodeCompanion[U], readFields: Seq[String] = List(), params: Map[String, Any] = Map())
                        (implicit format: Format[U], typeTag: TypeTag[U], ec: ExecutionContext): Cursor[U] = {
    val endpoint = companion.END_POINT
    val sentFields = if (readFields == null || readFields.isEmpty) companion.defaultReadFields else readFields
    val sentParams = if (params == null || params.size == 0) Map("summary" -> true) else params + ("summary" -> true)
    val request = apiRequestFactory.createAPIRequest(apiContext, id, endpoint, APIRequest.METHOD_GET, sentFields, sentParams, Map())
    new Cursor[U](request)
  }

  def fetchAllConnections[U <: APINode[U]](companion: APINodeCompanion[U], readFields: Seq[String] = List(), params: Map[String, Any] = Map())
                         (implicit format: Format[U], typeTag: TypeTag[U], ec: ExecutionContext): Future[Either[JsValue, List[U]]] = {
    val endpoint = companion.END_POINT
    val sentFields = if (readFields == null || readFields.isEmpty) companion.defaultReadFields else readFields
    val request = apiRequestFactory.createAPIRequest(apiContext, id, endpoint, APIRequest.METHOD_GET, sentFields, params, Map())
    fetchAllConnections(params, request, List[U]())
  }

  def toStringAll(): String = {
    s"""info = ${this.toString}, context = $apiContext, parentId = $parentId, requestFactory = $apiRequestFactory"""
  }

  private def fetchAllConnections[U <: APINode[U]](params: Map[String, Any], request: APIRequest, currentResults: List[U])
                            (implicit format: Format[U], typeTag: TypeTag[U], ec: ExecutionContext): Future[Either[JsValue, List[U]]] = {

    request.getList[U](params) flatMap {
      case Left(x) => Future(Left(x))
      case Right(list) => {
        val newCurrentResults = currentResults ++ list
        if (request.canGoNext) {
          fetchAllConnections(params,request, newCurrentResults)
        } else {
          Future(Right(newCurrentResults))
        }
      }
    }
  }

  private def injectObjectId(id: String)(implicit m: Mappable[T]): T = {
    val currentData = APINode.mapify(this)
    val newData = currentData + ("id" -> id)
    val result = APINode.materialize[T](newData)
    result.apiContext = this.apiContext
    result.apiRequestFactory = this.apiRequestFactory
    result.parentId = this.parentId
    result.props = new PropsContainer(newData)
    result
  }
  private def exportData(implicit m: Mappable[T]): Map[String, Any] = {
    if (props == null) {
      props = new PropsContainer(APINode.mapify(this))
    }
    props.exportData
  }

  private def exportAllData(implicit m: Mappable[T]): Map[String, Any] = {
    val allProps = new PropsContainer(APINode.mapify(this))
    allProps.exportAllData
  }

  def companion: APINodeCompanion[T]
}

trait APINodeCompanion[T <: APINode[T]] {
  def END_POINT(implicit typeTag: TypeTag[T]): String = APINode.getEndPoint[T]

  def allFields(implicit typeTag: TypeTag[T]): Seq[String] = APINode.classAccessors[T]

  def defaultReadFields(implicit typeTag: TypeTag[T]): Seq[String] = allFields

  implicit val format: Format[T]

}

object APINode {
  def classAccessors[T: TypeTag]: List[String] = typeOf[T].members.collect {
    case m: MethodSymbol if m.isCaseAccessor => m.name.toString
  }.toList

  def getEndPoint[T <: APINode[T]](implicit typeTag: TypeTag[T]): String = {
    val fullName = typeTag.tpe.typeSymbol.asClass.fullName
    val className = fullName.substring(fullName.lastIndexOf(".") + 1)
    s"${className.toLowerCase(Locale.ENGLISH)}s"
  }

  def mapify[T](t: T)(implicit m: Mappable[T]) = m.toMap(t)

  def materialize[T: Mappable](map: Map[String, Any]) =
    implicitly[Mappable[T]].fromMap(map)
}
