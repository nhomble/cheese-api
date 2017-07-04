package org.hombro.cheese

import org.hombro.cheese.api.CheeseInfo

/**
  * Created by nicolas on 7/4/2017.
  */
case class WikiClient() extends CheeseAPI {
  private val baseUrl = "https://en.wikipedia.org/wiki/List_of_cheeses"

  override def getCheeseNames(startingWith: String): List[String] = ???

  override def getCheeseInfo(cheeseName: String): CheeseInfo = ???
}