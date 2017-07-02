package org.hombro.cheese

import com.typesafe.scalalogging.Logger
import org.hombro.cheese.api.CheeseInfo

import scala.annotation.tailrec
import scalaj.http.{Http, HttpRequest}

/**
  * Created by nicolas on 7/1/2017.
  */
object CheeseClient {
  private val BASE_URL = "http://www.cheese.com/"
  private val ENDPOINT_ALPHA = BASE_URL + "alphabetical/"

  protected def cheeseInfoEndpoint(name: String) = BASE_URL + name.toLowerCase() + "/"
}

trait CheeseAPI {
  def getCheese(startingWith: String): List[String]

  def getCheeseInfo(cheeseName: String): CheeseInfo
}

case class CheeseClient() extends CheeseAPI {
  val logger = Logger(classOf[CheeseClient])

  private def getThePages(request: HttpRequest, page: Int) = {

    @tailrec
    def _helper(request: HttpRequest, page: Int, prev: String, list: List[String]): List[String] = {
      val toHit = request
        .param("page", page.toString)
        .param("per_page", "100") // best we can do
      val out = toHit.asString.body
      if (out == prev)
        list ++ List(out)
      else {
        _helper(request, page + 1, out, list ++ List(out))
      }
    }

    _helper(request, page, "", List())
  }

  def getCheese(startingWith: String = "") = {
    assert(startingWith.length <= 1)
    val pages = if (startingWith.isEmpty)
      getThePages(Http(CheeseClient.BASE_URL), 0)
    else
      getThePages(Http(CheeseClient.ENDPOINT_ALPHA).param("i", startingWith.toLowerCase), 0)
    CheeseInfo.parseNames(pages)
  }

  def getCheeseInfo(cheeseName: String) = {
    CheeseInfo.parseInfo(Http(CheeseClient.cheeseInfoEndpoint(cheeseName)).asString.body)
  }
}
