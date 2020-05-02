package org.metadata.marquez.model

import io.circe.Decoder.Result
import io.circe.{Decoder, Json}
import org.metadata.cdm.model.CDMModel
import org.metadata.cdm.operations._
import sttp.model.Uri

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
  import sttp.client._

  implicit val decodeGetSource: Decoder[GetSource] = deriveDecoder[GetSource]

  def extractGetSourceResponse(body: String): Unit = {
    val responseJson: Json = parseJsonFromString(jsonString = body)
    responseJson.as[GetSource].fold(error => {
      println(s"Error Message: ${error.message}")
    },
      success => GetSource.extractModel(success))
  }

  private def putJsonBody(targetURL: Uri, jsonBody: String) = {
    val request: Request[Either[String, String], Nothing] =
      basicRequest
        .body(jsonBody)
        .header("Content-Type", "application/json")
        .put(targetURL)
    implicit val backend: SttpBackend[Identity, Nothing, NothingT] =
      HttpURLConnectionBackend()
    val response: Identity[Response[Either[String, String]]] = request.send()
    println(s"Response: ${response.statusText}")
    println(s"Response Body:")
    response.body.fold(x => println(x), y => println(y))
  }

  def createDataset(cdmModel: CDMModel): Unit = {
    val createDataset: CreateDataset = CreateDataset(cdmModel)
    val (namespace, dataset) = (createDataset.namespace, createDataset.dataset)
    val targetURL: Uri =
      uri"http://localhost:5000/api/v1/namespaces/$namespace/datasets/$dataset"
    putJsonBody(targetURL, createDataset.jsonBody)
  }

  def createJob(cdmModel: CDMModel): Unit = {
    val createJob: CreateJob = CreateJob(cdmModel)
    val (namespace, jobName) = (createJob.namespace, createJob.jobName)
    val targetURL: Uri =
      uri"http://localhost:5000/api/v1/namespaces/$namespace/jobs/$jobName"
    putJsonBody(targetURL, createJob.jsonBody)
  }
}
