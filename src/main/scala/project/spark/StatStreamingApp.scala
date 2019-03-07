package project.spark

import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.{Seconds, StreamingContext}
import project.dao.{CourseClickCountDAO, CourseSearchClickCountDAO}
import project.domain.{ClickLog, CourseClickCount, CourseSearchClickCount}
import project.utils.DateUtils

import scala.collection.mutable.ListBuffer

/**
  * @author zhanghao
  * @create 2019/01/04
  */
object StatStreamingApp {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("StatStreamingApp").setMaster("local[*]")
    val ssc = new StreamingContext(sparkConf, Seconds(60))


    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "localhost:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "spark-consumer",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    val topics = Array("weblogs")

    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams)
    )

    val logs = stream.map(record => record.value())
    logs.print()


    //132.167.124.30	2019-01-04 20:16:00	 GET /class/145.html HTTP/1.1	500 	-
    val cleanLogs = logs.map(_.split("\t")).map(line => {
      val url = line(2).split(" ")(2)
      var courseId = 0
      if (url.startsWith("/class")) {
        val courseIdHtml = url.split("/")(2)
        courseId = courseIdHtml.substring(0, courseIdHtml.lastIndexOf(".")).toInt
      }

      ClickLog(line(0), DateUtils.parseToMinutes(line(1)), courseId, line(3), line(4))
    }).filter(_.courseId != 0)


    //统计到今天为止实战课程的访问量
    cleanLogs.map(x => {
      //HBase rowKey
      (x.time.substring(0, 8) + "_" + x.courseId, 1)
    }).reduceByKey(_ + _).foreachRDD(rdd => {
      rdd.foreachPartition(
        partitionRecord => {
          val list = new ListBuffer[CourseClickCount]
          partitionRecord.foreach(
            record => {
              list.append(CourseClickCount(record._1, record._2))
            }
          )
          CourseClickCountDAO.save(list)
        }
      )
    })



    //统计到搜索引擎过来的课程的访问量
    cleanLogs.map(x => {
      val referer = x.referer.replaceAll("//", "/")
      val splits = referer.split("/")
      var host = ""
      if (splits.length > 2) {
        host = splits(1)
      }
      (host, x.courseId, x.time)
    }
    ).filter(_._1 != "").map(
      x => {
        (x._3.substring(0, 8) + "_" + x._1 + "_" + x._2, 1)
      }
    ).reduceByKey(_ + _).foreachRDD(
      rdd => {
        rdd.foreachPartition(
          partitionRecord => {
            val list = new ListBuffer[CourseSearchClickCount]
            partitionRecord.foreach(
              record => {
                list.append(CourseSearchClickCount(record._1, record._2))
              }
            )
            CourseSearchClickCountDAO.save(list)
          }
        )
      }
    )


    ssc.start()

    ssc.awaitTermination()


  }
}
