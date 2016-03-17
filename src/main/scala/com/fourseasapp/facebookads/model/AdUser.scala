package com.fourseasapp.facebookads.model

import com.fourseasapp.facebookads.{Cursor, APIContext, APIException}
import com.fourseasapp.facebookads.network.{APINodeCompanion, APINode, APIRequestFactory}
import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by hailegia on 3/11/2016.
  */
case class AdUser(id: String = "me") extends APINode[AdUser] {

  override def allFields: Seq[String] = null

  override def defaultReadFields: Seq[String] = null

  def getAdAccounts(params: Map[String, Any] = Map())(implicit ec: ExecutionContext): Future[Cursor[AdAccount]] = {
    import AdAccount._
    fetchConnections(AdAccount.END_POINT, AdAccount.ALL_FIELDS, params)
  }
}

object AdUser extends APINodeCompanion[AdUser] {
  import enumeratum._

  sealed trait Fields extends EnumEntry

  object Fields extends Enum[Fields] with PlayJsonEnum[Fields] {
    val values = findValues

    case object id extends Fields
    case object name extends Fields
    case object permissions extends Fields
    case object role extends Fields
  }

  val ALL_FIELDS = Fields.values.map(v => v.entryName)

  override implicit val format: Format[AdUser] = Jsonx.formatCaseClass[AdUser]
}