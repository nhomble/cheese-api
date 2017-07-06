package org.hombro.cheese

import org.hombro.cheese.api.CheeseInfo
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

import scala.collection.immutable.IndexedSeq
import scalaj.http.Http

/**
  * Created by nicolas on 7/4/2017.
  */
object WikiClient {
  private def webPage() = Http("https://en.wikipedia.org/wiki/List_of_cheeses").asString.body

  private def parsePage() = {
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

    def grabFromWikiTable(table: Element): IndexedSeq[Option[(String, Option[String])]] = {
      val rows = soup.getElementsByTag("tr")
      for (i <- 0 until rows.size()) yield {
        if (rows.get(i).getElementsByTag("td").size() > 0) {
          val td = rows.get(i).getElementsByTag("td").get(0)
          Some(parseName(td))
        }
        else {
          None
        }
      }
    }

    val out: IndexedSeq[IndexedSeq[(String, Option[String])]] = for (i <- 0 until wikitables.size()) yield {
      grabFromWikiTable(wikitables.get(i))
    }.flatten

    val names: List[String] = out.flatMap(l => l.map(t => t._1)).toList
    val nameToLink: Map[String, Option[String]] = out.flatMap(l => l.map(t => t._1 -> t._2)).toMap
    (names, nameToLink)
  }

  def apply() = {
    val (cheeseList, nameToLink) = parsePage()
    new WikiClient(cheeseList, nameToLink)
  }
}

case class WikiClient private(val cheeseList: List[String], val nameToLink: Map[String, Option[String]]) extends CheeseAPI with CheeseEnricher {
  override def getCheeseNames(startingWith: String = "") = if (startingWith.isEmpty) cheeseList else cheeseList.filter(_.startsWith(startingWith))

  def wikiLink(cheeseName: String) = nameToLink.getOrElse(cheeseName.toLowerCase(), nameToLink.getOrElse(cheeseName.toLowerCase() + " org", None))

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