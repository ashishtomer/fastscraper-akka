val AkkaVersion = "2.6.9"
val seleniumVersion = "3.141.59"
val AkkaHttpVersion = "10.2.0"

libraryDependencies ++= Seq(
  "org.seleniumhq.selenium" % "selenium-java" % seleniumVersion,
  //  "org.seleniumhq.selenium" % "selenium-chrome-driver" % seleniumVersion

  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion
)

lazy val root = (project in file("."))
  .settings(
    name := "fastscraper",
    scalaVersion := "2.13.3",
    version := "0.1.0"
  )
