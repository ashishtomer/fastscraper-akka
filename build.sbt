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
  "com.typesafe.play" %% "play-json" % "2.8.1",

  "org.apache.logging.log4j" % "log4j-api" % log4jVersion,
  "org.apache.logging.log4j" % "log4j-core" % log4jVersion,
  "org.apache.logging.log4j" % "log4j-slf4j-impl" % log4jVersion, //route logs of slf4j to log4j

  "org.mongodb.scala" %% "mongo-scala-driver" % "4.1.1",

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
