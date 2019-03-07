package project.dao

import org.apache.hadoop.hbase.client.Get
import org.apache.hadoop.hbase.util.Bytes
import project.domain.CourseSearchClickCount
import project.utils.HBaseUtils

import scala.collection.mutable.ListBuffer

/**
  * @author zhanghao
  */
object CourseSearchClickCountDAO {
  private val tableName = "course_search_click_count"
  private val cf = "data"
  private val qualifer = "click_count"


  /**
    * 保存数据到HBase
    *
    * @param list CourseClickCount集合
    */
  def save(list: ListBuffer[CourseSearchClickCount]): Unit = {
    val table = HBaseUtils.getTable(tableName)
    for (ele <- list) {
      table.incrementColumnValue(Bytes.toBytes(ele.day_search_course),
        Bytes.toBytes(cf), Bytes.toBytes(qualifer), ele.clickCount)
    }

  }



  /**
    * 根据rowKey查值
    *
    * @param day_search_course rowKey
    */
  def count(day_search_course: String): Long = {
    val table = HBaseUtils.getTable(tableName)
    val get = new Get(Bytes.toBytes(day_search_course))
    val value = table.get(get).getValue(cf.getBytes, qualifer.getBytes)
    if (value == null) {
      0L
    } else {
      Bytes.toLong(value)
    }
  }


  def main(args: Array[String]): Unit = {
    val list = new ListBuffer[CourseSearchClickCount]
    list.append(CourseSearchClickCount("20180101_www.sougou.com_145", 10))
    list.append(CourseSearchClickCount("20180102_www.baidu.com_189", 100))
    list.append(CourseSearchClickCount("20180104_cn.bing.com_190", 90))
    save(list)

    println(count("20180101_www.sougou.com_145") + ":" + count("20180104_cn.bing.com_190"))
  }


}
