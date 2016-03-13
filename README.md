# facebook-scala-ads-sdk

##### IMPORTANT: This SDK is not Facebook's Official SDK. However, I used a lot of ideas from their Java and PHP SDK. This project still in devlopment, so please help contribute if you like it.

### Introduction 

This SDK helps you to use Facebook Marketing API easier (without the need to understand Graph API). It's a scala library that you can add to your Scala project as normal. The most important reasons that lead me to create this project are:

* I love Scala and don't want to adopt Facebook Ads Java SDK.
* Facebook Java Ads SDK doesn't support Asynchronous yet.
* I think Facebook Java Ads SDK is not as nice as their PHP SDK (a lot of Json parser code).

In this project, I used play-ws and their Json library but you can still use it in non-play projects. The reason I choose to do this is because Play provides very nice Http client interface for Scala. Also it's very fast to define Json objects using `case` class. 

### Usage
At this stage of development, there's nothing much to explore, you can take a look at `GetAdsAccount.scala` for the idea.