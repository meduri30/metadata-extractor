package org.metadata.marquez.model

import org.metadata.cdm.model.CDMModel
import sttp.model.Uri

trait MarquezOps {

}

object MarquezOps {
  import sttp.client._

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

  def getJsonBody(targetURL: Uri, jsonBody: Option[String]): Either[String, String] = {
    val request: Request[Either[String, String], Nothing] =
      jsonBody.fold(basicRequest)(body => basicRequest.body(body))
        .header("Content-Type", "application/json")
        .get(targetURL)
    implicit val backend: SttpBackend[Identity, Nothing, NothingT] =
      HttpURLConnectionBackend()
    val response: Identity[Response[Either[String, String]]] = request.send()
    response.body
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

  def createSource(cdmModel: CDMModel): Unit = {
    val createSource: CreateSource = CreateSource(cdmModel)
    val targetURL: Uri =
      uri"http://localhost:5000/api/v1/sources/${createSource.sourceName}"
    putJsonBody(targetURL, createSource.jsonBody)
  }
}
