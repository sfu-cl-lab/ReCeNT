package relationalClustering

import org.clapper.argot.ArgotParser
import relationalClustering.bagComparison.ChiSquared
import relationalClustering.clustering.evaluation.{AdjustedRandIndex, LabelsContainer}
import relationalClustering.clustering.{Hierarchical, Spectral}
import relationalClustering.representation.KnowledgeBase
import relationalClustering.similarity.{SimilarityNTv2, SimilarityNeighbourhoodTrees}
import relationalClustering.utils.{Helper, PredicateDeclarations}

/**
  * Created by seb on 05.02.16.
  */
object CommandLineInterface {

  //parser specification
  import org.clapper.argot.ArgotConverters._

  val parser = new ArgotParser("RelationalClustering.jar", preUsage = Some("Version 2.1"))
  val dbs = parser.multiOption[String](List("db"), "knowledgeBase", "database(s) with data to cluster")
  val head = parser.option[String](List("domain"), "domain definition", "header for the knowledge base(s); specification of logical predicates")
  val declarationFile = parser.option[String](List("declarations"), "file path", "file containing declarations of predicates")
  val depth = parser.option[Int](List("depth"), "n", "depth of the neighbourhood graph")
  val rootFolder = parser.option[String](List("root"), "filePath", "folder to place files in")
  val k = parser.option[Int](List("k"), "n", "number of clusters to create")
  val query = parser.option[String](List("query"), "comma-separated list", "list of query domains")
  val weights = parser.option[String](List("weights"), "Array[Double]", "comma-separated list of weights [attributes,attribute distribution,connections,vertex neighbourhood, edge distribution]")
  val algorithm = parser.option[String](List("algorithm"), "[Spectral|Hierarchical]", "algorithm to perform clustering")
  val similarity = parser.option[String](List("similarity"), "[RCNT|RCNTv2|HS|RIBL|HSAG]", "similarity measure")
  val bag = parser.option[String](List("bagSimilarity"), "[chiSquared]", "bag similarity measure")
  val linkage = parser.option[String](List("linkage"), "[average|complete|ward]", "linkage for hierarchical clustering")
  val validate = parser.flag[Boolean](List("validate"), "should validation be performed")
  val labels = parser.option[String](List("labels"), "file path to the labels", "labels for the query objects")
  val valMethod = parser.option[String](List("validationMethod"), "[ARI|RI]", "cluster validation method")


  def main(args: Array[String]) {
    parser.parse(args)
    require(head.hasValue, "no header specified")
    require(dbs.hasValue, "no databases specified")
    require(query.hasValue, "query not specified")
    require(declarationFile.hasValue, "declarations of the predicates not provided")

    val predicateDeclarations = new PredicateDeclarations(declarationFile.value.get)
    val KnowledgeBase = new KnowledgeBase(dbs.value, Helper.readFile(head.value.get).mkString("\n"), predicateDeclarations)

    val similarityMeasure = similarity.value.getOrElse("RCNT") match {
      case "RCNT" =>
        val bagComparison = bag.value.getOrElse("chiSquared") match {
          case "chiSquared" => new ChiSquared()
        }
        new SimilarityNeighbourhoodTrees(KnowledgeBase,
                                         depth.value.getOrElse(0),
                                         weights.value.getOrElse("0.2,0.2,0.2,0.2,0.2").split(",").toList.map(_.toDouble),
                                         bagComparison)
      case "RCNTv2" =>
        val bagComparison = bag.value.getOrElse("chiSquared") match {
          case "chiSquared" => new ChiSquared()
        }
        new SimilarityNTv2(KnowledgeBase,
                           depth.value.getOrElse(0),
                           weights.value.getOrElse("0.2,0.2,0.2,0.2,0.2").split(",").toList.map(_.toDouble),
                           bagComparison)
    }

    val clusters = algorithm.value.getOrElse("Spectral") match {
      case "Spectral" =>
        val filename = similarityMeasure.getObjectSimilaritySave(query.value.get.split(",").toList, rootFolder.value.getOrElse("./tmp"))
        val cluster = new Spectral(rootFolder.value.getOrElse("./tmp"))
        cluster.clusterFromFile(filename, k.value.getOrElse(2))

      case "Hierarchical" =>
        val filename = similarityMeasure.getObjectSimilaritySave(query.value.get.split(",").toList, rootFolder.value.getOrElse("./tmp"))
        val cluster = new Hierarchical(linkage.value.getOrElse("average"), rootFolder.value.getOrElse("./tmp"))
        cluster.clusterFromFile(filename, k.value.getOrElse(2))
    }

    println("FOUND CLUSTERS")
    clusters.zipWithIndex.foreach( cluster => println(s"CLUSTER ${cluster._2}: ${cluster._1.mkString(",")}"))


    //if validation is to be performed
    if (validate.value.getOrElse(false)) {
      val labContainer = new LabelsContainer(labels.value.get)
      val validator = valMethod.value.getOrElse("ARI") match {
        case "ARI" => new AdjustedRandIndex(rootFolder.value.getOrElse("./tmp"))
      }

      println(s"${valMethod.value.getOrElse("ARI")} score: ${validator.validate(clusters, labContainer)}")
    }
  }
}