package com.fourseasapp.facebookads

/**
  * Created by hailegia on 3/11/2016.
  */

import com.fourseasapp.facebookads.model.{Campaign, AdUser}
import com.fourseasapp.facebookads.network.APINode
import com.google.inject.Guice

import scala.concurrent.Future
import scala.reflect.runtime.universe._

object SimpleSamples extends App {
  val ACCESS_TOKEN = "CAAXwYOmSoHEBACSpUYKyViaI44TsJBtRMlbR8WmMtXsbADYXJSEze6HMSPpISZAXXZCKBF6ZBVvZAEeWBvh9yaOYV8EuvOOSPCqAldj8I5DgAZCcWoKsd0za0VKLritbSwojthaGlukdXgDheCsAZCpyuAs0IoQK6AmlpUUB011WQVvqKL0JFZCWdxkfEByqqRckvFUFIJiBQZDZD"
  val APP_SECRET = "2d73cce33da8365788fb9938a204f286"

  val injector = Guice.createInjector(new FBAdsModule)
  implicit val ec = APIContext.apiEC

  val apiContext = APIContext(ACCESS_TOKEN, APP_SECRET, isDebug = true)

  val user = AdUser()
  user.apiContext = apiContext
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
