name := "RelationalClusteringOverNeighbourhoodGraphs"

version := "2.2.6"

scalaVersion := "2.11.7"

mainClass in assembly := some("relationalClustering.CommandLineInterface")
assemblyJarName := "RelationalClustering.jar"

libraryDependencies += "org.clapper" %% "argot" % "1.0.3"

libraryDependencies  ++= Seq(
  // other dependencies here
  "org.scalanlp" %% "breeze" % "0.12",
  // native libraries are not included by default. add this if you want them (as of 0.7)
  // native libraries greatly improve performance, but increase jar sizes.
  // It also packages various blas implementations, which have licenses that may or may not
  // be compatible with the Apache License. No GPL code, as best I know.
  "org.scalanlp" %% "breeze-natives" % "0.12",
  // the visualization library is distributed separately as well.
  // It depends on LGPL code.
  "org.scalanlp" %% "breeze-viz" % "0.12"
  // git-wagon for publishing
  //"net.trajano.wagon" % "wagon-git" % "2.0.5-SNAPSHOT"
)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}