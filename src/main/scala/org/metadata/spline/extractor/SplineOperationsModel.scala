package org.metadata.spline.extractor

import org.metadata.cdm.model.{LocalEntity, ReferenceEntity}
import org.metadata.cdm.operations._
import io.circe.{ACursor, Json}

sealed trait Operations {
  type self <: Operations
  def updateWithColumnInfo(extraInfo: ExtraInfo): self
}

case class WriteOperation(outputSource: String,
                          append: Boolean,
                          destinationType: String,
                          schemaIds: Array[String],
                          schema: Array[ColumnInfo] = Array.empty[ColumnInfo])
  extends Operations {
  override type self = WriteOperation
  def updateWithColumnInfo(extraInfo: ExtraInfo): WriteOperation = {
    val schemaIdsSet = schemaIds.toSet
    val updatedSchema: Array[ColumnInfo] =
      extraInfo.columns.collect { case x if schemaIdsSet(x.id) => x }
    WriteOperation(outputSource, append, destinationType, schemaIds, updatedSchema)
  }

  def toLocalEntity: Either[LocalEntity, ReferenceEntity] = {
    val fileName: String = extractFileNameFromPath(outputSource)
    Left(
      LocalEntity(
        fileName,
        schema.map(x => x.toAttribute),
        None,
        None,
        Some(false),
        None)
    )
  }
}

case class InputSourceInfo(inputSource: Array[String],
                           sourceType: String,
                           schemaIds: Array[String],
                           schema: Array[ColumnInfo] = Array.empty[ColumnInfo]) {
  def updateWithColumnInfo(extraInfo: ExtraInfo): InputSourceInfo = {
    val schemaIdsSet = schemaIds.toSet
    val updatedSchema: Array[ColumnInfo] =
      extraInfo.columns.collect { case x if schemaIdsSet(x.id) => x }
    InputSourceInfo(inputSource, sourceType, schemaIds, updatedSchema)
  }

  def toReference: Either[LocalEntity, ReferenceEntity] = {
    val fileName: String =
      inputSource.map(extractFileNameFromPath).mkString(" & ")
    val source: String = inputSource.mkString(" & ")
    val referenceEntity: ReferenceEntity =
      ReferenceEntity(fileName, source, "", None, None, Some(false))
    Right(referenceEntity)
  }
}

case class ReadOperation(inputSources: Array[InputSourceInfo]) extends Operations {
  override type self = ReadOperation
  def updateWithColumnInfo(extraInfo: ExtraInfo): ReadOperation = {
    ReadOperation(inputSources.map(_.updateWithColumnInfo(extraInfo)))
  }

  def toReferences: Array[Either[LocalEntity, ReferenceEntity]] = {
    inputSources.map(_.toReference)
  }
}

object Operations {

  import io.circe.{Decoder, HCursor}

  implicit val decodeWriteOperation: Decoder[WriteOperation] =
    (c: HCursor) => {
      val writeOperation = c.downField("operations").downField("write")
      for {
        childIds <- writeOperation.downField("childIds").as[Array[Int]]
        outputSource <- writeOperation.downField("outputSource").as[String]
        append <- writeOperation.downField("append").as[Boolean]
        destinationType <-
          writeOperation.downField("extra").downField("destinationType").as[String]
      } yield {
        val schemaIds: Array[String] =
          fetchSchemaDetailsFromChildIds(
            childIds,
            c.downField("operations").downField("other")
          )
        WriteOperation(outputSource, append, destinationType, schemaIds)
      }
    }

  implicit val decodeInputSourceInfo: Decoder[InputSourceInfo] =
    (c: HCursor) => {
      for {
        inputSource <- c.downField("inputSources").as[Array[String]]
        sourceType <- c.downField("extra").downField("sourceType").as[String]
        schemaIds <- c.downField("schema").as[Array[String]]
      } yield {
        InputSourceInfo(inputSource, sourceType, schemaIds)
      }
    }

  implicit val decodeReadOperation: Decoder[ReadOperation] =
    (c: HCursor) => {
      for {
        inputSource <-
          c.downField("operations").downField("reads").as[Array[InputSourceInfo]]
      } yield {
        ReadOperation(inputSource)
      }
    }

  def fetchSchemaDetailsFromChildIds(childIds: Array[Int],
                                     otherCursor: ACursor):
  Array[String] = {
    val otherItems: Vector[Json] =
      otherCursor.focus.flatMap(_.asArray).getOrElse(Vector.empty)
    val filteredOtherItems: Array[Json] =
      childIds.flatMap(x => otherItems.filter(_.hcursor.get[Int]("id").contains(x)))
    filteredOtherItems
      .map(_.hcursor.downField("schema").as[Array[String]])
      .flatMap(_.getOrElse(Array.empty[String]))
  }
}
