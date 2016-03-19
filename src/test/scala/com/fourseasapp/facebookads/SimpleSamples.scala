package com.fourseasapp.facebookads

/**
  * Created by hailegia on 3/11/2016.
  */

import com.fourseasapp.facebookads.model.{AdAccount, AdUser}
import com.google.inject.Guice
import com.fourseasapp.facebookads.model._
import com.fourseasapp.facebookads.network.APIRequestFactory
import net.codingwell.scalaguice.InjectorExtensions._

import scala.concurrent.Future

object SimpleSamples extends App {
//  println(Util.generateAPINodeCompanion[AdLabel])

  val ACCESS_TOKEN = ""
  val APP_SECRET = ""

  val injector = Guice.createInjector(new FBAdsModule)
  implicit val ec = APIContext.apiEC
  val apiContext = APIContext(ACCESS_TOKEN, APP_SECRET, isDebug = true)

  val user = AdUser()
  user.apiContext = apiContext
  //Inject ApiRequestFactory to APINode. We need to do this only once, at the root of the object graph (e.g., AdUser). Other
  // APINode loaded by this node (directly, indirectly will have required properties automatically).
  injector.injectMembers(user)
  val apiRequestFactory = injector.instance[APIRequestFactory]

  val accountsFuture = user.getAdAccounts().fetchRemaining()

  val campaignsFuture = accountsFuture flatMap {
    accounts => if (accounts.size > 0) {
      accounts(0).getCampaigns().fetchRemaining()
    } else {
      Future(List())
    }
  }

  accountsFuture map println _

  campaignsFuture.map(campaigns =>
    campaigns.headOption.foreach(campaign => {
      try {
        val batch = apiRequestFactory.createAPIBatchRequest(apiContext)

        val newCampaign1 = campaign.copy(Campaign.Fields.id -> null,
          Campaign.Fields.name -> Some("Test campaign 2"))
        newCampaign1.create(batchAPIRequest = batch)

        val newCampaign2 = campaign.copy(Campaign.Fields.id -> null,
          Campaign.Fields.name -> Some("Test campaign 3"))
        newCampaign2.create(batchAPIRequest = batch)

        batch.execute[Campaign]() map(println _)
      } catch {
        case e: Exception => e.printStackTrace()
      }


  }))
}
