package base

import org.joda.time.{DateTime, DateTimeZone}

/**
 * 基于UTC+8（北京时间）的时间扩展类
 */
object beijingTime {
  private val DEFAULT_TIMEZONE = DateTimeZone.forID("Asia/Shanghai")
  private val MILLI_SECONDS_FORMAT = "yyMMddHHmmssSSS"
  private val SECONDS_FORMAT = "yyMMddHHmmss"

  def now: DateTime = DateTime.now(DEFAULT_TIMEZONE)

  /**
   * 获得一个毫秒级的即时时间字符串，以yyMMddHHmmssSSS格式表示
   */
  def nowAtMilliseconds = now.toString(MILLI_SECONDS_FORMAT)

  /**
    * 获取一个毫秒级的时间字符串，表示从当前时刻偏移几天
    */
  def dayAtMilliseconds(days: Int) = now.plusDays(days).toString(MILLI_SECONDS_FORMAT)

  /**
    * 将指定的时间，格式化成指定的毫米级别的字符串
    */
  def whenAtMilliseconds(time: DateTime) = time.toString(MILLI_SECONDS_FORMAT)

  def nowAtSeconds = now.toString(SECONDS_FORMAT)
  def dayAtSeconds(days: Int) = now.plusDays(days).toString(SECONDS_FORMAT)
  def whenAtSeconds(time: DateTime) = time.toString(SECONDS_FORMAT)
}
