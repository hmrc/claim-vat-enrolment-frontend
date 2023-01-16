
import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % "7.12.0",
    "uk.gov.hmrc" %% "play-frontend-hmrc" % "6.2.0-play-28",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"  % "0.74.0",
    "uk.gov.hmrc" %% "auth-client" % "6.0.0-play-28",
    "uk.gov.hmrc" %% "bootstrap-backend-play-28" % "7.12.0"
  )

  val sharedTestDependencies: Seq[ModuleID] = {
    val scope = "test, it"
    Seq(
      "org.scalatest" %% "scalatest" % "3.2.15" % scope,
      "org.jsoup" % "jsoup" % "1.15.3" % scope,
      "com.typesafe.play" %% "play-test" % current % scope,
      "com.vladsch.flexmark" % "flexmark-all" % "0.62.2" % scope,
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % scope
    )
  }

  val test: Seq[ModuleID] = Seq(
    "org.mockito" % "mockito-core" % "4.10.0" % Test,
    "org.scalatestplus" %% "mockito-3-4" % "3.2.10.0" % Test,
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.14.1"
  )

  val it: Seq[ModuleID] = Seq(
    "com.github.tomakehurst" % "wiremock-jre8" % "2.35.0" % IntegrationTest
  )

  def apply(): Seq[ModuleID] = compile ++ sharedTestDependencies ++ test ++ it

}
