package com.fourseasapp.facebookads

/**
  * Created by hailegia on 3/11/2016.
  */

import com.fourseasapp.facebookads.model.EnumCampaignEffectiveStatus.ADSET_PAUSED
import com.fourseasapp.facebookads.model.{AdAccount, AdUser}
import com.google.inject.Guice
import com.fourseasapp.facebookads.model._
import com.fourseasapp.facebookads.network.APIRequestFactory
import net.codingwell.scalaguice.InjectorExtensions._

import scala.concurrent.Future

object SimpleSamples extends App {
//  println(Util.generateAPINodeCompanion[AdLabel])

  val ACCESS_TOKEN = ""
  val APP_SECRET = "2d73cce33da8365788fb9938a204f286"

  val injector = Guice.createInjector(new FBAdsStandaloneModule)
  implicit val ec = APIContext.apiEC
  val apiContext = APIContext(ACCESS_TOKEN, APP_SECRET, isDebug = true)

  val user = AdUser()
  user.apiContext = apiContext
  //Inject ApiRequestFactory to APINode. We need to do this only once, at the root of the object graph (e.g., AdUser). Other
  // APINode loaded by this node (directly, indirectly will have required properties automatically).
  injector.injectMembers(user)

  def crudCampaign(): Unit = {
    val accountsFuture = user.getAdAccounts().fetchRemaining()

    val campaignsFuture = accountsFuture flatMap {
      accounts => if (accounts.size > 0) {
        accounts(0).getCampaigns().fetchRemaining()
      } else {
        Future(List())
      }
    }

    //Test created
    val createF = for {
      campaigns <- campaignsFuture if campaigns.size > 0
      newCampaign = campaigns(0).set(Campaign.Fields.id -> null,
        Campaign.Fields.name -> Some("Test campaign 2"))
      createdCampaign <- newCampaign.create()
    } yield createdCampaign

    createF map println _

    //Test update
    val updateF = for {
      campaignO <- createF if campaignO.isDefined
      campaign = campaignO.get
      result <- campaign.set(Campaign.Fields.name -> Some("Test Campaign 2 Updated")).update()
    } yield result

    updateF map println _

    //Test delete
    val deleteF = for {
      updateO <- updateF if updateO.isDefined
      campaign = updateO.get
      result <- campaign.delete()
    } yield result

    deleteF map println _
  }

  def createCampaignsInBatch(): Unit = {
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

          val newCampaign1 = campaign.set(Campaign.Fields.id -> null,
            Campaign.Fields.name -> Some("Test campaign 2"))
          newCampaign1.create(batchAPIRequest = batch)

          val newCampaign2 = campaign.set(Campaign.Fields.id -> null,
            Campaign.Fields.name -> Some("Test campaign 3"))
          newCampaign2.create(batchAPIRequest = batch)

          batch.execute[Campaign]() map {list =>
            list.foreach {
              case Left(x) => println(x)
              case Right(c) => println(c.toStringAll())
            }
          }
        } catch {
          case e: Exception => e.printStackTrace()
        }
      }))
  }

  def getAdPixels(): Unit = {
    val accountsFuture = user.getAdAccounts().fetchRemaining()
    accountsFuture.map {accounts =>
      accounts(0).getAdsPixels().fetchRemaining().map(println _)
    }
  }

  getAdPixels()
}
