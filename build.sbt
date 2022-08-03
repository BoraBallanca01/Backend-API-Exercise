ThisBuild / scalaVersion := "2.13.8"

ThisBuild / version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayJava)
  .settings(
    name := """backend-api-exercise""",
    libraryDependencies ++= Seq(
      guice,
      "com.auth0" % "java-jwt" % "3.3.0",
      "org.mongodb" % "mongodb-driver-sync" % "4.3.0",
      "org.projectlombok" % "lombok" % "1.18.12",
      "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "2.2.0",
      "org.mindrot" % "jbcrypt" % "0.3m",
      "org.hibernate" % "hibernate-validator" % "6.1.5.Final",
      "org.glassfish" % "javax.el" % "3.0.0"

)
)
