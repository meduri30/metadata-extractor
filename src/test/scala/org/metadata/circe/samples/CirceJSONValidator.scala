package org.metadata.circe.samples

import io.circe.Json
import org.metadata.cdm.model.CDMModel

object CirceJSONValidator {

  import org.metadata.cdm.operations._

  def main(args: Array[String]): Unit = {
    val invalidCDMContent: Json =
      parseJsonFromString("""{"type": "Polygon", "coordinates": [[0, 0], [1, 0], [1, 1], [0, 1]] }""")

    println(s"Sample validation against CDM master schema")
    val sampleModelURL: String =
      "https://raw.githubusercontent.com/microsoft/CDM/master/docs/schema/examples/OrdersProducts/model.json"
    val validModel: Json = fetchModelSchema("sample", Some(sampleModelURL))
    println(s"Result: ${validateAgainstMasterModel(validModel)}")
    println(s"Bad Result: ${validateAgainstMasterModel(invalidCDMContent)}")

    // mapping JSON to case class
    val result: Option[CDMModel] = validModel.as[CDMModel] match {
      case Right(value) => Some(value)
      case Left(error) =>
        println(s"Parsing failed with error, ${error.message}")
        None
    }
    result.fold(println("Nothing!"))(printCDMModel)
  }

}
