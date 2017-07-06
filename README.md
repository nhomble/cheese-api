# cheese-api
pull stuff off of cheese.com for some apps

[![Build Status](https://travis-ci.org/nhomble/currency-layer-api.svg?branch=master)](https://travis-ci.org/nhomble/currency-layer-api)

```scala
package org.hombro.cheese
object Example extends App {
  val client = CheeseClient()
  val wiki = WikiClient()
  val info = client.getCheeseInfo("pepper jack").get
  print(wiki.enrichCheeseInfo(info).toJson)
}
```