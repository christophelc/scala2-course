package model.data

object TablePlant {
  private def multiply(head: RowPlant, tail: Seq[RowPlant]): RowPlant = {
    tail.toList match {
      case Nil => head
      case head1::tail1 =>
        val result = multiply(head1, tail1)
        RowPlant(head.plant, (head.ratePct + 1) * (result.ratePct + 1))
    }
  }
  def reduce(table1: TablePlant, table2: TablePlant): TablePlant = {
    val rows = (table1.rows ++ table2.rows).groupBy(_.plant).map {
      case (_, seq) => multiply(seq.head, seq.tail)
    }.toSeq
    TablePlant(rows = rows.sortBy(_.plant))
  }
}
case class TablePlant(rows: Seq[RowPlant]) {
  override def toString = rows.toList.mkString(System.lineSeparator)
  def modulo(mod: Int, offset: Int): TablePlant = TablePlant(rows =
    rows.map(row => row.copy(ratePct = (row.ratePct + offset) % mod)))
}
case class RowPlant(plant: String, ratePct: Int)
