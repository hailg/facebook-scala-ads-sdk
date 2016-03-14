package com.fourseasapp.facebookads.network

import com.fourseasapp.facebookads.APIContext
import com.google.inject.assistedinject.{Assisted, AssistedInject}
import org.slf4j.LoggerFactory
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by hailegia on 3/12/2016.
  */
class APIRequest @AssistedInject()(wsClient: WSClient,
                                   @Assisted apiContext: APIContext,
                                   @Assisted apiRequestFactory: APIRequestFactory,
                                   @Assisted("nodeId") nodeId: String,
                                   @Assisted("endpoint") endpoint: String,
                                   @Assisted returnFields: Seq[String],
                                   @Assisted params: Map[String, Any]) {

  private var paging: Paging = null

  private val parentId = if (endpoint != null) nodeId else null

  def get[T <: APINode[T]](extraParams: Map[String, Any] = Map())
            (implicit format: Format[T], ec: ExecutionContext): Future[Either[JsValue, T]] = {
    callInternal(APIRequest.METHOD_GET, extraParams)
  }

  def getList[T <: APINode[T]](extraParams: Map[String, Any] = Map())
                (implicit format: Format[T], ec: ExecutionContext): Future[Either[JsValue, List[T]]] = {
    callInternalList(APIRequest.METHOD_GET, extraParams)
  }

  def canGoNext = paging != null && paging.next.isDefined

  def getNext[T <: APINode[T]](extraParams: Map[String, Any] = Map())
                (implicit format: Format[T], ec: ExecutionContext): Future[Either[JsValue, List[T]]] = {

    if (canGoNext) {
      var nextExtraParams = Map[String, Any]()
      if (extraParams != null) {
        nextExtraParams ++= extraParams
      }
      nextExtraParams += "after" -> paging.cursors.after
      callInternalList(APIRequest.METHOD_GET, nextExtraParams)
    } else {
      Future(Right(List()))
    }
  }

  def canGoPrev = paging != null && paging.previous.isDefined

  def getPrev[T <: APINode[T]](extraParams: Map[String, Any] = Map())
                (implicit format: Format[T], ec: ExecutionContext): Future[Either[JsValue, List[T]]] = {

    if (canGoPrev) {
      var prevExtraParams = Map[String, Any]()
      if (extraParams != null) {
        prevExtraParams ++= extraParams
      }
      prevExtraParams += "before" -> paging.cursors.before
      callInternalList(APIRequest.METHOD_GET, prevExtraParams)
    } else {
      Future(Right(List()))
    }
  }

  def post[T <: APINode[T]](extraParams: Map[String, Any] = Map())
             (implicit format: Format[T], ec: ExecutionContext): Future[Either[JsValue, T]] = {
    callInternal(APIRequest.METHOD_POST, extraParams)
  }

  def delete[T <: APINode[T]](extraParams: Map[String, Any] = Map())
               (implicit format: Format[T], ec: ExecutionContext): Future[Either[JsValue, T]] = {
    callInternal(APIRequest.METHOD_DELETE, extraParams)
  }

  def setParams(newParams: Map[String, Any]): APIRequest = {
    new APIRequest(wsClient, apiContext, apiRequestFactory, nodeId, endpoint, returnFields, newParams)
  }

  def setReturnFields(newReturnFields: List[String]): APIRequest = {
    new APIRequest(wsClient, apiContext, apiRequestFactory, nodeId, endpoint, newReturnFields, params)
  }

  private def callInternal[T <: APINode[T]](method: String, extraParams: Map[String, Any])
                             (implicit format: Format[T], ec: ExecutionContext): Future[Either[JsValue, T]]  = {

    val result: Future[WSResponse] = prepareRequest(method, extraParams)
    result map {
      wsResponse =>
        wsResponse.json.validate[T].fold(
        invalid => Left(wsResponse.json),
        obj => {
          obj.apiContext = apiContext
          obj.apiRequestFactory = apiRequestFactory
          if (parentId != null) {
            obj.parentId = parentId
          }
          Right(obj)
        }
      )
    }
  }

  private def callInternalList[T <: APINode[T]](method: String, extraParams: Map[String, Any])
                                 (implicit format: Format[T], ec: ExecutionContext): Future[Either[JsValue, List[T]]]  = {

    val result: Future[WSResponse] = prepareRequest(method, extraParams)

    result map {
      wsResponse =>
        (wsResponse.json \ "paging").validate[Paging].fold(
          _ => Left(wsResponse.json),
          obj => {
            paging = obj
            (wsResponse.json \ "data").validate[List[T]].fold(
              invalid => {
                APIRequest.logger.error(invalid.toString())
                APIRequest.logger.error(Json.stringify(wsResponse.json))
                Left(wsResponse.json)
              },
              data => {
                data.foreach(obj => {
                  obj.apiContext = apiContext
                  obj.apiRequestFactory = apiRequestFactory
                  if (parentId != null) {
                    obj.parentId = parentId
                  }
                })
                Right(data)
              }
            )
          }
        )
    }
  }

  private def prepareRequest[T](method: String, extraParams: Map[String, Any]): Future[WSResponse] = {
    val allParams = getAllParams(extraParams)
    val apiUrl = getApiUrl()
    val request = wsClient
      .url(apiUrl)
      .withRequestTimeout((apiContext.timeOut * 1000).seconds)
      .withHeaders("User-Agent" -> APIContext.USER_AGENT)

    val result = method match {
      case APIRequest.METHOD_GET => {
        request
          .withQueryString(
            allParams.toSeq.map {
              case (k, v) => (k, v.toString)
            }: _*)
          .get()
      }
      case APIRequest.METHOD_POST => {
        request
          .post(allParams.mapValues(value => Seq(value.toString)))
      }
      case APIRequest.METHOD_DELETE => {
        request
          .withQueryString(
            allParams.toSeq.map {
              case (k, v) => (k, v.toString)
            }: _*)
          .delete()
      }
      case _ => Future.failed(new scala.RuntimeException(s"HTTP METHOD ${method} is not supported."))
    }
    result
  }

  private def getAllParams(extraParams: Map[String, Any]): Map[String, Any] = {
    var result = Map[String, Any]()
    if (params != null) {
      result ++= params
    }
    if (extraParams != null) {
      result ++= extraParams
    }
    result += ("access_token" -> apiContext.accessToken)
    if (apiContext.hasAppSecret) {
      result += ("appsecret_proof" -> apiContext.getAppSecretProof())
    }
    if (returnFields != null) {
      result += ("fields" -> returnFields.mkString(","))
    }

    result
  }

  def getApiUrl(): String = {
    if (endpoint != null) {
      s"${apiContext.endpointBase}/${apiContext.version}/${nodeId}/${endpoint}"
    } else {
      s"${apiContext.endpointBase}/${apiContext.version}/${nodeId}"
    }
  }
}

case class PagingCursor(after: String, before: String)

case class Paging(cursors: PagingCursor, previous: Option[String], next: Option[String])

object Paging {
  implicit val pagingCursorFormat = Json.format[PagingCursor]

  implicit val pagingFormats: Format[Paging] = (
      (JsPath \ "cursors").format[PagingCursor] and
      (JsPath \ "previous").formatNullable[String] and
      (JsPath \ "next").formatNullable[String]
    )(Paging.apply, unlift(Paging.unapply))
}

object APIRequest {
  val METHOD_POST = "POST"
  val METHOD_GET = "GET"
  val METHOD_DELETE = "DELETE"
  val logger = LoggerFactory.getLogger(classOf[APIRequest])
}

trait APIRequestFactory {
  def createAPIRequest(@Assisted apiContext: APIContext,
                       @Assisted apiRequestFactory: APIRequestFactory,
                       @Assisted("nodeId") nodeId: String,
                       @Assisted("endpoint") endpoint: String,
                       @Assisted returnFields: Seq[String],
                       @Assisted params: Map[String, Any]): APIRequest
}
