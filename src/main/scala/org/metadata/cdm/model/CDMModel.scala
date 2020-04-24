package org.metadata.cdm.model

import org.metadata.cdm.model
import org.metadata.cdm.operations.Entities

case class CDMModel(name: String, version: String, entities: Entities)

case class Partition(name: String,
                     description: Option[String],
                     refreshTime: Option[String],
                     location: Option[String],
                     isHidden: Option[Boolean])

object CDMModel {
  import org.metadata.cdm.operations._
  import Entity._
  import io.circe._
  import io.circe.generic.semiauto._

  implicit val decodeCDMModel: Decoder[CDMModel] = (c: HCursor) =>
    for {
      name <- c.downField("name").as[String]
      version <- c.downField("version").as[String]
      entities <- c.downField("entities").as[Entities]
    } yield {
      model.CDMModel(name, version, entities)
    }

  implicit val encodeCDMModel: Encoder[CDMModel] = deriveEncoder[CDMModel]
}