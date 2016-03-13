package com.fourseasapp.facebookads.network

import javax.inject.Inject

import com.fourseasapp.facebookads.APIContext
import play.api.libs.json.{JsValue, Format}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by hailegia on 3/12/2016.
  */
trait APINode[T <: APINode[T]] {
  private var _context: APIContext = null
  @Inject() private var _apiRequestFactory: APIRequestFactory = null

  def id: String

  def parentId: String

  def endpoint: String

  def allFields: List[String]

  def defaultReadFields: List[String]

  def apiContext = _context

  def apiRequestFactory = _apiRequestFactory

  protected[network] def apiContext_=(context: APIContext): Unit = {
    _context = context
  }

  private[network] def apiRequestFactory_=(value: APIRequestFactory) = _apiRequestFactory = value

  def read(readFields: List[String] = defaultReadFields, params: Map[String, Any] = Map())
          (implicit format: Format[T], ec: ExecutionContext): Future[Either[JsValue, T]] = {
    val request = apiRequestFactory.createAPIRequest(apiContext, apiRequestFactory, id, null, readFields, Map())
    request.get[T](params)
  }

  def fetchConnections[U <: APINode[U]](endpoint: String, readFields: List[String] = List(), params: Map[String, Any] = Map())
                        (implicit format: Format[U], ec: ExecutionContext): Future[Either[JsValue, (List[U], APIRequest)]] = {
    val request = apiRequestFactory.createAPIRequest(apiContext, apiRequestFactory, id, endpoint, readFields, Map())
    request.getList[U](params) map {
      case Left(x) => Left(x)
      case Right(list) => Right(list, request)
    }
  }

  def fetchNextConnections[U <: APINode[U]](request: APIRequest, params: Map[String, Any] = Map())
                             (implicit format: Format[U], ec: ExecutionContext): Future[Either[JsValue, (List[U], APIRequest)]] = {

    request.getNext[U](params) map {
      case Left(x) => Left(x)
      case Right(list) => Right(list, request)
    }
  }

  def fetchPrevConnections[U <: APINode[U]](request: APIRequest, params: Map[String, Any] = Map())
                             (implicit format: Format[U], ec: ExecutionContext): Future[Either[JsValue, (List[U], APIRequest)]] = {

    request.getPrev[U](params) map {
      case Left(x) => Left(x)
      case Right(list) => Right(list, request)
    }
  }

  def fetchAllConnections[U <: APINode[U]](endpoint: String, readFields: List[String] = List(), params: Map[String, Any] = Map())
                         (implicit format: Format[U], ec: ExecutionContext): Future[Either[JsValue, List[U]]] = {
    val request = apiRequestFactory.createAPIRequest(apiContext, apiRequestFactory, id, endpoint, readFields, Map())
    fetchAllConnections(params, request, List[U]())
  }

  private def fetchAllConnections[U <: APINode[U]](params: Map[String, Any], request: APIRequest, currentResults: List[U])
                            (implicit format: Format[U], ec: ExecutionContext): Future[Either[JsValue, List[U]]] = {

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

  private def assureId: String = {
    val _id = id
    if (_id == null) {
      throw new NullPointerException("Id cannot be null")
    }
    _id
  }
}
