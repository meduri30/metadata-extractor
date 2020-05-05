package org.metadata.spline.extractor

import java.nio.file.FileSystems

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, scaladsl}
import akka.stream.alpakka.file.DirectoryChange
import akka.stream.scaladsl.Sink

import scala.concurrent.ExecutionContext.Implicits.global
import org.metadata.cdm.model.CDMModel
import org.metadata.marquez.model.{CreateDataset, CreateSource, MarquezOps}

import scala.concurrent.{Await, Future}
import scala.io.Source
import scala.util.{Failure, Success}

object SplineService {
  // As of now, this service will only listen to file changes
  // Ideally, this should be a REST service where spline-agent can post captured metadata
  def main(args: Array[String]): Unit = {
    println(s"Starting to listen for new metadata!")
    import akka.stream.alpakka.file.scaladsl.DirectoryChangesSource

    import scala.concurrent.duration._

    implicit val system: ActorSystem = ActorSystem("spline-service")
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    val fs = FileSystems.getDefault
    val changes = DirectoryChangesSource(
      fs.getPath("/Users/aparna/myStuff/spline_output"),
      pollInterval = 10.second,
      maxBufferSize = 1000)
    changes.runForeach {
      case (path, DirectoryChange.Creation) => {
        println("Found Metadata at : " + path)
        push2Marquez(s"file://${path.toString}")
        println("Pushing to Marquez")
      }
    } onComplete {
      case Success(value) => println(s"Is computation done? ${value.toString}")
      case Failure(exception) => println(s"If not, ${exception.getMessage}")
    }
  }

  def push2Marquez(filePath: String): Unit = {
    val rawSource = Source.fromURL(filePath)
    println(s"Converting to Spline Model!")
    val splineModelOpt: Option[SplineModel] = SplineModel(rawSource.mkString)
    splineModelOpt.fold(println(s"Something went wrong during conversion!"))(splineModel => {
      val cdmModel: CDMModel = splineModel.toCDM
      println(s"Creating Source ...")
      MarquezOps.createSource(cdmModel)
      println(s"Creating Dataset ...")
      MarquezOps.createDataset(cdmModel)
    })
  }
}
