package com.fourseasapp.facebookads

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.fourseasapp.facebookads.network.{APIRequest, APIRequestFactory, BatchAPIRequest}
import com.google.inject.assistedinject.FactoryModuleBuilder
import com.google.inject.{AbstractModule, Inject, Provider}
import com.typesafe.config.ConfigFactory
import net.codingwell.scalaguice.ScalaModule
import org.asynchttpclient.AsyncHttpClientConfig
import play.api.libs.ws.ahc.{AhcConfigBuilder, AhcWSClient, AhcWSClientConfig}
import play.api.libs.ws.{WSClient, WSConfigParser}
import play.api.{Configuration, Environment, Mode}

/**
  * Created by hailegia on 3/11/2016.
  */
class FBAdsPlayModule extends AbstractModule with ScalaModule {
  override def configure(): Unit = {
    install(new FactoryModuleBuilder()
      .implement(classOf[APIRequest], classOf[APIRequest])
      .implement(classOf[BatchAPIRequest], classOf[BatchAPIRequest])
      .build(classOf[APIRequestFactory]))
  }
}