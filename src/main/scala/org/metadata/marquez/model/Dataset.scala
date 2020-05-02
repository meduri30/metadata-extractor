package org.metadata.marquez.model

import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._
import org.metadata.cdm.model.{Attribute, CDMModel, LocalEntity}

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
    val localEntities: Array[LocalEntity] =
      cdmModel.entities.collect { case Left(value) => value }
    if (localEntities.length > 1)
      println(s"More local entities, ${localEntities.length} than expected value of 1")
    val localEntity: LocalEntity = localEntities.head
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
                              tags: Option[String],
                              description: Option[String])

object CreateDatasetField {
  implicit val encodeDatasetField: Encoder.AsObject[CreateDatasetField] =
    deriveEncoder[CreateDatasetField]

  def apply(attribute: Attribute): CreateDatasetField = {
    CreateDatasetField(
      name = attribute.name,
      `type` = attribute.dataType.toUpperCase,
      tags = None,
      description = None)
  }
}
