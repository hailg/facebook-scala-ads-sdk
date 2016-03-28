package com.fourseasapp.facebookads.network

import java.io.File
import java.net.URLEncoder
import java.util.Locale
import java.util.function.BiConsumer
import javax.annotation.Nullable

import com.fourseasapp.facebookads.{APIContext, APIException, Mappable}
import com.google.inject.assistedinject.{Assisted, AssistedInject}
import org.slf4j.LoggerFactory
import org.asynchttpclient.{AsyncHttpClient, RequestBuilder, Response}
import org.asynchttpclient.request.body.multipart.{FilePart, StringPart}
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.ws.ahc.AhcWSResponse
import play.api.libs.ws.{WSClient, WSResponse}

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.reflect.runtime.universe._

/**
  * Created by hailegia on 3/12/2016.
  */
class APIRequest @AssistedInject()(wsClient: WSClient,
                                   apiRequestFactory: APIRequestFactory,
                                   @Assisted apiContext: APIContext,
                                   @Nullable @Assisted("nodeId") nodeId: String,
                                   @Nullable @Assisted("endpoint") endpoint: String,
                                   @Assisted("method") method: String,
                                   @Assisted returnFields: Seq[String],
                                   @Assisted params: Map[String, Any],
                                   @Assisted files: Map[String, File]) {

  private var _totalResultCount:Option[Int] = None
  private var paging: Option[Paging] = None

  private val parentId = if (endpoint != null) nodeId else null

  def getList[T <: APINode[T]](extraParams: Map[String, Any] = Map())
                (implicit format: Format[T], ec: ExecutionContext): Future[Either[JsValue, List[T]]] = {
    callInternalList(APIRequest.METHOD_GET, extraParams)
  }

  def totalResultCount = _totalResultCount

  def canGoNext = paging.isDefined && paging.get.next.isDefined

  def getNext[T <: APINode[T]](extraParams: Map[String, Any] = Map())
                (implicit format: Format[T], ec: ExecutionContext): Future[Either[JsValue, List[T]]] = {

    if (canGoNext) {
      var nextExtraParams = Map[String, Any]()
      if (extraParams != null) {
        nextExtraParams ++= extraParams
      }
      nextExtraParams += "after" -> paging.get.cursors.after
      callInternalList(APIRequest.METHOD_GET, nextExtraParams)
    } else {
      Future(Right(List()))
    }
  }

  def execute[T](extraParams: Map[String, Any] = Map())
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

  private def callInternal[T](method: String, extraParams: Map[String, Any])
                             (implicit format: Format[T], ec: ExecutionContext): Future[Either[JsValue, T]]  = {

    val result: Future[WSResponse] = prepareRequest(method, extraParams)
    result map {
      wsResponse =>
        wsResponse.json.validate[T].fold(
        invalid => Left(wsResponse.json),
        obj => {
          if (obj.isInstanceOf[APINode[_]]) {
            val resultObj = obj.asInstanceOf[APINode[_]]
            resultObj.apiContext = apiContext
            resultObj.apiRequestFactory = apiRequestFactory
            if (parentId != null) {
              resultObj.parentId = parentId
            }
            Right(resultObj.asInstanceOf[T])
          } else {
            Right(obj)
          }
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
        paging = (wsResponse.json \ "paging").validate[Paging].asOpt
        _totalResultCount = (wsResponse.json \ "summary" \ "total_count").validate[Int].asOpt
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
    if (returnFields != null && returnFields.size > 0) {
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

  def getBatchInfo[T <: APINode[T]](): BatchRequestInfo[T] = {
    var allParams = Map[String, Any]()
    if (params != null) {
      allParams ++= params
    }
    if (returnFields != null && returnFields.size > 0) {
      allParams += ("fields" -> returnFields.mkString(","))
    }
    val filesMap = allParams.filter(p => p._2.isInstanceOf).mapValues(f => f.asInstanceOf[File])
    val bodies = for {p <- allParams } yield (p._1 + "=" + URLEncoder.encode(p._2.toString, "UTF-8"))
    val relativeUrl = method match {
      case APIRequest.METHOD_POST =>s"$nodeId/$endpoint"
      case _ => nodeId
    }
    BatchRequestInfo[T](method, relativeUrl, bodies.mkString("&"), parentId, apiRequestFactory, apiContext, filesMap, Promise[T]())
  }
}

class BatchAPIRequest @AssistedInject()(apiRequestFactory: APIRequestFactory, @Assisted() context: APIContext) {
  var requests: Seq[BatchRequestInfo[_]] = Seq()

  def addRequest[T <: APINode[T]](request: APIRequest): Future[T] = {
    val requestInfo = request.getBatchInfo[T]()
    requests = requestInfo +: requests
    requestInfo.promise.future
  }

  def execute[T <: APINode[T]](extraParams: Map[String, Any] = Map())
                              (implicit format: Format[T], m: Mappable[T], ec: ExecutionContext): Future[List[Either[JsValue, T]]] = {
    var allFiles = Map[String, File]()
    var batch = Json.arr()
    requests.foreach(info => {
      var element = Json.obj("method" -> info.method, "relative_url" -> info.relativePath)
      if (info.body != null) {
        element = element + ("body", JsString(info.body))
      }
      if (info.files != null && info.files.size > 0) {
        element = element + ("attached_files", JsString(info.files.keys.mkString(",")))
        allFiles ++= info.files
      }
      batch = batch.append(element)
    })

    val request = apiRequestFactory.createAPIRequest(context, null, null, APIRequest.METHOD_POST, List(), Map("batch" -> batch), allFiles)
    request.callRaw(APIRequest.METHOD_POST).map {
      case JsArray(allResponses) => {
        var results = ListBuffer[Either[JsValue, T]]()
        var index = 0
        for (response <- allResponses) {
          val requestInfo = requests(index)
          index += 1
          if (response == JsNull) {
            results += null
            requestInfo.success(null)
          } else {
            val code = (response \ "code").get.as[Int]
            var node: Either[JsValue, T] = Left(response)
            if (code == 200) {
              (response \ "body").asOpt[String] foreach {body =>
                val bodyJson = Json.parse(body)
                bodyJson.validate[T].fold(
                  _ => (bodyJson \ "id").asOpt[String] foreach {id =>
                    node = Right(APINode.materialize(Map[String, Any]("id" -> id)))
                  },
                  x => node = Right(x)
                )
              }
            }
            results += node
            node match {
              case Left(_) => requestInfo.promise.failure(new APIException(response.toString()))
              case Right(nodeValue) => {
                nodeValue.apiContext = requestInfo.apiContext
                nodeValue.apiRequestFactory = requestInfo.apiRequestFactory
                if (requestInfo.parentId != null) {
                  nodeValue.parentId = requestInfo.parentId
                }
                requestInfo.success(nodeValue)
              }
            }
          }
        }
        results.toList
      }
      case x => throw new APIException(x.toString())
    }
  }

}

case class BatchRequestInfo[T <: APINode[T]](method: String, relativePath: String, body: String, parentId: String,
                                             apiRequestFactory: APIRequestFactory, apiContext: APIContext,
                                             files: Map[String, File], promise: Promise[T]) {
  def success(x: Any) = promise.success(x.asInstanceOf[T])
}

case class CUDResponse(id: Option[String], success: Option[Boolean])

object CUDResponse {
  implicit val CUDResponseFormat: Format[CUDResponse] = (
      (JsPath \ "id").formatNullable[String] and
      (JsPath \ "success").formatNullable[Boolean]
    )(CUDResponse.apply, unlift(CUDResponse.unapply))
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

  def createAPIBatchRequest(apiContext: APIContext): BatchAPIRequest
}
