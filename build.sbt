ThisBuild / scalaVersion := "2.13.8"

ThisBuild / version := "1.0-SNAPSHOT"

routesGenerator:=InjectedRoutesGenerator
//PlayKeys.devSettings+="config.resource"->""

scalaVersion:="2.12.9"
val akkaManagementVersion="1.0.0"
val akkaVersion="2.6.0"
val akkaHTTPVersion="10.1.10"
lazy val root = (project in file("."))
  .enablePlugins(PlayJava)
  .settings(
    name := """backend-api-exercise""",
    libraryDependencies ++= Seq(
      javaWs,
      guice,
      ehcache,
      javaJdbc,
      filters,
      "com.auth0" % "java-jwt" % "3.3.0",
      "org.mongodb" % "mongodb-driver-sync" % "4.3.0",
      "org.projectlombok" % "lombok" % "1.18.12",
      "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "2.2.0",
      "org.mindrot" % "jbcrypt" % "0.3m",
      "org.hibernate" % "hibernate-validator" % "6.1.5.Final",
      "org.glassfish" % "javax.el" % "3.0.0",


"org.hibernate" % "hibernate-validator" % "6.1.5.Final",
"org.glassfish" % "javax.el" % "3.0.0",
// akka related stuff
"com.typesafe.akka" %% "akka-actor" % akkaVersion,
"com.typesafe.akka" %% "akka-stream" % akkaVersion,
"com.typesafe.akka" %% "akka-cluster" % akkaVersion,
"com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion,
"com.typesafe.akka" %% "akka-persistence" % akkaVersion,
"com.typesafe.akka" %% "akka-distributed-data" % akkaVersion,
"com.typesafe.akka" %% "akka-discovery" % akkaVersion,
"com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
"com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
"com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,
// akka cluster related stuff
"com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % akkaManagementVersion,
"com.lightbend.akka.management" %% "akka-management" % akkaManagementVersion,
"com.lightbend.akka.management" %% "akka-management-cluster-http" % akkaManagementVersion,
"com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % akkaManagementVersion,
// akka htttp related stuff
"com.typesafe.akka" %% "akka-http-core" % akkaHTTPVersion,
"com.typesafe.akka" %% "akka-http2-support" % akkaHTTPVersion,
"com.typesafe.akka" %% "akka-http-spray-json" % akkaHTTPVersion,
"com.typesafe.akka" %% "akka-http" % akkaHTTPVersion,

)
)
