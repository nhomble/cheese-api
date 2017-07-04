package org.hombro.cheese

import org.hombro.cheese.api.CheeseInfo

/**
  * Created by nicolas on 7/1/2017.
  */
trait CheeseAPI {
  def getCheeseNames(startingWith: String): List[String]

  def getCheeseInfo(cheeseName: String): CheeseInfo
}