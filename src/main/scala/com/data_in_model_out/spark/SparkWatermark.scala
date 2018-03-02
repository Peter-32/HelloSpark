package com.data_in_model_out.spark

import org.apache.spark.sql.{Dataset, ForeachWriter, Row, SparkSession}

/**
  * Created by peterjmyers on 2/25/18.
  */
object SparkWatermark {
  case class MyClass(value: String)

  def main(args: Array[String]): Unit = {


    val spark = SparkSession
      .builder
      .appName("StructuredNetworkWordCount")
      .getOrCreate()

    import spark.implicits._

    val accumulator = spark.sparkContext.longAccumulator
    val lines = spark.readStream
      .format("socket")
      .option("host", "localhost")
      .option("port", 9999)
      .load()

    // Split the lines into words
    val words: Dataset[MyClass] = lines.as[MyClass]
    val words2 = words.flatMap(_.value.split(" "))

    words2.createOrReplaceTempView("words")
    val wordsResult = spark.sql("select * from words") // returns another streaming DF
    val query = wordsResult.writeStream
        .foreach(new ForeachWriter[Row]() {
          // true means that all partitions will be opened
          override def open(partitionId: Long, version: Long): Boolean = true

          override def process(row: Row): Unit = {
            accumulator.add(1)
            println(s">> Processing row ${accumulator.value}: ${row}")
          }

          override def close(errorOrNull: Throwable): Unit = {
            // do nothing
          }
        })
      .outputMode("append")
      //.format("console")
      .start()

    query.awaitTermination()
  }
}
