lazy val settings = Seq(
  name := "toolkit"
  , organization := "de.ioswarm"
  , version := "0.1.0"
  , scalaVersion := "2.12.4"
  , scalacOptions := Seq(
    "-language:_"
    , "-unchecked"
    , "-deprecation"
    , "-encoding", "UTF-8"
    , "target:jvm-1.8"
  )
)

lazy val toolkit = project.in(file("."))
  .settings(settings)
  .settings(
    name := "toolkit"
  )
  .aggregate(
    time
    , schedule
    , mail
    , poi
  )

lazy val time = project.in(file("time"))
  .settings(settings)
  .settings(
    name := "toolkit-time"
  )

lazy val schedule = project.in(file("schedule"))
  .settings(settings)
  .settings(
    name := "toolkit-schedule"
  )
  .dependsOn(
    time
  )

lazy val mail = project.in(file("mail"))
  .settings(settings)
  .settings(
    name := "toolkit-mail"
    , libraryDependencies ++= Seq(
      lib.config
      , lib.commonsMail
    )
  )

lazy val poi = project.in(file("poi"))
  .settings(settings)
  .settings(
    name := "toolkit-poi"
    , libraryDependencies ++= Seq(
      lib.poi
      , lib.poiOOXML
    )
  )


lazy val lib = new {
  object Version {
    val config = "1.3.1"

    val commonsMail = "1.5"
    val commonsNet = "3.6"
    val poi = "3.17"
  }

  val config = "com.typesafe" % "config" % Version.config

  val commonsMail = "org.apache.commons" % "commons-email" % Version.commonsMail
  val commonsNet = "commons-net" % "commons-net" % Version.commonsNet

  val poi = "org.apache.poi" % "poi" % Version.poi
  val poiOOXML = "org.apache.poi" % "poi-ooxml" % Version.poi

}
