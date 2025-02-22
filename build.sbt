name := "Game Server"

version := "1.0"

scalaVersion := "2.13.6" // Fixed - https://docs.codescreen.com/#/creating-custom-scala-assessments

libraryDependencies ++= Seq(

  "org.typelevel" %% "cats-effect" % "3.4.1",
  "org.typelevel" %% "cats-effect-testing-scalatest" % "1.4.0" % Test,
  "org.scalatest" %% "scalatest" % "3.2.14" % Test,
)
