package com.fourseasapp.facebookads

/**
  * Created by hailegia on 3/11/2016.
  */

import com.fourseasapp.facebookads.model.AdUser
import com.google.inject.Guice

import scala.concurrent.Future

object SimpleSamples extends App {
  val ACCESS_TOKEN = ""
  val APP_SECRET = ""

  val injector = Guice.createInjector(new FBAdsModule)
  implicit val ec = APIContext.apiEC

  val apiContext = APIContext(ACCESS_TOKEN, APP_SECRET, isDebug = true)

  val user = AdUser()(apiContext)
  //Inject ApiRequestFactory to APINode. We need to do this only once, at the root of the object graph (e.g., AdUser). Other
  // APINode loaded by this node (directly, indirectly will have required properties automatically).
  injector.injectMembers(user)

  val accountsFuture = user.getAdAccounts() flatMap (_.fetchRemaining())
  accountsFuture map println _

  val campaignsFuture = accountsFuture flatMap {
    accounts => if (accounts.size > 0) {
      accounts(0).getCampaigns() flatMap(_.fetchRemaining())
    } else {
      Future(List())
    }
  }
  campaignsFuture map println _
}
