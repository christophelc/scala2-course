import org.apache.spark.sql.SparkSession

trait SparkTest {

  implicit val spark: SparkSession = SparkSession.builder()
    .appName("test")
    .master("local[*]")
    .config("spark.ui.enabled", "false")
    .config("spark.sql.session.timeZone", "UTC")
    .getOrCreate()
}
