package org.metadata.utils

import org.apache.spark.sql.{SQLContext, SQLImplicits, SparkSession}

/**
  * The class represents skeleton of a example application and looks after initialization of SparkSession, etc
  * @param name A spark application name
  * @param master A spark master
  * @param conf Custom properties
  */
abstract class SparkApp
(
  name: String,
  master: String = "local[*]",
  conf: Seq[(String, String)] = Nil
) extends SQLImplicits with App {

  private val sparkBuilder = SparkSession.builder()

  sparkBuilder.appName(name)
  sparkBuilder.master(master)

  for ((k, v) <- conf) sparkBuilder.config(k, v)

  /**
    * A Spark session.
    */
  val spark: SparkSession = sparkBuilder.getOrCreate()

  protected override def _sqlContext: SQLContext = spark.sqlContext
}
