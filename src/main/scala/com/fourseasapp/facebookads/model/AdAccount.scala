package com.fourseasapp.facebookads.model

import com.fourseasapp.facebookads.Cursor
import com.fourseasapp.facebookads.network.APINode
import enumeratum.EnumEntry
import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format

import scala.concurrent.{Future, ExecutionContext}

/**
  * Created by hailegia on 3/13/2016.
  */
case class AdAccount(id: String,
                     account_id: Option[String],
                     account_status: Option[Int],
                     age: Option[Float],
                     amount_spent: Option[String],
                     asset_score: Option[Float],
                     balance: Option[String],
                     business_city: Option[String],
                     business_country_code: Option[String],
                     business_name: Option[String],
                     business_state: Option[String],
                     business_street: Option[String],
                     business_street2: Option[String],
                     business_zip: Option[String],
                     can_create_brand_lift_study: Option[Boolean],
                     capabilities: Option[Seq[String]],
                     created_time: Option[String],
                     currency: Option[String],
                     disable_reason: Option[Long],
                     end_advertiser: Option[String],
                     end_advertiser_name: Option[String],
                     funding_source: Option[String],
                     has_migrated_permissions: Option[Boolean],
                     io_number: Option[String],
                     is_notifications_enabled: Option[Boolean],
                     is_personal: Option[Int],
                     is_prepay_account: Option[Boolean],
                     is_tax_id_required: Option[Boolean],
                     last_used_time: Option[String],
                     line_numbers: Option[List[Int]],
                     media_agency: Option[String],
                     min_campaign_group_spend_cap: Option[String],
                     min_daily_budget: Option[Int],
                     name: Option[String],
                     offsite_pixels_tos_accepted: Option[Boolean],
                     owner: Option[String],
                     partner: Option[String],
                     spend_cap: Option[String],
                     tax_id: Option[String],
                     tax_id_status: Option[Int],
                     tax_id_type: Option[String],
                     timezone_id: Option[Int],
                     timezone_name: Option[String],
                     timezone_offset_hours_utc: Option[Float],
                     tos_accepted: Option[Map[String, Int]],
                     user_role: Option[String],
                     vertical_name: Option[String]
                    ) extends APINode[AdAccount] {

  override val endpoint: String = AdAccount.END_POINT

  override def allFields: Seq[String] = AdAccount.ALL_FIELDS

  override def defaultReadFields: Seq[String] = AdAccount.DEFAULT_READ_FIELDS

  def getCampaigns(params: Map[String, Any] = Map())(implicit ec: ExecutionContext): Future[Cursor[Campaign]] = {
    import Campaign._
    fetchConnections(Campaign.END_POINT, Campaign.ALL_FIELDS, params)
  }
}

object AdAccount {
  import enumeratum._

  sealed trait Fields extends EnumEntry

  object Fields extends Enum[Fields] {
    val values = findValues

    case object account_groups extends Fields
    case object account_id extends Fields
    case object account_status extends Fields
    case object age extends Fields
    case object agency_client_declaration extends Fields
    case object amount_spent extends Fields
    case object balance extends Fields
    case object business extends Fields
    case object business_city extends Fields
    case object business_country_code extends Fields
    case object business_id extends Fields
    case object business_name extends Fields
    case object business_state extends Fields
    case object business_street2 extends Fields
    case object business_street extends Fields
    case object business_zip extends Fields
    case object created_time extends Fields
    case object end_advertiser extends Fields
    case object media_agency extends Fields
    case object partner extends Fields
    case object capabilities extends Fields
    case object currency extends Fields
    case object id extends Fields
    case object is_personal extends Fields
    case object name extends Fields
    case object offsite_pixels_tos_accepted extends Fields
    case object spend_cap extends Fields
    case object spend_cap_action extends Fields
    case object funding_source extends Fields
    case object funding_source_details extends Fields
    case object timezone_id extends Fields
    case object timezone_name extends Fields
    case object timezone_offset_hours_utc extends Fields
    case object tos_accepted extends Fields
    case object users extends Fields
    case object tax_id_status extends Fields
    case object adlabels extends Fields
    case object min_daily_budget extends Fields
    case object min_campaign_group_spend_cap extends Fields
  }

  val END_POINT = "adaccounts"

  val ALL_FIELDS = Fields.values.map(v => v.entryName)
  val DEFAULT_READ_FIELDS = Seq(Fields.account_id.entryName, Fields.account_status.entryName)

  implicit val AdAccountFormat: Format[AdAccount] = Jsonx.formatCaseClass[AdAccount]
}
