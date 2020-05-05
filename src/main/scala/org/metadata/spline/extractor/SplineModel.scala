package org.metadata.spline.extractor

import io.circe.{Decoder, Json}
import org.metadata.cdm.model.{CDMModel, LocalEntity, ReferenceEntity}
import org.metadata.cdm.operations._
import org.metadata.marquez.model.{CreateDataset, CreateSource, MarquezOps}

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

  def main(args: Array[String]): Unit = {
    import Operations._

    val extractedMetadataFile: String = "file:////Users/aparna/myStuff/myProjects/metadata-extractor/metadata-extractor/src/main/resources/SampleJSON_3.json"
    val splineModelOpt: Option[SplineModel] = for {
      splineOutputJSON <- extractMetadataFromFile(extractedMetadataFile)
      extraInfo <- splineOutputJSON.to[ExtraInfo]
      writeOperation <- extract[WriteOperation](splineOutputJSON, extraInfo)
      readOperation <- extract[ReadOperation](splineOutputJSON, extraInfo)
    } yield {
      SplineModel(writeOperation, readOperation, extraInfo)
    }
    printSplineModel(splineModelOpt)
    println(s"Converting to CDM and printing!")
    import CDMModel._
    import io.circe.syntax._
    splineModelOpt.fold(println(s"something went wrong!"))(splineModel => {
      val cdmModel: CDMModel = splineModel.toCDM
      println(cdmModel.asJson)
      println(s"printing Marquez model!")
      val createDataset: CreateDataset = CreateDataset(cdmModel)
      println(createDataset)
      val createSource: CreateSource = CreateSource(cdmModel)
      println(createSource)
      println(s"Sending metadata to Marquez!")
//      MarquezOps.createSource(cdmModel)
//      MarquezOps.createDataset(cdmModel)
      //      MarquezOps.createJob(cdmModel)
    })
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


