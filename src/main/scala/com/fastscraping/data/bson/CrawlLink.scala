package com.fastscraping.data.bson

import java.util

import org.bson._
import org.bson.codecs.configuration.CodecRegistry
import org.bson.codecs.{Codec, DecoderContext, DocumentCodec, EncoderContext}
import org.bson.conversions.Bson
import play.api.libs.json.{Format, Json}
import scala.jdk.CollectionConverters._

case class CrawlLink(_link_to_crawl: String, is_crawled: Boolean) extends Bson {
  override def toBsonDocument[TDocument](documentClass: Class[TDocument], codecRegistry: CodecRegistry): BsonDocument = {
    new BsonDocumentWrapper[CrawlLink](this, codecRegistry.get(classOf[CrawlLink]))
  }

  def asMap = Map[String, AnyRef](
    "_link_to_crawl" -> _link_to_crawl.asInstanceOf[AnyRef],
    "is_crawled" -> is_crawled.asInstanceOf[AnyRef]
  )

  def asJavaMap: util.Map[String, AnyRef] = asMap.asJava
}

object CrawlLink {

  implicit val fmt: Format[CrawlLink] = Json.format[CrawlLink]


  class CrawlLinksCodec extends Codec[CrawlLink] {
    /**
     * Reference from https://gist.github.com/JaiHirsch/944c8313d4eff0a59ec9
     */
    private val documentCodec: Codec[Document] = new DocumentCodec()

    override def encode(writer: BsonWriter, value: CrawlLink, encoderContext: EncoderContext): Unit = {
      val doc = new Document()
      doc.put("_link_to_crawl", value._link_to_crawl)
      doc.put("is_crawled", value.is_crawled)
      documentCodec.encode(writer, doc, encoderContext)
    }

    override def getEncoderClass: Class[CrawlLink] = classOf[CrawlLink]

    override def decode(reader: BsonReader, decoderContext: DecoderContext): CrawlLink = {
      val doc = documentCodec.decode(reader, decoderContext)
      CrawlLink(doc.getString("_link_to_crawl"), doc.getBoolean("is_crawled"))
    }
  }

  object CrawlLinksCodec {
    def apply(): Codec[CrawlLink] = new CrawlLinksCodec()
  }

}
