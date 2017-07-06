package org.hombro.cheese.api

import argonaut.Argonaut._
import argonaut._

/**
  * Created by nicolas on 7/1/2017.
  */
case class CheeseInfo(val name: String,
                      val description: String,
                      val region: String,
                      val family: String,
                      val rind: String,
                      val colour: String,
                      val aroma: List[String],
                      val producers: List[String],
                      val wikiLink: Option[String]) {
  def toJson = {
    val _wikiLink = if (wikiLink.isDefined) wikiLink.get else ""
    Json(
      "name" := name,
      "description" := description,
      "region" := region,
      "family" := family,
      "rind" := rind,
      "colour" := colour,
      "aroma" := aroma,
      "producers" := producers,

      "hasWiki" := _wikiLink.nonEmpty,
      "wikiLink" := _wikiLink
    ).toString()
  }
}