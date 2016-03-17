package com.fourseasapp.facebookads.model

import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format

/**
  * Created by hailegia on 3/14/2016.
  */
case class AdAccountGroup(id: String)

object AdAccountGroup {
  import enumeratum._

  sealed trait Fields extends EnumEntry

  object Fields extends Enum[Fields] with PlayJsonEnum[Fields] {
    val values = findValues

    case object id extends Fields
    case object account_group_id extends Fields
    case object name extends Fields
    case object status extends Fields
    case object users extends Fields
    case object accounts extends Fields
    case object currency extends Fields
  }

  val END_POINT = "adaccountgroups"

  val ALL_FIELDS = Fields.values.map(v => v.entryName)

  implicit val AdAccountGroupFormat: Format[AdAccountGroup] = Jsonx.formatCaseClass[AdAccountGroup]
}
