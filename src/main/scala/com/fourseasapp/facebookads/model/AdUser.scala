package com.fourseasapp.facebookads.model

import com.fourseasapp.facebookads.{APIContext, APIException}
import com.fourseasapp.facebookads.network.{APINode, APIRequestFactory}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by hailegia on 3/11/2016.
  */
case class AdUser(id: String = "me")(context: APIContext) extends APINode[AdUser] {
  apiContext = context

  override def parentId: String = null

  override def endpoint: String = null

  override def allFields: List[String] = null

  override def defaultReadFields: List[String] = null

  def getAdAccounts(params: Map[String, Any] = Map())(implicit ec: ExecutionContext): Future[List[AdAccount]] = {
    import AdAccount._

    fetchAllConnections(AdAccount.END_POINT, AdAccount.ALL_FIELDS, params) map {
      case Left(jsValue) => throw new APIException("Cannot parse accounts from " + jsValue)
      case Right(data) => data
    }
  }
}
