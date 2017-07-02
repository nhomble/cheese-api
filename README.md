# cheese-api
pull stuff off of cheese.com for some apps

```scala
package org.hombro.cheese
object Example extends App {
  val client = CheeseClient()
  val cheese = client.getCheese(startingWith = "a")
  print(cheese)
}
```