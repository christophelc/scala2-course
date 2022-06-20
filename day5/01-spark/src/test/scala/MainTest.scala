import org.apache.commons.io.FileUtils

import java.io.File

class MainTest extends SparkSpec {

 "Spark" must "create RDD from a sequence" in  {

	 val rdd = spark.sparkContext.parallelize(Seq(
		 ("Cat", "Felix"),
		 ("Cat", "Tiger"),
		 ("Dog", "Rex"),
		 ("Dog", "Beethoven")
	 ))
	 rdd.foreach(println)
	 println(s"Rdd partition: ${rdd.partitions.length}")
	 assert(rdd.count == 4)
 }

	"Spark" must "create RDD from a csv fiile" in {
		val path = getClass.getResource("/").getPath
		val df = spark.read.option("header", "true").csv(s"$path/csv-schema1.csv")
		df.rdd.foreach(println)
		assert(df.rdd.count == 5)
	}

	"Spark" must "infer schema from a csv fiile" in {
		val path = getClass.getResource("/").getPath
		val df = spark.read.option("header", "true").csv(s"$path/csv-schema1.csv")
		val expectedJson = """{
										 |  "type": "struct",
										 |  "fields": [
										 |    {
										 |      "name": "plant",
										 |      "type": "string",
										 |      "nullable": true,
										 |      "metadata": { }
										 |    },
										 |    {
										 |      "name": "rate_of_growth",
										 |      "type": "string",
										 |      "nullable": true,
										 |      "metadata": { }
										 |    },
										 |    {
										 |      "name": "date_measure",
										 |      "type": "string",
										 |      "nullable": true,
										 |      "metadata": { }
										 |    },
										 |    {
										 |      "name": "sv",
										 |      "type": "string",
										 |      "nullable": true,
										 |      "metadata": { }
										 |    },
										 |    {
										 |      "name": "ev",
										 |      "type": "string",
										 |      "nullable": true,
										 |      "metadata": { }
										 |    }
										 |  ]
										 |}
										 |""".stripMargin
		val expected = expectedJson
			.replace(System.lineSeparator, "")
			.replace(" ", "")
		assert(df.schema.json == expected)
	}

	// it test in fact here
	"Spark" must "Write dataframe as avro" in {
		val path = getClass.getResource("/").getPath
		FileUtils.deleteDirectory(new File(s"$path/example"))
		val df = spark.read.option("header", "true").csv(s"$path/csv-schema1.csv")
		df.write
			.format("avro")
			.save(s"$path/example/file")
		val df2 = spark
			.read
			.format("avro")
			.load(s"$path/example/file")
		df2.show(false)
		assert(df2.count() == 5)
	}
	"Spark" must "Write dataframe as delta" in {
		val path = getClass.getResource("/").getPath
		FileUtils.deleteDirectory(new File(s"$path/example"))
		val df = spark.read.option("header", "true").csv(s"$path/csv-schema1.csv")
		df.write
			.format("delta")
			.save(s"$path/example/file")
		val df2 = spark
			.read
			.format("delta")
			.load(s"$path/example/file")
		df2.show(false)
		assert(df2.count() == 5)
	}
}
