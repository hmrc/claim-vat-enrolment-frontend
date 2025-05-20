
import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  private val playVersion = "play-30"
  val mongoDbVersion   = "2.6.0"
  val bootstrapVersion = "8.6.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% s"bootstrap-frontend-$playVersion" % bootstrapVersion,
    "uk.gov.hmrc" %% s"play-frontend-hmrc-$playVersion" % "8.5.0",
    "uk.gov.hmrc.mongo" %% s"hmrc-mongo-$playVersion"  % mongoDbVersion,
    "uk.gov.hmrc" %% s"bootstrap-backend-$playVersion" % bootstrapVersion
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"  %% s"bootstrap-test-$playVersion"  % bootstrapVersion    % Test,
    "org.jsoup" % "jsoup" % "1.17.2" % Test,
    "org.playframework"  %% "play-test" % current % Test,
    "com.vladsch.flexmark" % "flexmark-all" % "0.64.8" % Test,
    "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % Test,
    "org.mockito" % "mockito-core" % "5.11.0" % Test,
    "org.scalatestplus" %% "mockito-3-4" % "3.2.10.0" % Test,
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.17.0"
  )

  def apply(): Seq[ModuleID] = compile ++ test

}
