import FS._
import com.typesafe.sbt.packager.docker.{Cmd, ExecCmd}

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

  "ch.qos.logback" % "logback-core" % "1.2.3",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.slf4j" % "slf4j-api" % "1.7.30",

  "org.mongodb" % "mongodb-driver-sync" % "4.2.0",

  "org.scalatest" %% "scalatest" % "3.0.8" % Test
)

lazy val root = (project in file("."))
  .enablePlugins(UniversalPlugin, DockerPlugin, JavaAppPackaging)
  .settings(
    name := "fastscraper",
    scalaVersion := "2.13.3",
    version := projectVersion,
    (mappings in Universal) ++= Seq(
      file("/opt/fastscraper/geckodriver") -> "geckodriver",
      file("/opt/fastscraper/chromedriver") -> "chromedriver"
    ),
    (defaultLinuxInstallLocation in  Docker) := "/opt/fastscraper",
    dockerExposedPorts := Seq(8082, 443, 80),
    dockerEnvVars := Map("PATH" -> "/opt/fastscraper:${PATH}"),
    dockerBaseImage := "openjdk:8-jre-alpine",
    dockerCommands ++= Seq(
      Cmd("USER", "root"),
      ExecCmd("RUN", "apk", "update"),
      ExecCmd("RUN", "apk", "add", "bash"),
      ExecCmd("RUN", "apk", "add", "curl"),
      ExecCmd("RUN", "echo", "http://dl-cdn.alpinelinux.org/alpine/latest-stable/community/x86_64/", ">>", "/etc/apk/repositories"),
      ExecCmd("RUN", "apk", "add", "firefox-esr"),
      ExecCmd("RUN", "chmod", "+x", "geckodriver"),
      ExecCmd("RUN", "apk", "+x", "chromedriver"),
      Cmd("USER", (daemonUser in Docker).value)
    )
  )
