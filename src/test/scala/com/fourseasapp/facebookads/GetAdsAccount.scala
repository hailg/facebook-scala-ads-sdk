package com.fourseasapp.facebookads

/**
  * Created by hailegia on 3/11/2016.
  */

import com.fourseasapp.facebookads.model.AdUser
import com.google.inject.Guice

object GetAdsAccount extends App {
  val ACCESS_TOKEN = ""
  val APP_SECRET = ""

  val injector = Guice.createInjector(new FBAdsModule)
  implicit val ec = APIContext.apiEC

  val apiContext = APIContext(ACCESS_TOKEN, APP_SECRET, isDebug = true)

  val user = AdUser()(apiContext)
  injector.injectMembers(user)

  user.getAdAccounts().map {
    data => {
      println(data)
    }
  }
}
