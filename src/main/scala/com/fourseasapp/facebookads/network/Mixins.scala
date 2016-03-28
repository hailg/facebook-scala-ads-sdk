package com.fourseasapp.facebookads.network

import java.io.File

import com.fourseasapp.facebookads.Mappable
import com.fourseasapp.facebookads.model.{AdLabel, EnumConfiguredStatus}
import play.api.libs.json.{Format, JsValue}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.runtime.universe._

/**
  * Created by hailegia on 3/27/2016.
  */
trait CanValidate[T <: APINode[T]] {self: T =>
  def validate(params: Map[String, Any] = Map())(implicit format: Format[T], typeTag: TypeTag[T], m: Mappable[T],
                                                         ec: ExecutionContext): Future[Option[T]] = {
    var sentParams: Map[String, Any] = Map()
    if (params != null) {
      sentParams ++= params
    }
    sentParams += "execution_options" -> "validate_only"
    save(params = sentParams, validating = true)
  }
}

trait CanArchive[T <: APINode[T]] {self: T =>
  def delete(batchAPIRequest: BatchAPIRequest = null, params: Map[String, Any] = Map())
            (implicit format: Format[T], typeTag: TypeTag[T], m: Mappable[T], ec: ExecutionContext): Future[Option[Boolean]] = {
    var sentParams: Map[String, Any] = Map()
    if (params != null) {
      sentParams ++= params
    }
    sentParams += "status" -> EnumConfiguredStatus.DELETED
    update(batchAPIRequest, sentParams) map {
      case Some(_) => Some(true)
      case None => Some(false)
    }
  }

  def archive(batchAPIRequest: BatchAPIRequest = null, params: Map[String, Any] = Map())
            (implicit format: Format[T], typeTag: TypeTag[T], m: Mappable[T], ec: ExecutionContext): Future[Option[Boolean]] = {
    var sentParams: Map[String, Any] = Map()
    if (params != null) {
      sentParams ++= params
    }
    sentParams += "status" -> EnumConfiguredStatus.ARCHIVED
    update(batchAPIRequest, sentParams) map {
      case Some(_) => Some(true)
      case None => Some(false)
    }
  }
}

trait CannotCreate[T <: APINode[T]] {self: T =>
  def create(batchAPIRequest: BatchAPIRequest = null, params: Map[String, Any] = Map(), files: Map[String, File] = Map(), validating: Boolean = false)
            (implicit format: Format[T], typeTag: TypeTag[T], m: Mappable[T], ec: ExecutionContext): Future[Option[T]] = {
    Future.failed(new UnsupportedOperationException(s"$this cannot be created."))
  }
}

trait CannotDelete[T <: APINode[T]] {self: T =>
  def delete(batchAPIRequest: BatchAPIRequest = null, params: Map[String, Any] = Map())
            (implicit format: Format[T], typeTag: TypeTag[T], ec: ExecutionContext): Future[Option[Boolean]] = {
    Future.failed(new UnsupportedOperationException(s"$this cannot be deleted."))
  }
}

trait CannotUpdate[T <: APINode[T]] {self: T =>
  def update(batchAPIRequest: BatchAPIRequest = null, params: Map[String, Any] = Map(), files: Map[String, File] = Map(), validating: Boolean = false)
            (implicit format: Format[T], typeTag: TypeTag[T], m: Mappable[T], ec: ExecutionContext): Future[Option[T]] = {
    Future.failed(new UnsupportedOperationException(s"$this cannot be deleted."))
  }
}

trait HasAdLabels[T <: APINode[T]] {self: T =>
  def addLabels(labels: Seq[AdLabel])(implicit ex: ExecutionContext) = {
    val labelIds = labels.map(label => Map("id" -> label.id))
    val params = Map[String, Any]("adlabels" -> labelIds)
    val request = apiRequestFactory
      .createAPIRequest(apiContext, self.id, "adlabels", APIRequest.METHOD_POST, Seq(), params, Map())
    request.execute[JsValue]()
  }

  def removeLabels(labels: Seq[AdLabel])(implicit ex: ExecutionContext) = {
    val labelIds = labels.map(label => Map("id" -> label.id))
    val params = Map[String, Any]("adlabels" -> labelIds)
    val request = apiRequestFactory
      .createAPIRequest(apiContext, self.id, "adlabels", APIRequest.METHOD_DELETE, Seq(), params, Map())
    request.execute[JsValue]()
  }

}