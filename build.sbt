name := """bolero-server"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, JavaServerAppPackaging)

scalaVersion := "2.11.6"

resolvers ++= Seq(
  "RoundEights" at "http://maven.spikemark.net/roundeights",
  Resolver.jcenterRepo,
  Resolver.bintrayRepo("scalaz", "releases")
)

libraryDependencies ++= Seq(
  jdbc,
  // anorm,
  cache,
  ws,
  // specs2 % Test,
  // "org.specs2" %% "specs2-core" % "3.6.5" % "test",
  "javax.inject" % "javax.inject" % "1",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.7.play24",
  // "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "1.50.0" % Test,
  "com.github.athieriot" %% "specs2-embedmongo" % "0.7.0",
  "org.scalikejdbc" %% "scalikejdbc" % "2.2.8",
  // "com.h2database" % "h2" % "1.4.189",
  "mysql" % "mysql-connector-java" % "5.1.36",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "Pingplusplus" % "pingpp-java" % "2.1.1",
  "com.roundeights" %% "hasher" % "1.2.0"
)

scalacOptions += "-feature"

scalacOptions ++= Seq("-Xmax-classfile-name", "100")

// https://github.com/playframework/playframework/issues/3017
scalacOptions ++= Seq("-encoding", "UTF-8")

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

// 生产环境配置
javaOptions in Universal ++= Seq(
  // "-Dconfig.resource=release.conf"

  // Since play uses separate pidfile we have to provide it with a proper path
  s"-Dpidfile.path=/var/run/${packageName.value}/play.pid",

  // Use separate configuration file for production environment
  s"-Dconfig.file=/usr/share/${packageName.value}/conf/application.conf",

  s"-Dhttp.port=9000"

  // Use separate logger configuration file for production environment
  // s"-Dlogger.file=/usr/share/${packageName.value}/conf/production-logger.xml",
)

maintainer in Linux := "Scott LIU <scozv@yandex.com>"

packageSummary in Linux := "RESTful API of Bolero Server"

packageDescription := ""
