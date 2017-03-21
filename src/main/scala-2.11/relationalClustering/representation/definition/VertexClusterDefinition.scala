package relationalClustering.representation.definition

/**
  * Created by seb on 21.03.17.
  */
class VertexClusterDefinition(protected val tupleContexts: List[TupleContext]) {

  protected def stringRep(tuples: List[TupleContext], initialOffset: Int = 0): String = {
    tuples.groupBy(_.getSimilaritySource).map(simGroup => {

      s"${"\t" * initialOffset}SIMILARITY SOURCE ${simGroup._1}\n" + simGroup._2.groupBy(_.getDepth).map(depthGroup => {
        s"${"\t" * (initialOffset + 1)}DEPTH ${depthGroup._1}\n" + depthGroup._2.groupBy(_.getVType).map(typeGroup => {
          s"${"\t" * (initialOffset + 2)}VERTEX TYPE ${typeGroup._1}\n" + typeGroup._2.map(_.stringRep(initialOffset + 3)).mkString("\n") + "\n"
        }).mkString("\n")
      }).mkString("\n")
    }).mkString("\n\n")
  }

  override def toString: String = {
    stringRep(tupleContexts)
  }

}
