package project.utils

import java.util.Date

import org.apache.commons.lang3.time.FastDateFormat

/**
  * 日期时间工具类
  *
  * @author zhanghao
  *
  */
object DateUtils {
  private val YYYYMMDDHHMMSS: FastDateFormat = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss")

  private val TARGET_FORMAT: FastDateFormat = FastDateFormat.getInstance("yyyyMMddHHmmss")

  def getTime(time: String): Long = {
    YYYYMMDDHHMMSS.parse(time).getTime
  }


  def parseToMinutes(time: String): String = {
    TARGET_FORMAT.format(new Date(getTime(time)))
  }

}
