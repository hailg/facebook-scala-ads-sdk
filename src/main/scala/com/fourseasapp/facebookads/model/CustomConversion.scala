package com.fourseasapp.facebookads.model

import com.fourseasapp.facebookads.network.{APINode, APINodeCompanion, BatchAPIRequest, CannotDelete}
import play.api.libs.json.Format
import scala.reflect.runtime.universe._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by hailegia on 4/7/2016.
  */
case class CustomConversion(id: String,
                            account_id: Option[String] = None,
                            custom_event_type: Option[String] = None,
                            creation_time: Option[String] = None,
                            default_conversion_value: Option[Long] = None,
                            description: Option[String] = None,
                            first_fired_time: Option[String] = None,
                            is_archived: Option[Boolean] = None,
                            last_fired_time: Option[String] = None,
                            name: Option[String] = None,
                            pixel_id: Option[String] = None,
                            pixel_rule: Option[String] = None)
  extends APINode[CustomConversion]
  with CannotDelete[CustomConversion] {

  type Fields = CustomConversion.Fields

  override def companion: APINodeCompanion[CustomConversion] = CustomConversion

  override def delete(batchAPIRequest: BatchAPIRequest = null, params: Map[String, Any] = Map())
                     (implicit format: Format[CustomConversion], typeTag: TypeTag[CustomConversion], ec: ExecutionContext): Future[Option[Boolean]] = super[CannotDelete].delete(batchAPIRequest, params)
}

object CustomConversion extends APINodeCompanion[CustomConversion] {
  import enumeratum._
  import org.cvogt.play.json.Jsonx
  import play.api.libs.json.Format

  sealed trait Fields extends EnumEntry

  object Fields extends Enum[Fields] with PlayJsonEnum[Fields] {
    val values = findValues

    case object id extends Fields
    case object account_id extends Fields
    case object custom_event_type extends Fields
    case object creation_time extends Fields
    case object default_conversion_value extends Fields
    case object description extends Fields
    case object first_fired_time extends Fields
    case object is_archived extends Fields
    case object last_fired_time extends Fields
    case object name extends Fields
    case object pixel_id extends Fields
    case object pixel_rule extends Fields
  }

  override implicit val format: Format[CustomConversion] = Jsonx.formatCaseClass[CustomConversion]
}