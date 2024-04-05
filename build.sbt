import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.DefaultBuildSettings.addTestReportOption
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "claim-vat-enrolment-frontend"

val silencerVersion = "1.7.16"

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys

  val exclusionList: List[String] = List(
    "<empty>",
    "Reverse.*",
    "app.*",
    "config.*",
    ".*(AuthService|BuildInfo|Routes).*",
    "testOnly.*",
    "business.*",
    "testOnlyDoNotUseInAppConf.*",
    "uk.gov.hmrc.claimvatenrolmentfrontend.featureswitch.api.*",
    "uk.gov.hmrc.claimvatenrolmentfrontend.featureswitch.frontend.*",
    "uk.gov.hmrc.claimvatenrolmentfrontend.testOnly.*",
    "uk.gov.hmrc.claimvatenrolmentfrontend.views.html.*"
  )

  Seq(
    ScoverageKeys.coverageExcludedPackages := exclusionList.mkString(";"),
    ScoverageKeys.coverageMinimumStmtTotal := 90,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true
  )
}

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "2.13.13"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    PlayKeys.playDefaultPort := 9936,
    libraryDependencies ++= AppDependencies.apply(),
    TwirlKeys.templateImports ++= Seq(
      "uk.gov.hmrc.claimvatenrolmentfrontend.config.AppConfig",
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.govukfrontend.views.html.components.implicits._",
      "uk.gov.hmrc.hmrcfrontend.views.html.helpers._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components.implicits._",
      "uk.gov.hmrc.claimvatenrolmentfrontend.views.helpers.ViewUtils._"
    ),
    // ***************
    // Use the silencer plugin to suppress warnings
    // You may turn it on for `views` too to suppress warnings from unused imports in compiled twirl templates, but this will hide other warnings.
    scalacOptions += "-P:silencer:pathFilters=views;routes",
    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
      "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
    )
    // ***************
  )
  .settings(scoverageSettings)
  .settings(resolvers += Resolver.jcenterRepo)
  .disablePlugins(JUnitXmlReportPlugin)

Test / Keys.fork := true
Test / javaOptions += "-Dlogger.resource=logback-test.xml"
Test / parallelExecution := true
addTestReportOption(Test, "test-reports")

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(DefaultBuildSettings.itSettings())
  .settings(libraryDependencies ++= AppDependencies.test)