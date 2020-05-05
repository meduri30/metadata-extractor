package org.metadata.marquez.model

import java.net.URI

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import org.metadata.cdm.model.{CDMModel, LocalEntity}
import org.metadata.cdm.operations._
import sttp.model.Uri

case class CreateSource(sourceName: String, jsonBody: String)

object CreateSource {
  def apply(cdmModel: CDMModel): CreateSource = {
    val sourceBody: CreateSourceBody = CreateSourceBody(cdmModel)
    CreateSource(cdmModel.name, sourceBody.asJson.noSpaces)
  }
}

case class CreateSourceBody(`type`: String,
                            connectionUrl: String,
                            description: Option[String])

object CreateSourceBody {
  implicit val encodeCreateSourceBody: Encoder.AsObject[CreateSourceBody] =
    deriveEncoder[CreateSourceBody]

  def apply(cdmModel: CDMModel): CreateSourceBody = {
    val localEntity: LocalEntity = cdmModel.getLocalEntities.head
    val filePath: String =
      localEntity.fileInformation.fold("")(x => x.fileLocation)
    CreateSourceBody("KAFKA", filePath, None)
  }
}

case class GetSource(`type`: String,
                     name: String,
                     createdAt: String,
                     updatedAt: String,
                     connectionUrl: String,
                     description: Option[String])

object GetSource {

  implicit val decodeGetSource: Decoder[GetSource] = deriveDecoder[GetSource]

  def apply(baseURL: String, sourceName: String): Option[GetSource] = {
    val targetURL: Uri = Uri(new URI(s"$baseURL/sources/$sourceName"))
    val response: Either[String, String] = MarquezOps.getJsonBody(targetURL, None)
    response match {
      case Right(responseBody) => parseJsonFromString(responseBody).to[GetSource]
      case Left(errorMessage) => {
        println(s"Error Occurred while parsing GetSource response: $errorMessage")
        None
      }
    }
  }
}