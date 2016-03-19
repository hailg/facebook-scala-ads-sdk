package com.fourseasapp.facebookads

import scala.reflect.runtime.universe._

/**
  * Created by hailegia on 3/13/2016.
  */
object Util {
  def generateAPINodeCompanion[T: TypeTag] = {
    def fields = typeOf[T].members.collect {
      case m: MethodSymbol if m.isCaseAccessor => m.name.toString
    }
    val className = typeOf[T].typeSymbol.name.toString
    val simpleName = className.substring(className.lastIndexOf(".") + 1)

    val caseObjectsBuilder = new StringBuilder

    fields.foreach(field => {
      caseObjectsBuilder.append(s"    case object ${field} extends Fields\n")
    })

    s"""
      |object $className extends APINodeCompanion[$className] {
      |  import enumeratum._
      |  import org.cvogt.play.json.Jsonx
      |  import play.api.libs.json.Format
      |
      |  sealed trait Fields extends EnumEntry
      |
      |  object Fields extends Enum[Fields] with PlayJsonEnum[Fields] {
      |    val values = findValues
      |
      |${caseObjectsBuilder.toString()}
      |  }
      |
      |  override implicit val format: Format[$className] = Jsonx.formatCaseClass[$className]
      |}
    """.stripMargin

  }
}

//object AdUser extends APINodeCompanion[AdUser] {
//  import enumeratum._
//
//  sealed trait Fields extends EnumEntry
//
//  object Fields extends Enum[Fields] with PlayJsonEnum[Fields] {
//    val values = findValues
//
//    case object id extends Fields
//    case object name extends Fields
//    case object permissions extends Fields
//    case object role extends Fields
//  }
//
//  override implicit val format: Format[AdUser] = Jsonx.formatCaseClass[AdUser]
//}