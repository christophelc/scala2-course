package com.example 

import io.delta.tables.DeltaTable
import org.apache.spark.sql.{Column, DataFrame, SparkSession}
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.types.StructType

import scala.util.{Failure, Success, Try}

object SparkMain {

  def main(args: Array[String]): Unit = {

    val spark: SparkSession = SparkSession
      .builder()
      .appName("Wax-Test")
      .config("spark.master", "local")
      .config("spark.sql.session.timeZone", "UTC")
      .getOrCreate()

    val path = getClass.getResource("/").getPath
    println(s"save to $path")
    val df1 = spark.read.option("header", "true").csv(s"$path/csv-schema1.csv")
    val df2 = spark.read.option("header", "true").csv(s"$path/csv-schema2.csv")
    df1.write.format("delta").mode("overwrite").save(s"$path/delta/data")
    df1.show(false)
    df1.printSchema()
    df2.show(false)
    df2.printSchema()
    df2.write.format("delta").mode("append").option("mergeSchema", "true").save(s"$path/delta/data")
    val df = spark.read.format("delta").load(s"$path/delta/data").show(false)
    // update column order
    val dfUpdate = spark.read.format("delta").load(s"$path/delta/data")
    val colToDrop = "date_measure"
    val columnsOrdered: Seq[Column] = df2.schema.map(_.name).filter(_ != colToDrop).map(col)
    dfUpdate.select(columnsOrdered: _*)
      .write.format("delta")
      .mode("overwrite")
      .option("overwriteSchema", "true")
      .save(s"$path/delta/data")
    spark.read.format("delta").load(s"$path/delta/data").show(false)
    spark.stop()

  }
}
