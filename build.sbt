import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.DefaultBuildSettings.addTestReportOption
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "claim-vat-enrolment-frontend"

val silencerVersion = "1.7.0"

lazy val scoverageSettings = {

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
    ScoverageKeys.coverageMinimum := 90,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true
  )
}

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    majorVersion := 0,
    scalaVersion := "2.12.12",
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
  .settings(publishingSettings: _*)
  .configs(IntegrationTest)
  .settings(DefaultBuildSettings.integrationTestSettings())
  .settings(resolvers += Resolver.jcenterRepo)
  .disablePlugins(JUnitXmlReportPlugin)

Keys.fork in Test := true
javaOptions in Test += "-Dlogger.resource=logback-test.xml"
parallelExecution in Test := true
addTestReportOption(Test, "test-reports")

Keys.fork in IntegrationTest := true
unmanagedSourceDirectories in IntegrationTest := (baseDirectory in IntegrationTest) (base => Seq(base / "it")).value
javaOptions in IntegrationTest += "-Dlogger.resource=logback-test.xml"
addTestReportOption(IntegrationTest, "int-test-reports")
parallelExecution in IntegrationTest := false