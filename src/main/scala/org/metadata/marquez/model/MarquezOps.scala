package org.metadata.marquez.model

import io.circe.Decoder.Result
import io.circe.{Decoder, Json}
import org.metadata.cdm.model.CDMModel
import org.metadata.cdm.operations._

trait MarquezOps {

}

case class GetSource(`type`: String,
                     name: String,
                     createdAt: String,
                     updatedAt: String,
                     connectionUrl: String)
object GetSource {
  def emptySource: GetSource = GetSource("", "", "", "", "")
  def extractModel(getSource: GetSource): Unit = {
    val cdmModel: Result[CDMModel] =
      fetchModelSchema("sample_ref", Some(getSource.connectionUrl)).as[CDMModel]
    val result: Option[CDMModel] = cdmModel  match {
      case Right(value) => Some(value)
      case Left(error) => {
        println(s"Parsing failed with error, ${error.message}")
        None
      }
    }
    result.fold(println("Nothing!"))(printCDMModel)
    println(s"Printing JSON")
    import io.circe.syntax._
    result.fold(println("Nothing!"))(x => println(s"${x.asJson}"))
  }
}

object MarquezOps {
  import io.circe.generic.semiauto.deriveDecoder

  implicit val decodeGetSource: Decoder[GetSource] = deriveDecoder[GetSource]

  def extractGetSourceResponse(body: String): Unit = {
    val responseJson: Json = parseJsonFromString(jsonString = body)
    responseJson.as[GetSource].fold(error => {
      println(s"Error Message: ${error.message}")
    },
      success => GetSource.extractModel(success))
  }
}
