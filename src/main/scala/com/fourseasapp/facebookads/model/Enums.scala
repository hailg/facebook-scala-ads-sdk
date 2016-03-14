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

object Enums {
//  implicit val EnumConfiguredStatusFormat: Format[EnumConfiguredStatus] = Jsonx.formatAuto[EnumConfiguredStatus]
//  implicit val EnumCampaignEffectiveStatusFormat: Format[EnumCampaignEffectiveStatus] = Jsonx.formatAuto[EnumCampaignEffectiveStatus]
}