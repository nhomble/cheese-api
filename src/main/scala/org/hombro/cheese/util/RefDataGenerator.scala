package org.hombro.cheese.util

import org.hombro.cheese.{CheeseClient, WikiClient}

/**
  * Created by nicolas on 7/5/2017.
  */
object RefDataGenerator extends App {
  val client = CheeseClient()
  val enricher = WikiClient()

  val cheeses = client.getCheeseNames().par.toSet
  val info = cheeses flatMap (cheese => client.getCheeseInfo(cheese)) map enricher.enrichCheeseInfo
  val filtered = info filter (info => info.wikiLink.isDefined && info.cheeseImg.isDefined)
  val cheeseToInfo = filtered.groupBy[String](_.name) mapValues (infos => infos.head)

  // UGH
  println("{")
  for((cheese, info) <- cheeseToInfo.seq){
    print("\"%s\":".format(cheese))
    print(info.toJson)
    println(",")
  }
  println("}")
}
