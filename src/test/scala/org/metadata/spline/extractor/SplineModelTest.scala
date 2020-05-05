package org.metadata.spline.extractor

import org.metadata.cdm.model.CDMModel
import org.metadata.marquez.model.{CreateDataset, CreateSource}
import org.metadata.spline.extractor.SplineModel.{extract, extractMetadataFromFile, printSplineModel}
import org.metadata.cdm.operations._


object SplineModelTest {
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
}
