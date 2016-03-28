package com.fourseasapp.facebookads.model

import com.fourseasapp.facebookads.Cursor
import com.fourseasapp.facebookads.network.{APINode, APINodeCompanion}
import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format

import scala.concurrent.ExecutionContext

/**
  * Created by hailegia on 3/11/2016.
  */
case class AdUser (id: String = "me", name: String = null, permission: Option[Int] = None, role: Option[Int] = None) extends APINode[AdUser] {
  type Fields = AdUser.Fields

  override def companion = AdUser

  def getAdAccounts(params: Map[String, Any] = Map())(implicit ec: ExecutionContext): Cursor[AdAccount] = {
    import AdAccount._
    fetchConnections(AdAccount, params = params)
  }

}

object AdUser extends APINodeCompanion[AdUser] {
  import enumeratum._

  object Permission {
    val AccountAdmin = 1
    val AdManagerRead = 2
    val AdManagerWrite = 3
    val BillingRead = 4
    val BillingWrite = 5
    val Reports = 7
  }

  object Role {
    val Administrator = 1001
    val Analyst = 1002
    val Manager = 1003
  }

  sealed trait Fields extends EnumEntry

  object Fields extends Enum[Fields] with PlayJsonEnum[Fields] {
    val values = findValues

    case object id extends Fields
    case object name extends Fields
    case object permissions extends Fields
    case object role extends Fields
  }

  override implicit val format: Format[AdUser] = Jsonx.formatCaseClass[AdUser]
}