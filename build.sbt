import FS._

val AkkaVersion = "2.6.9"
val seleniumVersion = "3.141.59"
val AkkaHttpVersion = "10.2.0"
val log4jVersion = "2.13.3"

libraryDependencies ++= Seq(
  "org.seleniumhq.selenium" % "selenium-java" % seleniumVersion,
  "org.seleniumhq.selenium" % "selenium-chrome-driver" % seleniumVersion,
  "org.seleniumhq.selenium" % "selenium-firefox-driver" % seleniumVersion,

  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
  "com.typesafe.play" %% "play-json" % "2.8.1",

  "org.apache.logging.log4j" % "log4j-api" % log4jVersion,
  "org.apache.logging.log4j" % "log4j-core" % log4jVersion,
  "org.apache.logging.log4j" % "log4j-slf4j-impl" % log4jVersion, //route logs of slf4j to log4j

  "org.scalatest" %% "scalatest" % "3.0.8" % Test
)

lazy val root = (project in file("."))
  .settings(
    name := "fastscraper",
    scalaVersion := "2.13.3",
    version := projectVersion,
    mainClass := Some("com.fastscraping.AppBootstraper"),
    assemblyJarName in assembly := s"fastscraper-$projectVersion.jar"
  )

assemblyMergeStrategy in assembly := {
  case "module-info.class" => MergeStrategy.discard
  case x => (assemblyMergeStrategy in assembly).value(x)
}

(assembly in Compile) := {
  val jar = (assembly in Compile).value

  jar
}
