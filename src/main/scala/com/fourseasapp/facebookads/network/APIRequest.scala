package com.fourseasapp.facebookads.network

import java.io.File
import java.net.URLEncoder
import java.util.Locale
import java.util.function.BiConsumer

import com.fourseasapp.facebookads.{APIException, APIContext}
import com.google.inject.assistedinject.{Assisted, AssistedInject}
import org.slf4j.LoggerFactory
import org.asynchttpclient.{Response, RequestBuilder, AsyncHttpClient}
import org.asynchttpclient.request.body.multipart.{StringPart, FilePart}

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.ws.ahc.AhcWSResponse
import play.api.libs.ws.{WSClient, WSResponse}

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._
import scala.concurrent.{Promise, ExecutionContext, Future}

/**
  * Created by hailegia on 3/12/2016.
  */
class APIRequest @AssistedInject()(wsClient: WSClient,
                                   apiRequestFactory: APIRequestFactory,
                                   @Assisted apiContext: APIContext,
                                   @Assisted("nodeId") nodeId: String,
                                   @Assisted("endpoint") endpoint: String,
                                   @Assisted("method") method: String,
                                   @Assisted returnFields: Seq[String],
                                   @Assisted params: Map[String, Any],
                                   @Assisted files: Map[String, File]) {

  private var paging: Paging = null

  private val parentId = if (endpoint != null) nodeId else null

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

  def execute[T <: APINode[T]]()(extraParams: Map[String, Any] = Map())
                              (implicit format: Format[T], ec: ExecutionContext): Future[Either[JsValue, T]] = {
    callInternal(method, extraParams)
  }

  def setMethod(newMethod: String): APIRequest = {
    new APIRequest(wsClient, apiRequestFactory, apiContext, nodeId, endpoint, newMethod, returnFields, params, files)
  }

  def setParams(newParams: Map[String, Any]): APIRequest = {
    new APIRequest(wsClient, apiRequestFactory, apiContext, nodeId, endpoint, method, returnFields, newParams, files)
  }

  def setReturnFields(newReturnFields: List[String]): APIRequest = {
    new APIRequest(wsClient, apiRequestFactory, apiContext, nodeId, endpoint, method, newReturnFields, params, files)
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

  private[network] def callRaw(method: String)(implicit ec: ExecutionContext): Future[JsValue] = {
    prepareRequest(method, Map()).map(_.json)
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

  private def prepareRequest(method: String, extraParams: Map[String, Any]): Future[WSResponse] = {

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
        val hasFile = files != null && files.size > 0
        if (hasFile) {
          val client: AsyncHttpClient = wsClient.underlying
          val requestBuilder = new RequestBuilder().setUrl(apiUrl)
          allParams.foreach(e => {
            requestBuilder.addBodyPart(new StringPart(e._1, e._2.toString))
          })
          files.foreach(e => {
            requestBuilder.addBodyPart(new FilePart(e._1, e._2, APIRequest.getContentTypeForFile(e._2)))
          })
          val p = Promise[WSResponse]()
          val realRequest = requestBuilder.build()
          val requestFuture = client.executeRequest(realRequest).toCompletableFuture
          requestFuture.whenComplete(new BiConsumer[Response, Throwable] {
            override def accept(r: Response, t: Throwable): Unit = {
              if (t != null) {
                p.failure(t)
              } else {
                p.success(AhcWSResponse(r))
              }
            }
          })
          client.executeRequest(requestBuilder)
          p.future
        } else {
          request
            .post(allParams.mapValues(value => Seq(value.toString)))
        }
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
    } else if (nodeId != null) {
      s"${apiContext.endpointBase}/${apiContext.version}/${nodeId}"
    } else {
      s"${apiContext.endpointBase}/${apiContext.version}"
    }
  }

  def getNodePath(): String = {
    return nodeId
  }

  def getBatchInfo(): BatchRequestInfo = {
    var allParams = Map[String, Any]()
    if (params != null) {
      allParams ++= params
    }
    if (returnFields != null) {
      allParams += ("fields" -> returnFields.mkString(","))
    }
    val filesMap = allParams.filter(p => p._2.isInstanceOf).mapValues(f => f.asInstanceOf[File])
    val bodies = for {p <- allParams } yield (p._1 + "=" + URLEncoder.encode(p._2.toString, "UTF-8"))
    BatchRequestInfo(method, getApiUrl(), bodies.mkString("&"), filesMap)
  }
}

class BatchAPIRequest @AssistedInject()(apiRequestFactory: APIRequestFactory, @Assisted() context: APIContext) {
  var requests: Seq[APIRequest] = Seq()

  def addRequest(request: APIRequest): Unit = {
    requests = request +: requests
  }

  def execute[T <: APINode[T]]()(extraParams: Map[String, Any] = Map())
                              (implicit format: Format[T], ec: ExecutionContext): Future[List[Either[APIException, T]]] = {
    var allFiles = Map[String, File]()
    val batch = requests.map(request => {
      val info = request.getBatchInfo()
      var element = Json.obj("method" -> info.method, "relative_url" -> info.relativePath)
      if (info.body != null) {
        element = element + ("body", JsString(info.body))
      }
      if (info.files != null) {
        element = element + ("attached_files", JsString(info.files.keys.mkString(",")))
        allFiles ++= info.files
      }
      element
    })
    val request = apiRequestFactory.createAPIRequest(context, null, null, null, null, Map("batch" -> batch), allFiles)
    request.callRaw(APIRequest.METHOD_POST).map {case JsArray(allResponses)=> {
      var results = ListBuffer[Either[APIException, T]]()
      for (response <- allResponses) {
        if (response == JsNull) {
          results += null
        } else {
          val code = (response \ "code").get.as[Int]
          if (code == 200) {
            results += Right((response \ "body").get.as[T])
          } else {
            results += Left(new APIException(response.toString()))
          }
        }
      }
      results.toList
    }}
  }

}

case class BatchRequestInfo(method: String, relativePath: String, body: String, files: Map[String, File])

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

  val fileToContentTypeMap = Map(
    ".atom" -> "application/atom+xml",
    ".rss" -> "application/rss+xml",
    ".xml" -> "application/xml",
    ".csv" -> "text/csv",
    ".txt" -> "text/plain"
  )

  def getContentTypeForFile(file: File): String = {
    for (e <- fileToContentTypeMap) {
      if (file.getName.toLowerCase(Locale.getDefault).endsWith(e._1)) {
        return e._2
      }
    }
    return null
  }
}

trait APIRequestFactory {
  def createAPIRequest(@Assisted apiContext: APIContext,
                       @Assisted("nodeId") nodeId: String,
                       @Assisted("endpoint") endpoint: String,
                       @Assisted("method") method: String,
                       @Assisted returnFields: Seq[String],
                       @Assisted params: Map[String, Any],
                       @Assisted files: Map[String, File]): APIRequest

  def createAPIBatchRequest(apiContext: APIContext)
}
