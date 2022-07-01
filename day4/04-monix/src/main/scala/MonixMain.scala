package com.example

import model.{BatchAggregate, BatchModulo, DataType, Nothing, RawFile, Tree}

/**
 *                (Table1)                  (Table2)
 *                  |                       |
 *               RawFile1                   RawFile2________
 *               /       \                /       |     \
 *              Batch12 Batch13.        Batch22 Batch23 Batch24
 *                  \.    /                  \    |     /
 *                   Batch14                    Batch25
 */
object MonixMain {
  def main(args: Array[String]): Unit = {
    val batch14: Tree[DataType] = Tree(BatchAggregate(batchName = "Batch14"))
    val treeTable1 = Tree[DataType](RawFile("table1.csv"))
      .addChildren(Seq(
        Tree[DataType](BatchModulo(batchName = "Batch12", modulo = 2, offset = 0))
          .addChild(batch14),
        Tree[DataType](BatchModulo(batchName = "Batch13", modulo = 2, offset = 1))
          .addChild(batch14)
      ))

    val batch25: Tree[DataType] = Tree(BatchAggregate(batchName = "Batch25"))
    val treeTable2 =
      Tree[DataType](RawFile("table2.csv"))
        .addChildren(Seq(
          Tree[DataType](BatchModulo(batchName = "Batch22", modulo = 3, offset = 0))
            .addChild(batch25),
          Tree[DataType](BatchModulo(batchName = "Batch23", modulo = 3, offset = 1))
            .addChild(batch25),
          Tree[DataType](BatchModulo(batchName = "Batch24", modulo = 3, offset = 2))
            .addChild(batch25)
        ))
    val tree = Tree[DataType](Nothing)
      .addChildren(Seq(
        treeTable1,
        treeTable2)
      )
  }
}
