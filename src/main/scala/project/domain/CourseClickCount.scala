package project.domain

/**
  * 实战课程点击数实体类
  *
  * @param day_course  对应的就是HBase中的RowKey 20180101_167
  * @param click_count 访问总数
  */
case class CourseClickCount(dayCourse: String, clickCount: Long)
