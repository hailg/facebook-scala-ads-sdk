package com.fourseasapp.facebookads.model

import com.fourseasapp.facebookads.Cursor
import com.fourseasapp.facebookads.network.{APINode, APINodeCompanion}

import scala.concurrent.ExecutionContext

/**
  * Created by hailegia on 3/13/2016.
  */
case class AdAccount (id: String,
                     account_id: Option[String] = None,
                     account_status: Option[Int] = None,
                     age: Option[Float] = None,
                     amount_spent: Option[String] = None,
                     asset_score: Option[Float] = None,
                     balance: Option[String] = None,
                     business_city: Option[String] = None,
                     business_country_code: Option[String] = None,
                     business_name: Option[String] = None,
                     business_state: Option[String] = None,
                     business_street: Option[String] = None,
                     business_street2: Option[String] = None,
                     business_zip: Option[String] = None,
                     can_create_brand_lift_study: Option[Boolean] = None,
                     capabilities: Option[Seq[String]] = None,
                     created_time: Option[String] = None,
                     currency: Option[String] = None,
                     disable_reason: Option[Long] = None,
                     end_advertiser: Option[String] = None,
                     end_advertiser_name: Option[String] = None,
                     funding_source: Option[String] = None,
                     has_migrated_permissions: Option[Boolean] = None,
                     io_number: Option[String] = None,
                     is_notifications_enabled: Option[Boolean] = None,
                     is_personal: Option[Int] = None,
                     is_prepay_account: Option[Boolean] = None,
                     is_tax_id_required: Option[Boolean] = None,
                     last_used_time: Option[String] = None,
                     line_numbers: Option[List[Int]] = None,
                     media_agency: Option[String] = None,
                     min_campaign_group_spend_cap: Option[String] = None,
                     min_daily_budget: Option[Int] = None,
                     name: Option[String] = None,
                     offsite_pixels_tos_accepted: Option[Boolean] = None,
                     owner: Option[String] = None,
                     partner: Option[String] = None,
                     spend_cap: Option[String] = None,
                     tax_id: Option[String] = None,
                     tax_id_status: Option[Int] = None,
                     tax_id_type: Option[String] = None,
                     timezone_id: Option[Int] = None,
                     timezone_name: Option[String] = None,
                     timezone_offset_hours_utc: Option[Float] = None,
                     tos_accepted: Option[Map[String, Int]] = None,
                     user_role: Option[String] = None,
                     vertical_name: Option[String] = None) extends APINode[AdAccount] {

  type Fields = AdAccount.Fields

  override def companion = AdAccount

  def getCampaigns(params: Map[String, Any] = Map())(implicit ec: ExecutionContext): Cursor[Campaign] = {
    import Campaign._
    fetchConnections(Campaign, params = params)
  }

  def getConnectionObjects(params: Map[String, Any] = Map())(implicit ec: ExecutionContext): Cursor[ConnectionObject] = {
    import ConnectionObject._
    fetchConnections(ConnectionObject, params = params)
  }
}

object AdAccount extends APINodeCompanion[AdAccount] {
  import enumeratum._
  import org.cvogt.play.json.Jsonx
  import play.api.libs.json.Format

  sealed trait Fields extends EnumEntry

  object Fields extends Enum[Fields] with PlayJsonEnum[Fields] {
    val values = findValues

    case object vertical_name extends Fields
    case object user_role extends Fields
    case object tos_accepted extends Fields
    case object timezone_offset_hours_utc extends Fields
    case object timezone_name extends Fields
    case object timezone_id extends Fields
    case object tax_id_type extends Fields
    case object tax_id_status extends Fields
    case object tax_id extends Fields
    case object spend_cap extends Fields
    case object partner extends Fields
    case object owner extends Fields
    case object offsite_pixels_tos_accepted extends Fields
    case object name extends Fields
    case object min_daily_budget extends Fields
    case object min_campaign_group_spend_cap extends Fields
    case object media_agency extends Fields
    case object line_numbers extends Fields
    case object last_used_time extends Fields
    case object is_tax_id_required extends Fields
    case object is_prepay_account extends Fields
    case object is_personal extends Fields
    case object is_notifications_enabled extends Fields
    case object io_number extends Fields
    case object has_migrated_permissions extends Fields
    case object funding_source extends Fields
    case object end_advertiser_name extends Fields
    case object end_advertiser extends Fields
    case object disable_reason extends Fields
    case object currency extends Fields
    case object created_time extends Fields
    case object capabilities extends Fields
    case object can_create_brand_lift_study extends Fields
    case object business_zip extends Fields
    case object business_street2 extends Fields
    case object business_street extends Fields
    case object business_state extends Fields
    case object business_name extends Fields
    case object business_country_code extends Fields
    case object business_city extends Fields
    case object balance extends Fields
    case object asset_score extends Fields
    case object amount_spent extends Fields
    case object age extends Fields
    case object account_status extends Fields
    case object account_id extends Fields
    case object id extends Fields

  }

  override implicit val format: Format[AdAccount] = Jsonx.formatCaseClass[AdAccount]
}
