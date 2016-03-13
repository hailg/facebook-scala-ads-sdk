package com.fourseasapp.facebookads

/**
  * Created by hailegia on 3/11/2016.
  */

import com.fourseasapp.facebookads.model.{AdAccount, AdUser}
import com.fourseasapp.facebookads.network.APIRequestFactory
import com.google.inject.Guice
import net.codingwell.scalaguice.InjectorExtensions._
import play.api.libs.json._

object Test extends App {

  val injector = Guice.createInjector(new FBAdsModule)
  implicit val ec = APIContext.apiEC

  val apiContext = APIContext("CAAXwYOmSoHEBALzxPYagzIYF4RoUPRsRPTUFg3m0Jb69mQWg5JnewTP2XRRVlqfuue02mx6AB8giVzP56plCFEzWnf6dhAnkOQjOGaaHZCqeBZBoZBdRbY5ZAzxZAhuenEPGLHPCB4iIYZBOYh1o90UjjB10cV8bkMZB3GeJzFuK2a3yZAcyslMyP8gtCZCvB3alCfZBFq9p3ORAZDZD", "2d73cce33da8365788fb9938a204f286", isDebug = true)
  val user = AdUser()(apiContext)
  injector.injectMembers(user)


  user.getAdAccounts().map {
    data => {
      println(data)
      data.foreach(item => println(item.apiContext + ", "  + item.apiRequestFactory))
    }
  }

//  implicit val simpleObjectFormat = Json.format[SimpleObject]
//  parse[SimpleObject]("""{"name":"hai"}""")
//  parse[SimpleObject](Json.stringify(Json.toJson(SimpleObject("hai", 30))))

//  wsClient.url("http://google.com")
//    .get()
//    .map(response => {
//      println("RESPONSE: " + response.body)
//    })

  def parse[T](s: String)(implicit format: Format[T]) = {
    println(s)
    val result = Json.parse(s).validate[T].fold(
      invalid => (Left(Json.parse(s).as[JsValue]), Some(invalid)),
      obj => (Right(obj), None)
    )
    println(result)
    result
  }
}
