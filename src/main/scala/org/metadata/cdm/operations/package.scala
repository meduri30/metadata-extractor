package org.metadata.cdm

import io.circe.schema.Schema
import io.circe.{Decoder, Json}
import org.metadata.cdm.model.{CDMModel, LocalEntity, ReferenceEntity}

import scala.io.Source

package object operations {

  type Entities = Array[Either[LocalEntity, ReferenceEntity]]

  implicit def decodeToEither[A,B](implicit a: Decoder[A], b: Decoder[B]):
  Decoder[Either[A,B]] = {
    val l: Decoder[Either[A,B]]= a.map(Left.apply)
    val r: Decoder[Either[A,B]]= b.map(Right.apply)
    l or r
  }

  def parseJsonFromString(jsonString: String): Json = {
    import io.circe.parser._
    parse(jsonString) match {
      case Right(value) => value
      case _ => Json.fromString("")
    }
  }

  def validateAgainstMasterModel(targetModel: Json): Boolean = {
    val masterModel = Schema.load(fetchModelSchema("master"))
    val validationResult = masterModel.validate(targetModel)
    println(s"Validation Result: $validationResult")
    validationResult.isValid
  }

  def fetchModelSchema(schemaType: String, modelURL: Option[String] = None): Json = {
    val sourceURL = schemaType match {
      case "master" => "https://raw.githubusercontent.com/microsoft/CDM/master/docs/schema/modeljsonschema.json"
      case "sample_ref" => "https://raw.githubusercontent.com/microsoft/CDM/master/docs/schema/examples/OrdersProductsCustomersLinked/model.json"
      case _ => "https://raw.githubusercontent.com/microsoft/CDM/master/docs/schema/examples/OrdersProducts/model.json"
    }
    extractJSONFromURL(sourceURL)
  }

  def extractJSONFromURL(sourceUrl: String): Json = {
    rawContent2Json(parseJsonFromString)(sourceUrl)
  }

  def rawContent2Json(f: String => Json)(sourceUrl: String): Json = {
    val rawSource = Source.fromURL(sourceUrl)
    val raw2Json = f(rawSource.mkString)
    rawSource.close()
    raw2Json
  }

  def extractFileNameFromPath(filePath: String): String = {
    filePath.split("/").last
  }

  def printCDMModel(model: CDMModel): Unit = {
    def printLocalEntity(localEntity: LocalEntity): Unit = {
      println(s"Local Entity Name: ${localEntity.name}")
      println("Attributes:")
      localEntity.attributes.foreach(x => println(s"${x.name} :: ${x.dataType}"))
    }
    def printReferenceEntity(referenceEntity: ReferenceEntity): Unit = {
      println(s"Reference Entity Name: ${referenceEntity.name}")
      println(s"Source Name: ${referenceEntity.source}")
    }
    println(s"Model Name: ${model.name}")
    println(s"Model Version: ${model.version}")
    model.entities.foreach { localOrReference => {
      localOrReference.fold(printLocalEntity, printReferenceEntity)
    }
    }
  }

  implicit class JsonExtension(jsonContent: Json) {
    def to[A](implicit d: Decoder[A]): Option[A] = {
      jsonContent.as[A] match {
        case Right(success) => Some(success)
        case Left(failure) => {
          println(s"Details of the failure: ${failure.message}")
          failure.printStackTrace()
          None
        }
      }
    }
  }
}
