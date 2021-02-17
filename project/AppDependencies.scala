
import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-27" % "3.3.0",
    "uk.gov.hmrc" %% "bootstrap-backend-play-27" % "3.0.0",
    "uk.gov.hmrc" %% "play-frontend-hmrc" % "0.38.0-play-27",
    "uk.gov.hmrc" %% "play-frontend-govuk" % "0.60.0-play-27",
    "uk.gov.hmrc" %% "play-language" % "4.10.0-play-27",
    "uk.gov.hmrc" %% "simple-reactivemongo" % "7.31.0-play-27"
  )

  val sharedTestDependencies: Seq[ModuleID] = {
    val scope = "test, it"
    Seq(
      "org.scalatest" %% "scalatest" % "3.2.3" % scope,
      "org.jsoup" % "jsoup" % "1.13.1" % scope,
      "com.typesafe.play" %% "play-test" % current % scope,
      "com.vladsch.flexmark" % "flexmark-all" % "0.36.8" % scope,
      "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3" % scope,
      "org.scoverage" %% "scalac-scoverage-runtime" % "1.4.1" % scope
    )
  }

  val test = Seq(
    "org.mockito" % "mockito-core" % "3.6.0" % Test,
    "org.scalatestplus" %% "mockito-3-4" % "3.2.3.0" % Test
  )

  val it = Seq(
    "com.github.tomakehurst" % "wiremock-jre8" % "2.27.2" % IntegrationTest
  )

  def apply(): Seq[ModuleID] = compile ++ sharedTestDependencies ++ test ++ it

}
