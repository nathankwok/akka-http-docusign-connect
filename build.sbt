enablePlugins(JavaAppPackaging)

name := "akka-http-docusign-connect"
version := "1.0"
scalaVersion := "2.11.8"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV       = "2.4.9"
  val scalaTestV  = "2.2.6"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-stream" % akkaV,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaV,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaV,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaV,
    "org.scalatest"     %% "scalatest" % scalaTestV % "test",
    "com.typesafe.akka" %% "akka-http-xml-experimental" % akkaV

//    "com.greencatsoft" %% "scalajs-angular" % "0.7",
//    "com.beachape" % "enumeratum" % "1.4.9",
//    "com.beachape" % "enumeratum-circe" % "1.4.9",
//    "io.circe" % "circe-core" % "0.4.1",
//    "io.circe" % "circe-generic" % "0.4.1",
//    "io.circe" % "circe-parser" % "0.4.1",
//    "com.lihaoyi" % "scalatags" % "0.6.0"
  )
}

resolvers ++= Seq(
  "Sonatype repo" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Typesafe Snapshots" at "https://repo.typesafe.com/typesafe/snapshots/",
  "Spray repo" at "http://repo.spray.io",
  "Jasper Reports" at "http://jasperreports.sourceforge.net/maven2/",
  "mmreleases" at "https://artifactory.mediamath.com/artifactory/libs-release-global",
  "krasserm at bintray" at "http://dl.bintray.com/krasserm/maven",
  Resolver.bintrayRepo("hseeberger", "maven"),
  Resolver.bintrayRepo("websudos", "oss-releases")
)

Revolver.settings
