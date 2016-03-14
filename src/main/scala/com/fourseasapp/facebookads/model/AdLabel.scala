package com.fourseasapp.facebookads.model

import com.fourseasapp.facebookads.network.APINode
import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format

/**
  * Created by hailegia on 3/14/2016.
  */
case class AdLabel(id: String, account: Option[String], name: Option[String],
                   created_time: Option[String], updated_time: Option[String]) extends APINode[AdLabel] {

  override def endpoint: String = AdLabel.END_POINT

  override def defaultReadFields: Seq[String] = AdLabel.DEFAULT_READ_FIELDS

  override def allFields: Seq[String] = AdLabel.ALL_FIELDS
}

object AdLabel {
  import enumeratum._

  sealed trait Fields extends EnumEntry

  object Fields extends Enum[Fields] {
    val values = findValues

    case object id extends Fields
    case object account extends Fields
    case object name extends Fields
    case object created_time extends Fields
    case object updated_time extends Fields
  }

  val END_POINT = "adlabels"

  val ALL_FIELDS = Fields.values.map(v => v.entryName)
  val DEFAULT_READ_FIELDS = ALL_FIELDS

  implicit val AdLabelFormat: Format[AdLabel] = Jsonx.formatCaseClass[AdLabel]
}