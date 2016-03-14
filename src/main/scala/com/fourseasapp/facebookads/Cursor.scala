package com.fourseasapp.facebookads

import com.fourseasapp.facebookads.network.{APINode, APIRequest}
import play.api.libs.json.{JsValue, Format}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by hailegia on 3/14/2016.
  */
class Cursor[T <: APINode[T]](firstItems: List[T], apiRequest: APIRequest, params: Map[String, Any])
                          (implicit format: Format[T], ec: ExecutionContext) {
  var currentItems = firstItems
  var index = 0

  def hasNext: Boolean = index < currentItems.size || (index == currentItems.size && apiRequest.canGoNext)

  def next(): Future[T] = {
    if (index < currentItems.size) {
      val curIndex = index
      index += 1
      Future(currentItems(curIndex))
    } else {
      apiRequest.getNext(params) map {
        case Left(x) => throw new APIException("Cannot read next on cursor. Read value: " + x)
        case Right(list) => {
          currentItems = list
          index = 1
          currentItems(0)
        }
      }
    }
  }

  def fetchRemaining(): Future[List[T]] = {
    if (index < currentItems.size) {
      _fetchRemaining(currentItems.slice(index, currentItems.size))
    } else {
      _fetchRemaining(Nil)
    }

  }

  private def _fetchRemaining(fetchedItems: List[T]): Future[List[T]] = {
    if (!apiRequest.canGoNext) {
      Future(fetchedItems)
    } else {
      apiRequest.getNext(params) flatMap {
        case Left(x) => Future.failed(throw new APIException("Cannot read next on cursor. Read value: " + x))
        case Right(list) => {
          _fetchRemaining(fetchedItems ++ list)
        }
      }
    }
  }

}
