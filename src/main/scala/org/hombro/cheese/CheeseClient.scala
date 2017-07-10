package org.hombro.cheese

import org.apache.commons.lang3.StringUtils
import org.hombro.cheese.api.CheeseInfo
import org.jsoup.Jsoup
import org.jsoup.select.Elements

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import scalaj.http.{Http, HttpOptions, HttpRequest}

/**
  * Created by nicolas on 7/4/2017.
  */
object CheeseClient {
  private val baseUrl = "https://www.cheese.com/"
  private val alphaEndpoint = baseUrl + "alphabetical/"

  private def parseInfo(html: String) = {
    val soup = Jsoup.parse(html)
    val name = soup.getElementsByClass("unit").get(0).getElementsByTag("h3").get(0).text()
    val summary = soup.getElementsByClass("summary").get(0)

    val img =
      if (soup.select(".tmb img").size() > 0)
        Some(baseUrl + soup.select(".tmb img").get(0).attr("src"))
      else
        None

    val description = ListBuffer[String]()

    for (i <- 0 until summary.getElementsByTag("p").size()) {
      description.append(summary.getElementsByTag("p").get(i).text())
    }
    description -= description.last // hacky, but the final paragraph should be an <h>

    def fromColon(s: String) = s.split(":")(1).substring(1)
    def getLine(begin: String, elements: Elements): String = {
      for (i <- 0 until elements.size()) {
        if (elements.get(i).text().startsWith(begin))
          return fromColon(elements.get(i).text())
      }
      ""
    }
    val listElements = summary.getElementsByTag("ul").get(0).getElementsByTag("li")

    CheeseInfo(
      name,
      description.mkString("\n"),
      getLine("Region", listElements),
      getLine("Family", listElements),
      getLine("Rind", listElements),
      getLine("Colour", listElements),
      getLine("Aroma", listElements).split(", ").toList,
      getLine("Producers", listElements).split(",").toList,
      img
    )
  }

  private def parseNames(htmls: List[String]) = {
    def parsePage(html: String) = {
      val soup = Jsoup.parse(html)
      val elements = soup.getElementsByClass("unit")
      val buffer = ListBuffer[String]()
      for (i <- 0 until elements.size()) {
        buffer.append(elements.get(i).getElementsByAttribute("href").get(1).getElementsByAttribute("href").text())
      }
      buffer.toList
    }
    htmls flatMap parsePage
  }

  private def getThePages(request: HttpRequest, page: Int) = {

    @tailrec
    def _helper(request: HttpRequest, page: Int, prev: String, list: List[String]): List[String] = {
      val toHit = request
        .param("page", page.toString)
        .param("per_page", "100") // best we can do
        .option(HttpOptions.allowUnsafeSSL)
      val out = toHit.asString.body
      if (out == prev)
        list ++ List(out)
      else {
        _helper(request, page + 1, out, list ++ List(out))
      }
    }

    _helper(request, page, "", List())
  }
}

case class CheeseClient() extends CheeseAPI with CheeseGatherer {
  // 90% of the listed cheeses
  private def sanitizeName(name: String) = {
    StringUtils.stripAccents(name).toLowerCase
      .replace(" a ", "-")
      .replace(" ", "-")
      .replace("’", "")
      .replace(",", "")
      .replace("'", "")
      .replace("(", "")
      .replace(")", "")
      .replace(".", "")
      .replace("of", "")
      .replace("%", "")
      .replace("&", "")
      .replace("---", "-")
      .replace("--", "-")
  }

  private def cheeseInfoEndpoint(name: String) = CheeseClient.baseUrl + sanitizeName(name) + "/"

  def getCheeseNames(startingWith: String = "") = {
    assert(startingWith.length <= 1)
    val pages = if (startingWith.isEmpty)
      CheeseClient.getThePages(Http(CheeseClient.baseUrl), 0)
    else
      CheeseClient.getThePages(Http(CheeseClient.alphaEndpoint).param("i", startingWith.toLowerCase), 0)
    CheeseClient.parseNames(pages)
  }

  def getCheeseInfo(cheeseName: String) = {
    val endpoint = cheeseInfoEndpoint(cheeseName)
    try {
      val response = Http(endpoint)
        .option(HttpOptions.connTimeout(1000))
        .option(HttpOptions.readTimeout(5000))
        .option(HttpOptions.allowUnsafeSSL).asString
      if (response.isError) None else Some(CheeseClient.parseInfo(response.body))
    } catch {
      case e: Throwable =>
        println(e)
        println(endpoint)
        None
    }
  }
}
