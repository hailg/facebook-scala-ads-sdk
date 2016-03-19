package com.fourseasapp.facebookads

import com.fourseasapp.facebookads.network.{APINode, APIRequest}
import play.api.libs.json.{Format, JsValue}

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by hailegia on 3/14/2016.
  */
class Cursor[T <: APINode[T]](apiRequest: APIRequest)
                          (implicit format: Format[T], ec: ExecutionContext) {
  private var currentItems = List[T]()
  private var index = 0
  private var started = false

  def size: Option[Int] = apiRequest.totalResultCount

  def hasNext: Boolean = !started || index < currentItems.size || (index == currentItems.size && apiRequest.canGoNext)

  def next(): Future[T] = {
    if (index < currentItems.size) {
      val curIndex = index
      index += 1
      Future(currentItems(curIndex))
    } else {
      apiRequest.getNext() map {
        case Left(x) => throw new APIException("Cannot read next on cursor. Read value: " + x)
        case Right(list) => {
          started = true
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
    if (!started) {
      apiRequest.getList() flatMap {
        case Left(x) => Future.failed(throw new APIException("Cannot read next on cursor. Read value: " + x))
        case Right(list) => {
          started = true
          _fetchRemaining(fetchedItems ++ list)
        }
      }
    } else if (apiRequest.canGoNext) {
      apiRequest.getNext() flatMap {
        case Left(x) => Future.failed(throw new APIException("Cannot read next on cursor. Read value: " + x))
        case Right(list) => {
          _fetchRemaining(fetchedItems ++ list)
        }
      }
    } else {
      Future(fetchedItems)
    }
  }

}
