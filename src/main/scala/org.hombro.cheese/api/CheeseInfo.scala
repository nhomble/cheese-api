package org.hombro.cheese.api

import org.jsoup.Jsoup

import scala.collection.mutable.ListBuffer

/**
  * Created by nicolas on 7/1/2017.
  */
object CheeseInfo {
  def parseInfo(html: String) = {
    val soup = Jsoup.parse(html)
    val name = soup.getElementsByClass("unit").get(0).getElementsByTag("h3").get(0).text()
    val summary = soup.getElementsByClass("summary").get(0)

    val description = ListBuffer[String]()

    for (i <- 0 until summary.getElementsByTag("p").size()) {
      description.append(summary.getElementsByTag("p").get(i).text())
    }

    def fromColon(s: String) = s.split(":")(1).substring(1)

    CheeseInfo(name, description.mkString("\n"))
  }

  def parseNames(htmls: List[String]) = {
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
}

case class CheeseInfo(val name: String,
                      val description: String)
