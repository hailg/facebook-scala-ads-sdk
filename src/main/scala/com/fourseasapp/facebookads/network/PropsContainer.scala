package com.fourseasapp.facebookads.network

import scala.collection.mutable.ListBuffer

/**
  * Created by hailegia on 3/19/2016.
  */
class PropsContainer(props: Map[String, Any]) {
  private[network] var data: Map[String, Any] = props
  private var changesFields = ListBuffer[String]()

  def set(key: String, value: Any) {
    if (!data.contains(key) || data(key) != value) {
      changesFields += key
      if (value.isInstanceOf[APINode[_]]) {
        val propNode = value.asInstanceOf[APINode[_]]
        data += (key -> propNode.props)
      } else {
        data += (key -> value)
      }
    }
  }

  def del(key: String): Unit = {
    changesFields -= key
    data -= key
  }

  def clearHistory(): Unit = {
    changesFields.clear()
  }

  def setServerData(newData: Map[String, Any]): Unit = {
    newData.foreach(entry => {
      data += entry
      changesFields -= entry._1
    })
  }

  def exportAllData: Map[String, Any] = {
    var result: Map[String, Any] = Map()
    data.foreach(entry => {
      if (entry._2 != null) {
        if (entry._2.isInstanceOf[PropsContainer]) {
          result += entry._1 -> entry._2.asInstanceOf[PropsContainer].exportAllData
        } else if (entry._2.isInstanceOf[Option[_]]) {
          val optionValue = entry._2.asInstanceOf[Option[_]]
          if (optionValue.isDefined) {
            result += entry._1 -> optionValue.get
          }
        } else {
          result += entry._1 -> entry._2
        }
      }
    })
    result
  }

  def exportData: Map[String, Any] = {
    var result: Map[String, Any] = Map()
    changesFields.foreach(field => {
      val newData = data(field)
      if (newData != null) {
        if (newData.isInstanceOf[PropsContainer]) {
          result += field -> (newData.asInstanceOf[PropsContainer].exportData)
        } else if (newData.isInstanceOf[Option[_]]) {
          val optionValue = newData.asInstanceOf[Option[_]]
          if (optionValue.isDefined) {
            result += field -> optionValue.get
          }
        }
      }
    })
    result
  }
}
