package com.fourseasapp.facebookads

import _root_.net.codingwell.scalaguice.ScalaModule
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.fourseasapp.facebookads.network.{BatchAPIRequest, APIRequestFactory, APIRequest}
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
class FBAdsModule extends AbstractModule with ScalaModule {
  override def configure(): Unit = {
    install(new FactoryModuleBuilder()
      .implement(classOf[APIRequest], classOf[APIRequest])
      .implement(classOf[BatchAPIRequest], classOf[BatchAPIRequest])
      .build(classOf[APIRequestFactory]))

    bind[ActorSystem].toInstance(ActorSystem(classOf[FBAdsModule].getSimpleName))
    bind[WSClient].toProvider[WSClientProvider].in[javax.inject.Singleton]
  }
}

class WSClientProvider @Inject() (system: ActorSystem) extends Provider[WSClient] {
  override def get(): WSClient = {
    val materializer = ActorMaterializer(namePrefix = Some(classOf[FBAdsModule].getSimpleName))(system)

    val configuration = Configuration.reference ++ Configuration(ConfigFactory.parseString(
      """
        |ws.followRedirects = true
      """.stripMargin))

    val parser = new WSConfigParser(configuration, Environment.simple(mode = Mode.Prod))
    val config = new AhcWSClientConfig(wsClientConfig = parser.parse())
    val builder = new AhcConfigBuilder(config)
    val logging = new AsyncHttpClientConfig.AdditionalChannelInitializer() {
      override def initChannel(channel: io.netty.channel.Channel): Unit = {
        channel.pipeline.addFirst("log", new io.netty.handler.logging.LoggingHandler("debug"))
      }
    }
    val ahcBuilder = builder.configure()
    ahcBuilder.setHttpAdditionalChannelInitializer(logging)
    val ahcConfig = ahcBuilder.build()
    return new AhcWSClient(ahcConfig)(materializer)
  }
}