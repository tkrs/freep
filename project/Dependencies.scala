import sbt._

object Dependencies {
  val Ver = new {
    val `scala2.12` = "2.12.4"
    val `scala2.11` = "2.11.11"
    val scalafmt    = "1.2.0"
    val cats        = "1.0.0-RC1"
    val scalacheck  = "1.13.5"
    val scalatest   = "3.0.4"
    // val scalameta   = "2.1.2"
    val scalameta = "1.8.0"
  }

  val Pkg = new {
    lazy val catsCore   = "org.typelevel"  %% "cats-core"  % Ver.cats
    lazy val catsFree   = "org.typelevel"  %% "cats-free"  % Ver.cats
    lazy val scalatest  = "org.scalatest"  %% "scalatest"  % Ver.scalatest
    lazy val scalacheck = "org.scalacheck" %% "scalacheck" % Ver.scalacheck
    lazy val scalameta  = "org.scalameta"  %% "scalameta"  % Ver.scalameta

    lazy val forTest = Seq(catsCore, scalatest, scalacheck).map(_ % "test")
  }
}
