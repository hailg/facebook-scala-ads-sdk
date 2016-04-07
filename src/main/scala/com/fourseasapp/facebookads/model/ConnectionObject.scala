package com.fourseasapp.facebookads.model

import com.fourseasapp.facebookads.network.{APINode, APINodeCompanion}

/**
  * Created by hailegia on 3/28/2016.
  */
case class ConnectionObject(id: String,
                            app_installs_tracked: Option[Boolean] = None,
                            is_game: Option[Boolean] = None,
                            name: Option[String] = None,
                            name_with_location_descriptor: Option[String] = None,
                            native_app_targeting_ids: Option[Map[String, String]] = None,
                            object_store_urls: Option[Map[String, String]] = None,
                            picture: Option[String] = None,
                            supported_platforms: Option[Seq[Int]] = None,
                            tabs: Option[Map[String, String]] = None,
                            `type`: Option[Int] = None,
                            url: Option[String] = None) extends APINode[ConnectionObject] {

  type Fields = ConnectionObject.Fields

  override def companion = ConnectionObject

}

object ConnectionObject extends APINodeCompanion[ConnectionObject] {
  import enumeratum._
  import org.cvogt.play.json.Jsonx
  import play.api.libs.json.Format

  val APPLICATION = 2
  val DOMAIN = 7
  val EVENT = 3
  val PAGE = 6
  val PLACE = 1


  sealed trait Fields extends EnumEntry

  object Fields extends Enum[Fields] with PlayJsonEnum[Fields] {
    val values = findValues

    case object id extends Fields
    case object app_installs_tracked extends Fields
    case object is_game extends Fields
    case object name extends Fields
    case object name_with_location_descriptor extends Fields
    case object native_app_store_ids extends Fields
    case object native_app_targeting_ids extends Fields
    case object object_store_urls extends Fields
    case object picture extends Fields
    case object supported_platforms extends Fields
    case object tabs extends Fields
    case object `type` extends Fields
    case object url extends Fields
  }

  override implicit val format: Format[ConnectionObject] = Jsonx.formatCaseClass[ConnectionObject]
}
