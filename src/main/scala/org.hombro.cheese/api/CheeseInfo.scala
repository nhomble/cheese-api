package org.hombro.cheese.api

import argonaut._, Argonaut._
import org.jsoup.Jsoup
import org.jsoup.select.Elements

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
    def getLine(begin: String, elements: Elements): String = {
      for (i <- 0 until elements.size()) {
        if (elements.get(i).text().startsWith(begin))
          return fromColon(elements.get(i).text())
      }
      ""
    }
    val listElements = summary.getElementsByTag("ul").get(0).getElementsByTag("li")

    CheeseInfo(name, description.mkString("\n"),
      getLine("Region", listElements),
      getLine("Family", listElements),
      getLine("Rind", listElements),
      getLine("Colour", listElements),
      getLine("Aroma", listElements),
      getLine("Producers", listElements).split(",").toList
    )
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
                      val description: String,
                      val region: String,
                      val family: String,
                      val rind: String,
                      val colour: String,
                      val aroma: String,
                      val producers: List[String]) {
  def toJson = Json(
    "name"        := name,
    "description" := description,
    "region"      := region,
    "family"      := family,
    "rind"        := rind,
    "colour"      := colour,
    "aroma"       := aroma,
    "producers"   := producers
  ).toString()
}
