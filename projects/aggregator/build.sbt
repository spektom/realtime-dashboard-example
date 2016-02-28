name := "aggregator"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "1.5.2",
  "org.apache.spark" %% "spark-streaming" % "1.5.2",
  "org.apache.spark" %% "spark-streaming-kafka" % "1.5.2",
  "com.rethinkdb" % "rethinkdb-driver" %  "2.2-beta-1"
)

mainClass in (Compile, run) := Some("com.appsflyer.meetup.aggregator.Aggregator")
