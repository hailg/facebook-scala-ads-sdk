package com.fourseasapp.facebookads.model

import com.fourseasapp.facebookads.{Cursor, APIContext, APIException}
import com.fourseasapp.facebookads.network.{APINode, APIRequestFactory}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by hailegia on 3/11/2016.
  */
case class AdUser(id: String = "me")(context: APIContext) extends APINode[AdUser] {
  apiContext = context

  override def endpoint: String = null

  override def allFields: Seq[String] = null

  override def defaultReadFields: Seq[String] = null

  def getAdAccounts(params: Map[String, Any] = Map())(implicit ec: ExecutionContext): Future[Cursor[AdAccount]] = {
    import AdAccount._
    fetchConnections(AdAccount.END_POINT, AdAccount.ALL_FIELDS, params)
  }
}
