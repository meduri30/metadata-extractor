package org.metadata.spline.extractor

import io.circe.{Decoder, Json}
import org.metadata.cdm.model.{CDMModel, LocalEntity, ReferenceEntity}
import org.metadata.cdm.operations._

case class SplineModel(writeOperation: WriteOperation,
                       readOperation: ReadOperation,
                       extraInfo: ExtraInfo) {
  def toCDM: CDMModel = {
    // readOperation -> references; writeOperation -> local
    // either -> left: local ; right: reference
    val appName: String = extraInfo.appName
    val references: Array[Either[LocalEntity, ReferenceEntity]] =
      readOperation.toReferences
    val localEntity: Either[LocalEntity, ReferenceEntity] =
      writeOperation.toLocalEntity
    val entities: Array[Either[LocalEntity, ReferenceEntity]] =
      references ++ Array(localEntity)
    CDMModel(name = appName, version = "1.0", entities)
  }
}

object SplineModel {

  def apply(jsonBody: String): Option[SplineModel] = {
    for {
      splineOutputJSON <- Some(parseJsonFromString(jsonBody))
      extraInfo <- splineOutputJSON.to[ExtraInfo]
      writeOperation <- extract[WriteOperation](splineOutputJSON, extraInfo)
      readOperation <- extract[ReadOperation](splineOutputJSON, extraInfo)
    } yield {
      SplineModel(writeOperation, readOperation, extraInfo)
    }
  }

  def extractMetadataFromFile(fileURL: String): Option[Json] = {
    Some(extractJSONFromURL(fileURL))
  }

  def extract[A <: Operations](splineOutputJSON: Json,
                               extraInfo: ExtraInfo)
                              (implicit d: Decoder[A]): Option[A] = {
    splineOutputJSON.to[A] match {
      case None => None
      case Some(value) =>
        Some(value.updateWithColumnInfo(extraInfo).asInstanceOf[A])
    }
  }

  def printSplineModel(splineModelOpt: Option[SplineModel]): Unit = {
    splineModelOpt.fold(println(s"Something went wrong!")) {
      case SplineModel(writeOperation, readOperation, extraInfo) =>
        println("Extra Information!")
        extraInfo
          .columns
          .foreach(columnInfo => println(s"Column Information: $columnInfo"))
        println("Write Operation!")
        writeOperation.schema.foreach(x => println(x.toString))
        println("Read Operations!")
        readOperation.inputSources.foreach(x => {
          println(s"Input Source: ${x.inputSource.mkString(";")}")
          println(s"Format: ${x.sourceType}")
          x.schema.foreach(x => println(x.toString))
        })
    }
  }
}


