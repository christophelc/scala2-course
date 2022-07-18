package controlflow.model

trait DataType
object DataType {
  case object Nothing extends DataType

  case class RawFile(filename: String) extends DataType
}
