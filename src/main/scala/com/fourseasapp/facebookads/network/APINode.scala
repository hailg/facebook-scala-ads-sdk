package com.fourseasapp.facebookads.network

import java.io.File
import java.util.Locale
import javax.inject.Inject

import com.fourseasapp.facebookads._
import play.api.libs.json.{Format, JsValue}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.runtime.universe._

/**
  * Created by hailegia on 3/12/2016.
  */
trait APINode[T <: APINode[T]] { self: T =>
  type Fields

  private var _context: APIContext = null
  private var _parentId: String = null

  private[network] var props: PropsContainer = null

  @Inject() private var _apiRequestFactory: APIRequestFactory = null

  def id: String

  def parentId: String = _parentId

  def parentId_=(value: String) = _parentId = value

  def apiContext = _context

  def apiContext_=(context: APIContext) = _context = context

  def copy(fields: Map[Fields, Any])(implicit m: Mappable[T]): T = {
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

  def copy(fields: (Fields, Any)*)(implicit m: Mappable[T]): T = {
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

  def create(params: Map[String, Any] = Map(), files: Map[String, File] = Map(), batchAPIRequest: BatchAPIRequest = null)
            (implicit format: Format[T], typeTag: TypeTag[T], m: Mappable[T], ec: ExecutionContext): Future[T] = {
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
      Future.successful(this)
    } else {
      request.execute[T]() map {
        case Left(x) => throw new APIException("Cannot read object from " + x)
        case Right(v) => v
      }
    }

  }

  def read(readFields: Seq[String] = null, params: Map[String, Any] = Map())
          (implicit format: Format[T], typeTag: TypeTag[T], ec: ExecutionContext): Future[T] = {
    val sentFields = if (readFields == null) companion.defaultReadFields else readFields
    val request = apiRequestFactory.createAPIRequest(apiContext, id, null, APIRequest.METHOD_GET, sentFields, params, Map())
    request.execute[T]() map {
      case Left(x) => throw new APIException("Cannot read object from " + x)
      case Right(v) => v
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

  private[network] def apiRequestFactory_=(value: APIRequestFactory) = _apiRequestFactory = value

  private[network] def apiRequestFactory = _apiRequestFactory

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
