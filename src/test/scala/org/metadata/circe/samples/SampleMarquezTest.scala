package org.metadata.circe.samples

import org.metadata.marquez.model.{GetDataset, GetSource}

object SampleMarquezTest {

  /**
   * Make sure to run below command before testing the code
   * curl -X PUT http://localhost:5000/api/v1/sources/cdm_db \
   * -H 'Content-Type: application/json' \
   * -d '{
   * "type": "POSTGRESQL",
   * "connectionUrl": "https://raw.githubusercontent.com/microsoft/CDM/master/docs/schema/examples/OrdersProducts/model.json"
   * }'
   *
   */
  def main(args: Array[String]): Unit = {
    val baseURL = "http://localhost:5000/api/v1"
    val getSource: Option[GetSource] = GetSource(baseURL, "cdm_db")
    getSource.fold(println(s"Something went wrong!"))(x => println(s"Response: $x"))

    val getDataset: Option[GetDataset] =
      GetDataset(baseURL, "cdm_namespace", "job1_results_extractor")
    getDataset.fold(println(s"Something went wrong while getting GetDataset")){
      dataset => println(s"Get Dataset: $dataset")
    }
  }
}
