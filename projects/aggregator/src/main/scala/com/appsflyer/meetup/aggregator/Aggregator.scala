package com.appsflyer.meetup.aggregator

import com.fasterxml.jackson.core.JsonParseException
import kafka.serializer.StringDecoder
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.json4s._
import org.json4s.jackson.JsonMethods._


/**
  * Aggregates events over specified sliding window using Spark Streaming, and writes them to RethinkDB
  */
object Aggregator {

  case class Event(appId: String, ip: String, deviceType: String, country: String, city: String)

  /**
    * Parse JSON message
    */
  private def parseEvent(msg: String): Event = {
    implicit val formats = DefaultFormats
    try {
      return parse(msg).extract[Event]
    } catch {
      case _: JsonParseException => null
    }
  }

  /**
    * Create a local StreamingContext with two working threads and batch interval of 2 seconds
    */
  private def createStreamingContext(): StreamingContext = {
    val sparkConf = new SparkConf().setAppName("Aggregator").setMaster("local[2]")
      // Adapt stream rate to our computing resources
      .set("spark.streaming.backpressure.enabled", "true")
      
    return new StreamingContext(sparkConf, Seconds(2))
  }

  /**
    * Create Kafka stream of events
    */
  private def createKafkaStream(ssc: StreamingContext): DStream[(String, String)] = {
    val kafkaParams = Map(
      "metadata.broker.list" -> "kafka.appsflyer.com:9092",
      "auto.offset.reset" -> "largest"
    )
    val topics = Set("installs")
    return KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topics)
  }

  /**
    * Aggregates install events by country, and writes top 100 of them to the DB
    */
  private def findTopCountries(stream: DStream[Event]): Unit = {
    stream.map(event => ((event.appId, event.country), 1))
      .reduceByKeyAndWindow(_ + _, Seconds(2))
      .foreachRDD { rdd =>
        val topCountries = rdd
          .takeOrdered(100)(Ordering[Int].reverse.on(x => x._2))
          .groupBy(_._1._1)
          .mapValues(_.map(x => (x._1._2, x._2)).toMap)

        DB.insertStats("geo_stats", topCountries)
      }
  }

  /**
    * Aggregates install events by device, and writes top 5 of them to the DB
    */
  private def findTopDevices(stream: DStream[Event]): Unit = {
    stream.map(event => ((event.appId, event.deviceType), 1))
      .reduceByKeyAndWindow(_ + _, Seconds(2))
      .foreachRDD { rdd =>
        val topCountries = rdd
          .takeOrdered(5)(Ordering[Int].reverse.on(x => x._2))
          .groupBy(_._1._1)
          .mapValues(_.map(x => (x._1._2, x._2)).toMap)

        DB.insertStats("device_stats", topCountries)
      }
  }

  /**
    * Calculates total installs, and writes them to the DB
    */
  private def calcTotalInstalls(stream: DStream[Event]): Unit = {
    stream.map(event => ((event.appId, "installs"), 1))
      .reduceByKeyAndWindow(_ + _, Seconds(2))
      .foreachRDD { rdd =>
        val totalStats = rdd
          .groupBy(_._1._1)
          .mapValues(_.map(x => (x._1._2, x._2)).toMap).collectAsMap.toMap
        DB.insertStats("total_stats", totalStats)
      }
  }

  def main(args: Array[String]) {
    DB.initialize()

    val ssc = createStreamingContext()
    val kafkaStream = createKafkaStream(ssc)

    // Extract events
    val eventStream = kafkaStream.map { case (_, msg) => parseEvent(msg) }.filter(_ != null)

    findTopCountries(eventStream)
    findTopDevices(eventStream)
    calcTotalInstalls(eventStream)

    // Start execution of streams
    ssc.start()
    ssc.awaitTermination()
  }
}
