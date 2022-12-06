
import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % "7.6.0",
    "uk.gov.hmrc" %% "play-frontend-hmrc" % "3.28.0-play-28",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"  % "0.73.0",
    "uk.gov.hmrc" %% "auth-client" % "5.14.0-play-28",
    "uk.gov.hmrc" %% "bootstrap-backend-play-28" % "7.6.0"
  )

  val sharedTestDependencies: Seq[ModuleID] = {
    val scope = "test, it"
    Seq(
      "org.scalatest" %% "scalatest" % "3.2.12" % scope,
      "org.jsoup" % "jsoup" % "1.15.3" % scope,
      "com.typesafe.play" %% "play-test" % current % scope,
      "com.vladsch.flexmark" % "flexmark-all" % "0.62.2" % scope,
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % scope
    )
  }

  val test = Seq(
    "org.mockito" % "mockito-core" % "3.9.0" % Test,
    "org.scalatestplus" %% "mockito-3-4" % "3.2.8.0" % Test,
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.4"
  )

  val it = Seq(
    "com.github.tomakehurst" % "wiremock-jre8" % "2.33.2" % IntegrationTest
  )

  def apply(): Seq[ModuleID] = compile ++ sharedTestDependencies ++ test ++ it

}
