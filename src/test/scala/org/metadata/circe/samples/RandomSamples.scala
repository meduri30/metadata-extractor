package org.metadata.circe.samples

object RandomSamples {
  def main(args: Array[String]): Unit = {
    val l1 = List((1,"a"), (2, "b"), (3, "c"))
    val l2 = List((1,"d"), (2, "e"), (3, "f"))
    val joinedList = for {
      (k, v1) <- l1
      (`k`, v2) <- l2
    } yield {
      (k, (v1, v2))
    }
    println(joinedList)
  }
}
