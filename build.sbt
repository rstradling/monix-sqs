lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.11.8",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "monixSqs",
    libraryDependencies ++= Seq(
    	"io.monix" %% "monix" % "2.2.0-M1",
    	"io.monix" %% "monix-cats" % "2.2.0-M1",
    	"com.amazonaws" % "aws-java-sdk" % "1.11.83",
      "com.amazonaws" % "aws-java-sdk-core" % "1.11.83",
      "com.amazonaws" % "aws-java-sdk-sqs" % "1.11.83",
      "org.typelevel" %% "cats" % "0.9.0",
    	// Test dependencies
    	"org.specs2" %% "specs2-core" % "3.8.7" % Test
    	)
  )
    scalacOptions in Test ++= Seq("-Yrangepos")