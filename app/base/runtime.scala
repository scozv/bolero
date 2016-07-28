package base

object runtime {
  def conf(key: String): String =
    play.Play.application.configuration.getString(key, "")

  val HTTP_AUTH2_HEADER_KEY = conf("bolero.http.auth2header")
  val HTTP_AUTH2_QUERY_KEY = conf("bolero.http.auth2query")
}