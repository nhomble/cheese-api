package org.hombro.cheese

import org.hombro.cheese.api.CheeseInfo
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scalaj.http.Http

/**
  * Created by nicolas on 7/4/2017.
  */
object WikiClient {
  private def webPage() = Http("https://en.wikipedia.org/wiki/List_of_cheeses").asString.body

  private def parsePage() = {
    val names = ListBuffer[String]()
    val nameToLink = mutable.Map[String, Option[String]]()

    val soup = Jsoup.parse(webPage())
    val wikitables = soup.getElementsByClass("wikitable")

    def parseName(td: Element): (String, Option[String]) = {
      if (td.getElementsByTag("a").size() == 0)
        (td.text.toLowerCase(), None)
      else {
        val name = td.getElementsByTag("a").get(0).text()
        val a = td.getElementsByTag("a").get(0).getElementsByAttribute("href")
        val href =
          if (a.size() == 0) None
          else {
            if (a.get(0).attr("href").contains("redlink=1")) None else Some("https://en.wikipedia.org" + a.get(0).attr("href"))
          }
        (name.toLowerCase(), href)
      }
    }

    def grabFromWikiTable(table: Element) = {
      val rows = soup.getElementsByTag("tr")
      for (i <- 0 until rows.size()) {
        if (rows.get(i).getElementsByTag("td").size() > 0) {
          val td = rows.get(i).getElementsByTag("td").get(0)
          val (name, link) = parseName(td)
          names += name
          nameToLink += (name -> link)
        }
      }
    }

    for (i <- 0 until wikitables.size()) {
      grabFromWikiTable(wikitables.get(i))
    }
    (names.result(), nameToLink.toMap)
  }

  def apply() = {
    val (cheeseList, nameToLink) = parsePage()
    new WikiClient(cheeseList, nameToLink)
  }
}

case class WikiClient private(val cheeseList: List[String], val nameToLink: Map[String, Option[String]]) extends CheeseAPI with CheeseEnricher {
  override def getCheeseNames(startingWith: String = "") = if (startingWith.isEmpty) cheeseList else cheeseList.filter(_.startsWith(startingWith))

  def wikiLink(cheeseName: String) = nameToLink.getOrElse(cheeseName.toLowerCase(), nameToLink.getOrElse(cheeseName.toLowerCase() + " cheese", None))

  override def enrichCheeseInfo(info: CheeseInfo): CheeseInfo = CheeseInfo(
    info.name,
    info.description,
    info.region,
    info.family,
    info.rind,
    info.colour,
    info.aroma,
    info.producers,
    wikiLink(info.name)
  )
}