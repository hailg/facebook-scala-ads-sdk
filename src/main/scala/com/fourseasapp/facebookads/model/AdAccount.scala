package com.fourseasapp.facebookads.model

import com.fourseasapp.facebookads.network.APINode
import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format

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

  override val parentId: String = null

  override val endpoint: String = AdAccount.END_POINT

  override def allFields: List[String] = AdAccount.ALL_FIELDS

  override def defaultReadFields: List[String] = AdAccount.DEFAULT_READ_FIELDS
}

object AdAccount {
  object Fields {
    val ACCOUNT_GROUPS = "account_groups"
    val ACCOUNT_ID = "account_id"
    val ACCOUNT_STATUS = "account_status"
    val AGE = "age"
    val AGENCY_CLIENT_DECLARATION = "agency_client_declaration"
    val AMOUNT_SPENT = "amount_spent"
    val BALANCE = "balance"
    val BUSINESS = "business"
    val BUSINESS_CITY = "business_city"
    val BUSINESS_COUNTRY_CODE = "business_country_code"
    val BUSINESS_ID = "business_id"
    val BUSINESS_NAME = "business_name"
    val BUSINESS_STATE = "business_state"
    val BUSINESS_STREET2 = "business_street2"
    val BUSINESS_STREET = "business_street"
    val BUSINESS_ZIP = "business_zip"
    val CREATED_TIME = "created_time"
    val END_ADVERTISER = "end_advertiser"
    val MEDIA_AGENCY = "media_agency"
    val PARTNER = "partner"
    val CAPABILITIES = "capabilities"
    val CURRENCY = "currency"
    val ID = "id"
    val IS_PERSONAL = "is_personal"
    val NAME = "name"
    val OFFSITE_PIXELS_TOS_ACCEPTED = "offsite_pixels_tos_accepted"
    val SPEND_CAP = "spend_cap"
    val SPEND_CAP_ACTION = "spend_cap_action"
    val FUNDING_SOURCE = "funding_source"
    val FUNDING_SOURCE_DETAILS = "funding_source_details"
    val TIMEZONE_ID = "timezone_id"
    val TIMEZONE_NAME = "timezone_name"
    val TIMEZONE_OFFSET_HOURS_UTC = "timezone_offset_hours_utc"
    val TOS_ACCEPTED = "tos_accepted"
    val USERS = "users"
    val TAX_ID_STATUS = "tax_id_status"
    val ADLABELS = "adlabels"
    val MIN_DAILY_BUDGET = "min_daily_budget"
    val MIN_CAMPAIGN_GROUP_SPEND_CAP = "min_campaign_group_spend_cap"
  }

  val END_POINT = "adaccounts"
  val ALL_FIELDS = List(Fields.ACCOUNT_GROUPS, Fields.ACCOUNT_ID, Fields.ACCOUNT_STATUS, Fields.AGE, Fields.AGENCY_CLIENT_DECLARATION, Fields.AMOUNT_SPENT, Fields.BALANCE, Fields.BUSINESS, Fields.BUSINESS_CITY, Fields.BUSINESS_COUNTRY_CODE, Fields.BUSINESS_ID, Fields.BUSINESS_NAME, Fields.BUSINESS_STATE, Fields.BUSINESS_STREET2, Fields.BUSINESS_STREET, Fields.BUSINESS_ZIP, Fields.CREATED_TIME, Fields.END_ADVERTISER, Fields.MEDIA_AGENCY, Fields.PARTNER, Fields.CAPABILITIES, Fields.CURRENCY, Fields.ID, Fields.IS_PERSONAL, Fields.NAME, Fields.OFFSITE_PIXELS_TOS_ACCEPTED, Fields.SPEND_CAP, Fields.SPEND_CAP_ACTION, Fields.FUNDING_SOURCE, Fields.FUNDING_SOURCE_DETAILS, Fields.TIMEZONE_ID, Fields.TIMEZONE_NAME, Fields.TIMEZONE_OFFSET_HOURS_UTC, Fields.TOS_ACCEPTED, Fields.USERS, Fields.TAX_ID_STATUS, Fields.ADLABELS, Fields.MIN_DAILY_BUDGET, Fields.MIN_CAMPAIGN_GROUP_SPEND_CAP)
  val DEFAULT_READ_FIELDS = List("id", Fields.ACCOUNT_ID, Fields.ACCOUNT_STATUS)

  implicit val adAccountFormat: Format[AdAccount] = Jsonx.formatCaseClass[AdAccount]
}
