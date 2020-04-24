package org.metadata.spline.extractor

import org.metadata.cdm.model.{Attribute => CDMAttribute}

case class DataType(id: String, name: String, nullable: Boolean)

case class Attribute(id: String, name: String, dataTypeId: String)

case class ColumnInfo(id: String,
                      name: String,
                      dataType: String,
                      isNullable: Boolean) {
  def toAttribute: CDMAttribute = {
    CDMAttribute(name, dataType, None, None)
  }
}

case class ExtraInfo(appName: String, columns: Array[ColumnInfo])

object ExtraInfo {

  import io.circe.generic.semiauto.deriveDecoder
  import io.circe.{Decoder, HCursor}

  implicit val dataTypeDecoder: Decoder[DataType] = deriveDecoder[DataType]
  implicit val attributeDecoder: Decoder[Attribute] = deriveDecoder[Attribute]
  implicit val decodeAttributeAndDataType:
    Decoder[ExtraInfo] =
    (c: HCursor) => {
      val extraInfo = c.downField("extraInfo")
      for {
        appName <- extraInfo.downField("appName").as[String]
        dataTypes <- extraInfo.downField("dataTypes").as[Array[DataType]]
        attributes <- extraInfo.downField("attributes").as[Array[Attribute]]
      } yield {
        ExtraInfo(appName, joinDataTypeAndAttribute(dataTypes, attributes))
      }
    }

  private def joinDataTypeAndAttribute(dataTypes: Array[DataType],
                                       attributes: Array[Attribute]):
  Array[ColumnInfo] = {
    val dataTypeArray: Array[(String, DataType)] =
      dataTypes.map(x => (x.id, x))
    val attributeArray: Array[(String, Attribute)] =
      attributes.map(x => (x.dataTypeId, x))
    for {
      (id, dataType) <- dataTypeArray
      (`id`, attribute) <- attributeArray
    } yield {
      ColumnInfo(
        id = attribute.id,
        name = attribute.name,
        dataType = dataType.name,
        isNullable = dataType.nullable
      )
    }
  }
}