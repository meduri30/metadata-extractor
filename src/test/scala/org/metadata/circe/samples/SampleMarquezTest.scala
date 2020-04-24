package org.metadata.circe.samples

import org.metadata.marquez.model.MarquezOps

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
    import sttp.client.quick._
    val responseBody = quickRequest
      .get(uri"http://localhost:5000/api/v1/sources/cdm_db")
      .send().body
    MarquezOps.extractGetSourceResponse(responseBody)
  }
}
