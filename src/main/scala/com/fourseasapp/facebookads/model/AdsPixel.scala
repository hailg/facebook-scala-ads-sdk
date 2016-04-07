package com.fourseasapp.facebookads.model

import java.io.File

import com.fourseasapp.facebookads.Mappable
import com.fourseasapp.facebookads.network._
import enumeratum.{EnumEntry, PlayJsonEnum}
import play.api.libs.json.Format

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.runtime.universe._

/**
  * Created by hailegia on 4/4/2016.
  */
case class AdsPixel(id: String,
                    code: Option[String] = None,
                    name: Option[String] = None,
                    last_fired_time: Option[String] = None,
                    creation_time: Option[String] = None)
  extends APINode[AdsPixel]
    with CannotUpdate[AdsPixel]
    with CannotDelete[AdsPixel] {

  type Fields = AdsPixel.Fields

  override def companion: APINodeCompanion[AdsPixel] = AdsPixel

  override def delete(batchAPIRequest: BatchAPIRequest = null, params: Map[String, Any] = Map())
                     (implicit format: Format[AdsPixel], typeTag: TypeTag[AdsPixel], ec: ExecutionContext): Future[Option[Boolean]] = super[CannotDelete].delete(batchAPIRequest, params)

  override def update(batchAPIRequest: BatchAPIRequest = null, params: Map[String, Any] = Map(), files: Map[String, File] = Map(), validating: Boolean = false)
                     (implicit format: Format[AdsPixel], typeTag: TypeTag[AdsPixel], m: Mappable[AdsPixel], ec: ExecutionContext): Future[Option[AdsPixel]] = super[CannotUpdate].update(batchAPIRequest, params, files, validating)
}

object AdsPixel extends APINodeCompanion[AdsPixel] {
  import enumeratum._
  import org.cvogt.play.json.Jsonx
  import play.api.libs.json.Format

  sealed trait Fields extends EnumEntry

  object Fields extends Enum[Fields] with PlayJsonEnum[Fields] {
    val values = findValues

    case object id extends Fields
    case object code extends Fields
    case object name extends Fields
    case object last_fired_time extends Fields
    case object creation_time extends Fields

  }
  override implicit val format: Format[AdsPixel] = Jsonx.formatCaseClass[AdsPixel]
}
