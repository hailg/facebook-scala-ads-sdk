package com.fourseasapp.facebookads.model

import com.fourseasapp.facebookads.network.{APINode, APINodeCompanion}
import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format

/**
  * Created by hailegia on 3/14/2016.
  */
case class AdLabel(id: String, account: Option[String], name: Option[String],
                   created_time: Option[String], updated_time: Option[String]) extends APINode[AdLabel] {
  type Fields = AdLabel.Fields

  override def companion = AdLabel
}

object AdLabel extends APINodeCompanion[AdLabel] {
  import enumeratum._
  import org.cvogt.play.json.Jsonx
  import play.api.libs.json.Format

  sealed trait Fields extends EnumEntry

  object Fields extends Enum[Fields] with PlayJsonEnum[Fields] {
    val values = findValues

    case object updated_time extends Fields
    case object created_time extends Fields
    case object name extends Fields
    case object account extends Fields
    case object id extends Fields

  }

  override implicit val format: Format[AdLabel] = Jsonx.formatCaseClass[AdLabel]
}