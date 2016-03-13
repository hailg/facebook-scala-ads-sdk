name := "facebook-ads-sdk"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-ws" % "2.5.0",
  "org.cvogt" %% "play-json-extensions" % "0.6.0",

  "org.slf4j" % "slf4j-api" % "1.7.18",
  "ch.qos.logback" % "logback-classic" % "1.1.6",
  "ch.qos.logback" % "logback-core" % "1.1.6",

  "net.codingwell" %% "scala-guice" % "4.0.1",
  "com.google.inject.extensions" % "guice-assistedinject" % "4.0"

)
    