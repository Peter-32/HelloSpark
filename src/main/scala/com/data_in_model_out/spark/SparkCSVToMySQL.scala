package com.data_in_model_out.spark

import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{Dataset, SparkSession}

/**
  * Created by peterjmyers on 3/1/18.
  */
object SparkCSVToMySQL {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession
      .builder
      .appName("StructuredNetworkWordCount")
      .getOrCreate()

    import spark.implicits._

    val userSchema = new StructType().add("one", "double")
    val csvDF = spark
      .readStream
      .option("header", "true")
      .option("sep", ",")
      .schema(userSchema)
      .csv("resources")

    val dataset: Dataset[Double] = csvDF.as[Double]

    dataset.createOrReplaceTempView("numbers")
    val wordsResult = spark.sql("select SUM(one + one) from numbers") // returns another streaming DF

    val query = wordsResult.writeStream
      .outputMode("complete")
      .format("console")
      .start()

    query.awaitTermination()
  }
}
