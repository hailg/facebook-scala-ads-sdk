package com.fourseasapp.facebookads

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

import org.slf4j.{LoggerFactory, Logger}

/**
  * Created by hailegia on 3/12/2016.
  */

case class APIContext (accessToken: String,
                      appSecret: String = null,
                      endpointBase: String = "https://graph.facebook.com",
                      version: String = "v2.5",
                      isDebug: Boolean = false,
                       timeOut: Int = 30) {

  def hasAppSecret = appSecret != null

  def getAppSecretProof(): String = {
    sha256(appSecret, accessToken)
  }

  def log(s: String): Unit = {
    if (isDebug) {
      APIContext.logger.debug(s)
    }
  }

  def error(s: String, t: Throwable): Unit = {
    APIContext.logger.error(s, t)
  }

  private def sha256(secret: String, message: String): String = {
    val sha256HMAC = Mac.getInstance("HmacSHA256")
    val secretKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256")
    sha256HMAC.init(secretKey)
    val bytes = sha256HMAC.doFinal(message.getBytes())
    toHex(bytes)
  }

  private def toHex(bytes: Array[Byte]): String = {
    val sb = new StringBuilder()
    for (b <- bytes) {
      sb.append("%1$02x".format(b))
    }
    sb.toString()
  }
}

object APIContext {
  val USER_AGENT = "fourseasapp-scala-ads-api-sdk-v2.5"
  val logger = LoggerFactory.getLogger(classOf[APIContext])


  implicit val apiEC = scala.concurrent.ExecutionContext.global
}