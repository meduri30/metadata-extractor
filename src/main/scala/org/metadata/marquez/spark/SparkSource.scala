package org.metadata.marquez.spark

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.sql.sources.{BaseRelation, DataSourceRegister, RelationProvider, SchemaRelationProvider, TableScan}
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.metadata.marquez.model.GetSource

class MarquezSparkSource(location: String,
                         fileFormat: String,
                         userSchema: StructType)
                        (@transient val sqlContext: SQLContext)
  extends BaseRelation with TableScan with DataSourceRegister with Serializable {

  override def schema: StructType = {
    if (userSchema != null) userSchema
    else StructType(Seq(
      StructField("name", StringType, true),
      StructField("count", StringType, true)
    ))
  }

  override def buildScan(): RDD[Row] = {
    println(s"I am in custom source!")
    fileFormat.toUpperCase match {
      case "PARQUET" => {
        val df = sqlContext.sparkSession.read.parquet(location)
        println(s"Columns in Parquet: ${df.columns.mkString(",")}")
        df.rdd
      }
      case _ => sqlContext.sparkSession.read.text(location).rdd
    }
  }

  override def shortName(): String = "marquez"
}

class DefaultSource extends RelationProvider with SchemaRelationProvider {
  override def createRelation(sqlContext: SQLContext,
                              parameters: Map[String, String]): BaseRelation = {
    createRelation(sqlContext, parameters, null)
  }

  override def createRelation(sqlContext: SQLContext,
                              parameters: Map[String, String],
                              schema: StructType): BaseRelation = {
    val fileLocation: String = parameters("fileLocation")
    val fileFormat: String = parameters("fileFormat")
    val baseURL: String = parameters("baseURL")
    val sourceName: String = parameters("sourceName")
    val getSourceInfo: Option[GetSource] = GetSource(baseURL, sourceName)
    val marquezSparkSource =
      getSourceInfo
        .fold(new MarquezSparkSource(fileLocation, fileFormat, schema)(sqlContext)) {
          getSource =>
            println(s"Finally !!!!!!! from Spark 2 Marquez")
            new MarquezSparkSource(getSource.connectionUrl, fileFormat, schema)(sqlContext)
        }
    //    val marquezSparkSource =
    //      new MarquezSparkSource(fileLocation, fileFormat, schema)(sqlContext)
    marquezSparkSource
  }
}
