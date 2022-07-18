package util

import controlflow.model.data.{RowPlant, TablePlant}

import scala.io.Source

object FileIO {
  def load(path: String): TablePlant = {
    val rows: Seq[RowPlant] = Source.fromResource(path)
      .getLines()
      .drop(1)
      .map(line => {
        line.split(",").toSeq.map(_.trim) match {
          case Seq(plant, ratePct) => RowPlant(plant, ratePct.toInt)
        }
      }).toSeq
    TablePlant(rows)
  }
}
