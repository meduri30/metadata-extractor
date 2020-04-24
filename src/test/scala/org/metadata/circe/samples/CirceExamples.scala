package org.metadata.circe.samples

import io.circe.{Encoder, Json}

case class Item(id: Int, description: String, quantity: Int)
case class ContactDetails(address: String, phone: String)
case class Customer(name: String, contactDetails: ContactDetails)
case class Order(customer: Customer, items: Array[Item], total: Double)

case class Item2(id2: Int, description2: String, quantity2: Int)

object Order {
  import io.circe._
  import io.circe.generic.semiauto._

  implicit val itemDecoder: Decoder[Item] = deriveDecoder[Item]
  implicit val contactDetailsDecoder: Decoder[ContactDetails] = deriveDecoder[ContactDetails]
  implicit val customerDecoder: Decoder[Customer] = deriveDecoder[Customer]
  implicit val orderDecoder: Decoder[Order] = (c: HCursor) => {
    for {
      customer <- c.downField("order").downField("customer").as[Customer]
      items <- c.downField("order").downField("items").as[Array[Item]]
      total <- c.downField("order").downField("total").as[Double]
    } yield {
      Order(customer, items, total)
    }
  }
  implicit val itemEncoder: Encoder[Item] = deriveEncoder[Item]
//  implicit val itemEncoder: Encoder[Item] = {
//    Encoder[Item2].contramap[Item](x => Item2(x.id, x.description, x.quantity))
//  }
  implicit val contactDetailsEncoder: Encoder[ContactDetails] = deriveEncoder[ContactDetails]
  implicit val customerEncoder: Encoder[Customer] = deriveEncoder[Customer]

  implicit val item2Encoder: Encoder[Item2] = deriveEncoder[Item2]
}

object CirceExamples {
  def main(args: Array[String]): Unit = {
    import Order._
    import org.metadata.cdm.operations._
    val json: Json = getJSON
    val order: Option[Order] = json.to[Order]
    order.fold(println("Something went wrong!"))(x => println(s"Order: $x"))
    import io.circe.syntax._
    println("Testing!")
    order.fold(println("==>"))(x => println(x.customer.asJson))
    val item2: Item2 = order.fold(Item2(0, "", 0))(x => {
      val item = x.items(0)
      Item2(item.id, item.description, item.quantity)
    })
    println(s"Item2: ${item2.asJson}")
    implicit val itemEncoder: Encoder[Item] = {
      Encoder[Item2].contramap[Item](x => Item2(x.id, x.description, x.quantity))
    }
    println(s"Converting to Item2")
    val convertibleItem: Item = order.fold(Item(0, "", 0))(x => x.items(0))
    println(s"Item: ${convertibleItem.asJson}")
  }

  def filterCursors(json: Json): Unit = {
    val itemsFromCursor: Vector[Json] = json.hcursor.
      downField("order").
      downField("items").
      focus.
      flatMap(_.asArray).
      getOrElse(Vector.empty)
    val filteredJSON = itemsFromCursor.filter(_.hcursor.get[Int]("id").contains(456))
    filteredJSON.foreach(x => println(x.toString()))
  }

  def getJSON: Json = {
    import io.circe._
    import io.circe.parser._

    parse(
      """
{
  "order": {
    "customer": {
      "name": "Custy McCustomer",
      "contactDetails": {
        "address": "1 Fake Street, London, England",
        "phone": "0123-456-789"
      }
    },
    "items": [{
      "id": 123,
      "description": "banana",
      "quantity": 1
    }, {
      "id": 456,
      "description": "apple",
      "quantity": 2
    }],
    "total": 123.45
  }
}
""").getOrElse(Json.Null)
  }
}
