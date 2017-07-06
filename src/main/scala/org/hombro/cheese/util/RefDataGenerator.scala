package org.hombro.cheese.util

import org.hombro.cheese.{CheeseClient, WikiClient}

/**
  * Created by nicolas on 7/5/2017.
  */
object RefDataGenerator extends App {
  val client = CheeseClient()
  val enricher = WikiClient()

  val cheeses = client.getCheeseNames(startingWith = "s").par.toSet
  val info = cheeses flatMap (cheese => client.getCheeseInfo(cheese)) map enricher.enrichCheeseInfo
  val filtered = info filter (info => info.wikiLink.isDefined && info.imgLink.isDefined)
  val cheeseToInfo = filtered.groupBy[String](_.name)
}
