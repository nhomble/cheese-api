package org.hombro.cheese

import org.hombro.cheese.api.CheeseInfo
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

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

  def parseImgLink(url: String) = {
    val html = Http(url).asString.body
    val soup = Jsoup.parse(html)

    def theImg(element: Elements) = {
      val img = element.get(0).getElementsByTag("img")
      if (img.size() > 1) Some("https:" + img.attr("src")) else None
    }

    val infoBox = soup.getElementsByClass("infobox")
    if (infoBox.size() > 0) {
      theImg(infoBox)
    }
    else {
      val thumb = soup.getElementsByClass("thumb")
      if(thumb.size() > 0) theImg(thumb) else None
    }
  }

  def apply() = {
    val (cheeseList, nameToLink) = parsePage()
    new WikiClient(cheeseList, nameToLink)
  }
}

case class WikiClient private(val cheeseList: List[String],
                              val nameToLink: Map[String, Option[String]]) extends CheeseAPI with CheeseEnricher {
  override def getCheeseNames(startingWith: String = "") = if (startingWith.isEmpty) cheeseList else cheeseList.filter(_.startsWith(startingWith))

  private def wikiLink(cheeseName: String) = nameToLink.getOrElse(cheeseName, nameToLink.getOrElse(cheeseName + " cheese", None))

  private def imageLink(cheeseName: String) = {
    wikiLink(cheeseName) match {
      case Some(url) => WikiClient.parseImgLink(url)
      case None => None
    }
  }

  override def enrichCheeseInfo(info: CheeseInfo): CheeseInfo = CheeseInfo(
    info.name,
    info.description,
    info.region,
    info.family,
    info.rind,
    info.colour,
    info.aroma,
    info.producers,
    info.cheeseImg,
    wikiLink = wikiLink(info.name.toLowerCase()),
    imgLink = imageLink(info.name.toLowerCase())
  )
}