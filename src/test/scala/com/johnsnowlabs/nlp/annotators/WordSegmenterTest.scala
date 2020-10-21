package com.johnsnowlabs.nlp.annotators

import java.nio.file.{Files, Paths}

import com.johnsnowlabs.nlp.AnnotatorType.TOKEN
import com.johnsnowlabs.nlp.annotators.sbd.pragmatic.SentenceDetector
import com.johnsnowlabs.nlp.{Annotation, DocumentAssembler, SparkAccessor}
import org.apache.spark.ml.Pipeline
import org.apache.spark.sql.Dataset
import org.scalatest.FlatSpec

import scala.collection.mutable

class WordSegmenterTest extends FlatSpec {

  import SparkAccessor.spark.implicits._

  private val documentAssembler = new DocumentAssembler()
    .setInputCol("text")
    .setOutputCol("document")

  private val sentence = new SentenceDetector()
    .setInputCols("document")
    .setOutputCol("sentence")

  private val wordSegmenter = new WordSegmenterApproach()
    .setInputCols("sentence")
    .setOutputCol("token")
    .setMaxWordLength(4)
    .setMinAggregation(1.2)
    .setMinEntropy(0.4)
    .setWordSegmentMethod("ALL")

  private val wordSegmenterWithKnowledgeFile = new WordSegmenterApproach()
    .setInputCols("sentence")
    .setOutputCol("token")
    .setKnowledgeBase("src/test/resources/tokenizer/sample_chinese_doc.txt")
    .setMaxWordLength(2)
    .setMinAggregation(1.2)
    .setMinEntropy(0.4)
    .setWordSegmentMethod("ALL")

  "A WordSegmenter" should "tokenize Chinese text" in {
    val trainDataSet = Seq("永和四永和服装四服装", "永和不是服装", "服装不是永和", "饰品四饰品",
      "有限公司十有限公司", "有限公司十有限公司").toDS.toDF("text")
    val testDataSet = Seq("永和服装饰品有限公司", "永和饰品").toDS.toDF("text")
    val expectedResult = Array(Seq(
      Annotation(TOKEN, 0, 1, "永和", Map("sentence" -> "0")),
      Annotation(TOKEN, 2, 3, "服装", Map("sentence" -> "0")),
      Annotation(TOKEN, 4, 5, "饰品", Map("sentence" -> "0")),
      Annotation(TOKEN, 6, 9, "有限公司", Map("sentence" -> "0"))
    ),
      Seq(
        Annotation(TOKEN, 0, 1, "永和", Map("sentence" -> "0")),
        Annotation(TOKEN, 2, 3, "饰品", Map("sentence" -> "0"))
      )
    )

    val pipeline = new Pipeline().setStages(Array(documentAssembler, sentence, wordSegmenter))
    val tokenizerPipeline = pipeline.fit(trainDataSet)
    val tokenizerDataSet = tokenizerPipeline.transform(testDataSet)

    val actualResult = getActualResult(tokenizerDataSet)
    assertAnnotations(expectedResult, actualResult)
  }

  it should "tokenize Japanese text" in {
    val trainDataSet = Seq("音楽当時音楽 音楽 建築 数学 幾何学 解剖学 生理学 数学当時数学 生理学当時生理学")
      .toDS.toDF("text")
    val testDataSet = Seq("音楽数学生理学").toDS.toDF("text")
    val expectedResult = Array(Seq(
      Annotation(TOKEN, 0, 1, "音楽", Map("sentence" -> "0")),
      Annotation(TOKEN, 2, 3, "数学", Map("sentence" -> "0")),
      Annotation(TOKEN, 4, 6, "生理学", Map("sentence" -> "0"))
    ))

    val pipeline = new Pipeline().setStages(Array(documentAssembler, sentence, wordSegmenter))
    val tokenizerPipeline = pipeline.fit(trainDataSet)
    val tokenizerDataSet = tokenizerPipeline.transform(testDataSet)

    val actualResult = getActualResult(tokenizerDataSet)
    assertAnnotations(expectedResult, actualResult)
  }

  it should "tokenize Korean text" in {
    val trainDataSet = Seq("피부색한국피부색,피부색,성,언어성언어,언어,종교,종교한국종교").toDS.toDF("text")
    val testDataSet = Seq("피부색성언어종교").toDS.toDF("text")
    val expectedResult = Array(Seq(
      Annotation(TOKEN, 0, 2, "피부색", Map("sentence" -> "0")),
      Annotation(TOKEN, 3, 3, "성", Map("sentence" -> "0")),
      Annotation(TOKEN, 4, 5, "언어", Map("sentence" -> "0")),
      Annotation(TOKEN, 6, 7, "종교", Map("sentence" -> "0"))
    ))

    val pipeline = new Pipeline().setStages(Array(documentAssembler, sentence, wordSegmenter))
    val tokenizerPipeline = pipeline.fit(trainDataSet)
    val tokenizerDataSet = tokenizerPipeline.transform(testDataSet)

    val actualResult = getActualResult(tokenizerDataSet)
    assertAnnotations(expectedResult, actualResult)
  }

  it should "tokenize based on an external knowledge base file" in {

    val testDataSet = Seq("十四不是四十").toDS.toDF("text")
    val expectedResult = Array(Seq(
      Annotation(TOKEN, 0, 1, "十四", Map("sentence" -> "0")),
      Annotation(TOKEN, 2, 3, "不是", Map("sentence" -> "0")),
      Annotation(TOKEN, 4, 5, "四十", Map("sentence" -> "0")))
    )

    val pipeline = new Pipeline().setStages(Array(documentAssembler, sentence, wordSegmenterWithKnowledgeFile))
    val tokenizerPipeline = pipeline.fit(testDataSet)
    val tokenizerDataSet = tokenizerPipeline.transform(testDataSet)

    val actualResult = getActualResult(tokenizerDataSet)
    assertAnnotations(expectedResult, actualResult)
  }

  it should "serialize a model" in {
    val testDataSet = Seq("十四不是四十").toDS.toDF("text")

    wordSegmenterWithKnowledgeFile.fit(testDataSet).write.overwrite().save("./tmp_chinese_tokenizer")

    assertResult(true){
      Files.exists(Paths.get("./tmp_chinese_tokenizer"))
    }
  }

  it should "deserialize a model" in {
    val testDataSet = Seq("十四不是四十").toDS.toDF("text")
    val expectedResult = Array(Seq(
      Annotation(TOKEN, 0, 1, "十四", Map("sentence" -> "0")),
      Annotation(TOKEN, 2, 3, "不是", Map("sentence" -> "0")),
      Annotation(TOKEN, 4, 5, "四十", Map("sentence" -> "0")))
    )

    val wordSegmenter = WordSegmenterModel.load("./tmp_chinese_tokenizer")
    val pipeline = new Pipeline().setStages(Array(documentAssembler, sentence, wordSegmenter))
    val tokenizerPipeline = pipeline.fit(testDataSet)
    val tokenizerDataSet = tokenizerPipeline.transform(testDataSet)

    val actualResult = getActualResult(tokenizerDataSet)
    assertAnnotations(expectedResult, actualResult)
  }

  private def getActualResult(dataSet: Dataset[_]): Array[Seq[Annotation]] = {
    dataSet.select("token.result", "token.metadata", "token.begin",  "token.end").rdd.map{ row=>
      val resultSeq: Seq[String] = row.get(0).asInstanceOf[mutable.WrappedArray[String]]
      val metadataSeq: Seq[Map[String, String]] = row.get(1).asInstanceOf[mutable.WrappedArray[Map[String, String]]]
      val beginSeq: Seq[Int] = row.get(2).asInstanceOf[mutable.WrappedArray[Int]]
      val endSeq: Seq[Int] = row.get(3).asInstanceOf[mutable.WrappedArray[Int]]
      resultSeq.zipWithIndex.map{ case (token, index) =>
        Annotation(TOKEN, beginSeq(index), endSeq(index), token, metadataSeq(index))
      }
    }.collect()
  }

  private def assertAnnotations(expectedResult: Array[Seq[Annotation]], actualResult: Array[Seq[Annotation]]): Unit = {
    expectedResult.zipWithIndex.foreach { case (annotationDocument, indexDocument) =>
      val actualDocument = actualResult(indexDocument)
      annotationDocument.zipWithIndex.foreach { case (annotation, index) =>
        assert(annotation.result == actualDocument(index).result)
        assert(annotation.begin == actualDocument(index).begin)
        assert(annotation.end == actualDocument(index).end)
        assert(annotation.metadata == actualDocument(index).metadata)
      }
    }
  }

}