package com.fourseasapp.facebookads.model

import com.fourseasapp.facebookads.network.{APINode, APINodeCompanion}

/**
  * Created by hailegia on 3/14/2016.
  */
case class Campaign(id: String, adlabels: Option[List[AdLabel]], account_id: Option[String], buying_type: Option[String],
                    can_use_spend_cap: Option[Boolean], configured_status: Option[EnumConfiguredStatus], created_time: Option[String],
                    effective_status: Option[EnumCampaignEffectiveStatus], name: Option[String], objective: Option[String],
                    start_time: Option[String], stop_time: Option[String], updated_time: Option[String], spend_cap: Option[String]) extends APINode[Campaign] {
  type Fields = Campaign.Fields

  override def companion = Campaign
}

object Campaign extends APINodeCompanion[Campaign] {
  import enumeratum._
  import org.cvogt.play.json.Jsonx
  import play.api.libs.json.Format

  sealed trait Fields extends EnumEntry

  object Fields extends Enum[Fields] with PlayJsonEnum[Fields] {
    val values = findValues

    case object spend_cap extends Fields
    case object updated_time extends Fields
    case object stop_time extends Fields
    case object start_time extends Fields
    case object objective extends Fields
    case object name extends Fields
    case object effective_status extends Fields
    case object created_time extends Fields
    case object configured_status extends Fields
    case object can_use_spend_cap extends Fields
    case object buying_type extends Fields
    case object account_id extends Fields
    case object adlabels extends Fields
    case object id extends Fields

  }

  override implicit val format: Format[Campaign] = Jsonx.formatCaseClass[Campaign]
}