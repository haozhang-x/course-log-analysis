package project.domain

/**
  * 清洗后的日志信息
  *
  * @param ip   日志访问的ip地址
  * @param time 日志访问的时间
  * @param courseId 课程编号
  * @param statusCode http状态码
  * @param referer 日志访问的referer
  *
  */
case class ClickLog(ip: String, time: String, courseId: Int, statusCode: String, referer: String)
