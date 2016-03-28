package com.fourseasapp.facebookads.model

import enumeratum.EnumEntry
import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format

/**
  * Created by hailegia on 3/14/2016.
  */

import enumeratum._

sealed trait EnumConfiguredStatus extends EnumEntry

object EnumConfiguredStatus extends Enum[EnumConfiguredStatus] with PlayJsonEnum[EnumConfiguredStatus] {
  val values = findValues

  case object ACTIVE extends EnumConfiguredStatus
  case object PAUSED extends EnumConfiguredStatus
  case object DELETED extends EnumConfiguredStatus
  case object ARCHIVED extends EnumConfiguredStatus
}

sealed trait EnumCampaignEffectiveStatus extends EnumEntry

object EnumCampaignEffectiveStatus extends Enum[EnumCampaignEffectiveStatus] with PlayJsonEnum[EnumCampaignEffectiveStatus] {
  val values = findValues

  case object ACTIVE extends EnumCampaignEffectiveStatus
  case object PAUSED extends EnumCampaignEffectiveStatus
  case object DELETED extends EnumCampaignEffectiveStatus
  case object PENDING_REVIEW extends EnumCampaignEffectiveStatus
  case object DISAPPROVED extends EnumCampaignEffectiveStatus
  case object PREAPPROVED extends EnumCampaignEffectiveStatus
  case object PENDING_BILLING_INFO extends EnumCampaignEffectiveStatus
  case object CAMPAIGN_PAUSED extends EnumCampaignEffectiveStatus
  case object ARCHIVED extends EnumCampaignEffectiveStatus
  case object ADSET_PAUSED extends EnumCampaignEffectiveStatus
}

sealed trait EnumCampaignObjective extends EnumEntry

object EnumCampaignObjective extends Enum[EnumCampaignObjective] with PlayJsonEnum[EnumCampaignObjective] {
  val values = findValues

  case object BRAND_AWARENESS extends EnumCampaignObjective
  case object CANVAS_APP_ENGAGEMENT extends EnumCampaignObjective
  case object CANVAS_APP_INSTALLS extends EnumCampaignObjective
  case object EVENT_RESPONSES extends EnumCampaignObjective
  case object LEAD_GENERATION extends EnumCampaignObjective
  case object LOCAL_AWARENESS extends EnumCampaignObjective
  case object MOBILE_APP_ENGAGEMENT extends EnumCampaignObjective
  case object MOBILE_APP_INSTALLS extends EnumCampaignObjective
  case object NONE extends EnumCampaignObjective
  case object OFFER_CLAIMS extends EnumCampaignObjective
  case object PAGE_LIKES extends EnumCampaignObjective
  case object POST_ENGAGEMENT extends EnumCampaignObjective
  case object LINK_CLICKS extends EnumCampaignObjective
  case object CONVERSIONS extends EnumCampaignObjective
  case object VIDEO_VIEWS extends EnumCampaignObjective
  case object PRODUCT_CATALOG_SALES extends EnumCampaignObjective
}

sealed trait EnumBidInfo extends EnumEntry

object EnumBidInfo extends Enum[EnumBidInfo] with PlayJsonEnum[EnumBidInfo] {
  val values = findValues

  case object ACTIONS extends EnumBidInfo
  case object CLICKS extends EnumBidInfo
  case object IMPRESSIONS extends EnumBidInfo
  case object REACH extends EnumBidInfo
  case object SOCIAL extends EnumBidInfo
}