package org.metadata.cdm.model

import io.circe.Decoder.Result
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder, _}
import org.metadata.cdm.operations._

sealed trait Entity {
  def name: String
  def description: Option[String]
  def annotations: Option[Array[Annotation]]
  def isHidden: Option[Boolean]
}

case class Annotation(name: String, value: Option[String])

case class FileInformation(fileFormat: String, fileLocation: String)

case class Attribute(name: String,
                     dataType: String,
                     description: Option[String],
                     annotations: Option[Array[Annotation]])

case class LocalEntity(name: String,
                       attributes: Array[Attribute],
                       description: Option[String],
                       annotations: Option[Array[Annotation]],
                       isHidden: Option[Boolean],
                       partitions: Option[Array[Partition]],
                       fileInformation: Option[FileInformation]) extends Entity

case class ReferenceEntity(name: String,
                           source: String,
                           modelId: String,
                           description: Option[String],
                           annotations: Option[Array[Annotation]],
                           isHidden: Option[Boolean]) extends Entity

object Attribute {
  implicit val annotationDecoder: Decoder[Annotation] = deriveDecoder[Annotation]
  implicit val attributeDecoder: Decoder[Attribute] = deriveDecoder[Attribute]
  implicit val fileInformationDecoder: Decoder[FileInformation] =
    deriveDecoder[FileInformation]

  implicit val annotationEncoder: Encoder[Annotation] = deriveEncoder[Annotation]
  implicit val fileInformationEncoder: Encoder[FileInformation] =
    deriveEncoder[FileInformation]
  implicit val attributeEncoder: Encoder[Attribute] = deriveEncoder[Attribute]
  implicit val partitionEncoder: Encoder[Partition] = deriveEncoder[Partition]
}

object Entity {
  import Attribute._

  implicit val encodeLocalEntity: Encoder[LocalEntity] = deriveEncoder[LocalEntity]
  implicit val encodeReferenceEntity: Encoder[ReferenceEntity] = deriveEncoder[ReferenceEntity]

  implicit val decodeLocalEntity: Decoder[LocalEntity] =
    (c: HCursor) =>
      for {
        name <- c.downField("name").as[String]
        attributes <- c.downField("attributes").as[Array[Attribute]]
      } yield {
        LocalEntity(name, attributes, None, None, None, None, None)
      }

  implicit val decodeReferenceEntity: Decoder[ReferenceEntity] =
    (c: HCursor) =>
      for {
        name <- c.downField("name").as[String]
        source <- c.downField("source").as[String]
        modelId <- c.downField("modelId").as[String]
      } yield {
        ReferenceEntity(name, source, modelId, None, None, None)
      }

  implicit val decodeEntity: HCursor => Result[Either[LocalEntity, ReferenceEntity]] =
    (c: HCursor) => {
      c.as[Either[LocalEntity, ReferenceEntity]](decodeToEither[LocalEntity, ReferenceEntity])
    }

  lazy implicit val encodeEitherEntity: Encoder.AsObject[Either[LocalEntity, ReferenceEntity]] =
    Encoder.encodeEither("local", "reference")(encodeLocalEntity, encodeReferenceEntity)
  implicit val encodeEntitiesEncoder: Encoder[Either[LocalEntity, ReferenceEntity]] = encodeEitherEntity.mapJson(json => json)
  implicit def encodeEntities(implicit encodeEither: Encoder[Either[LocalEntity, ReferenceEntity]]):
  Encoder[Entities] = new Encoder[Entities] {
    override def apply(a: Entities): Json = {
      Json.arr(a.map(x => encodeEither(x)): _*)
    }
  }
}
