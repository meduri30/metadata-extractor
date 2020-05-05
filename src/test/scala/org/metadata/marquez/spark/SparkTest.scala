package org.metadata.marquez.spark

import org.apache.spark.sql.DataFrame
import org.metadata.utils.SparkApp

object SparkTest extends SparkApp("customize_spark_job") {
  val parquetFile: DataFrame =
    spark.
      read.
      format("org.metadata.marquez.spark").
      option("fileFormat", "parquet").
      option("fileLocation", "/Users/aparna/myStuff/myProjects/spline-spark-agent/spline-spark-agent/examples/data/output/batch/job2_stage2_results")
      .option("baseURL", "http://localhost:5000/api/v1")
      .option("sourceName", "Metadata_Extractor")
      .load()
  println(s"Count: ${parquetFile.count()}")
  parquetFile.show(2)
}
