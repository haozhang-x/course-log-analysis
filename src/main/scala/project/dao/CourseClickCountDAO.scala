package project.dao

import org.apache.hadoop.hbase.client.Get
import org.apache.hadoop.hbase.util.Bytes
import project.domain.CourseClickCount
import project.utils.HBaseUtils

import scala.collection.mutable.ListBuffer

/**
  * 实战课程点击数数据访问层
  */
object CourseClickCountDAO {
  private val tableName = "course_click_count"
  private val cf = "data"
  private val qualifer = "click_count"


  /**
    * 保存数据到HBase
    *
    * @param list CourseClickCount集合
    */
  def save(list: ListBuffer[CourseClickCount]): Unit = {
    val table = HBaseUtils.getTable(tableName)
    for (ele <- list) {
      table.incrementColumnValue(Bytes.toBytes(ele.dayCourse),
        Bytes.toBytes(cf), Bytes.toBytes(qualifer), ele.clickCount)
    }

  }


  /**
    * 根据rowKey查值
    *
    * @param day_course rowKey
    */
  def count(day_course: String): Long = {
    val table = HBaseUtils.getTable(tableName)
    val get = new Get(Bytes.toBytes(day_course))
    val value = table.get(get).getValue(cf.getBytes, qualifer.getBytes)
    if (value == null) {
      0L
    } else {
      Bytes.toLong(value)
    }
  }


  /*
  test
   */

  def main(args: Array[String]): Unit = {
    val list = new ListBuffer[CourseClickCount]
    list.append(CourseClickCount("20180101_126", 10))
    list.append(CourseClickCount("20180102_126", 100))
    list.append(CourseClickCount("20180104_126", 90))
    save(list)

    println(count("20180101_126") + ":" + count("20180104_126"))

  }

}
