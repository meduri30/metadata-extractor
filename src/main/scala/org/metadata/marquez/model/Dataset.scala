package org.metadata.marquez.model

import java.net.URI

import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._
import org.metadata.cdm.model.{Attribute, CDMModel, LocalEntity}
import org.metadata.cdm.operations.{parseJsonFromString, _}
import sttp.model.Uri

case class CreateDataset(namespace: String,
                         dataset: String,
                         jsonBody: String)

object CreateDataset {
  def apply(cdmModel: CDMModel): CreateDataset = {
    val datasetBody: CreateDatasetBody = CreateDatasetBody(cdmModel)
    CreateDataset(
      namespace = "cdm_namespace",
      dataset = datasetBody.physicalName,
      jsonBody = datasetBody.asJson.noSpaces
    )
  }
}

case class CreateDatasetBody(`type`: String,
                             physicalName: String,
                             sourceName: String,
                             fields: Array[CreateDatasetField],
                             tags: Option[String],
                             description: Option[String],
                             runId: Option[String])

object CreateDatasetBody {
  implicit val encodeDatasetBody: Encoder.AsObject[CreateDatasetBody] =
    deriveEncoder[CreateDatasetBody]

  def apply(cdmModel: CDMModel): CreateDatasetBody = {
    val localEntity: LocalEntity = cdmModel.getLocalEntities.head
    val fields: Array[CreateDatasetField] =
      localEntity.attributes.map(CreateDatasetField(_))
    CreateDatasetBody(
      `type` = "DB_TABLE",
      physicalName = localEntity.name,
      sourceName = cdmModel.name,
      fields = fields,
      tags = None,
      description = None,
      runId = None)
  }
}

case class CreateDatasetField(name: String,
                              `type`: String,
                              tags: Array[String],
                              description: Option[String])

object CreateDatasetField {
  implicit val encodeDatasetField: Encoder.AsObject[CreateDatasetField] =
    deriveEncoder[CreateDatasetField]
  implicit val decodeCreateDatasetField: Decoder[CreateDatasetField] =
    deriveDecoder[CreateDatasetField]

  def apply(attribute: Attribute): CreateDatasetField = {
    CreateDatasetField(
      name = attribute.name,
      `type` = attribute.dataType.toUpperCase,
      tags = Array.empty[String],
      description = None)
  }
}

case class GetDataset(`type`: String,
                      name: String,
                      physicalName: String,
                      createdAt: String,
                      updatedAt: String,
                      sourceName: String,
                      fields: Array[CreateDatasetField],
                      tags: Array[String],
                      lastModifiedAt: Option[String],
                      description: Option[String])

object GetDataset {
  implicit val decodeGetDataset: Decoder[GetDataset] = deriveDecoder[GetDataset]

  def apply(baseURL: String, namespace: String, datasetName: String): Option[GetDataset] = {
    val targetURL: Uri =
      Uri(new URI(s"$baseURL/namespaces/$namespace/datasets/$datasetName"))
    val response: Either[String, String] = MarquezOps.getJsonBody(targetURL, None)
    response match {
      case Right(responseBody) =>
        parseJsonFromString(responseBody).to[GetDataset]
      case Left(errorMessage) =>
        println(s"Error Occurred while parsing GetSource response: $errorMessage")
        None
    }
  }
}