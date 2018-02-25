package com.data_in_model_out.spark

import org.apache.spark.sql.{Dataset, SparkSession}

/**
  * Created by peterjmyers on 2/24/18.
  */
object SparkSQL {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession
      .builder
      .appName("StructuredNetworkWordCount")
      .getOrCreate()

    import spark.implicits._

    val lines = spark.readStream
      .format("socket")
      .option("host", "localhost")
      .option("port", 9999)
      .load()


    // Split the lines into words
    val words: Dataset[String] = lines.as[String].flatMap(_.split(" "))

    words.createOrReplaceTempView("words")
    val wordsResult = spark.sql("select * from words") // returns another streaming DF

    val query = wordsResult.writeStream
      .outputMode("append")
      .format("console")
      .start()

    query.awaitTermination()
  }
}
