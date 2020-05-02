package org.metadata.marquez.model

import io.circe.Encoder
import org.metadata.cdm.model.CDMModel
import io.circe.syntax._
import io.circe.generic.semiauto._

case class CreateJob(namespace: String, jobName: String, jsonBody: String)

object CreateJob {
  def apply(cdmModel: CDMModel): CreateJob = {
    val createJobBody: CreateJobBody = CreateJobBody(cdmModel)
    CreateJob(
      namespace = "cdm_namespace",
      jobName = cdmModel.name,
      jsonBody = createJobBody.asJson.noSpaces
    )
  }
}

case class CreateJobBody(`type`: String,
                         inputs: Array[String],
                         outputs: Array[String],
                         location: Option[String],
                         context: Option[Map[String, String]],
                         description: Option[String])

object CreateJobBody {
  implicit val encodeJobBody: Encoder.AsObject[CreateJobBody] =
    deriveEncoder[CreateJobBody]

  def apply(cdmModel: CDMModel): CreateJobBody = {
    val outputs: Array[String] =
      cdmModel.entities.collect { case Left(value) => value.name }
    val inputs: Array[String] =
      cdmModel.entities.collect { case Right(value) => value.name }
    CreateJobBody("BATCH", inputs, outputs, None, None, None)
  }
}