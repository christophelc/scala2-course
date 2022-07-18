package controlflow.model

sealed trait Batch extends DataType {
  val batchName: String
}
case class BatchModulo(override val batchName: String, modulo: Int, offset: Int, delaySec: Option[Int] = None) extends Batch
case class BatchAggregate(override val batchName: String) extends Batch
