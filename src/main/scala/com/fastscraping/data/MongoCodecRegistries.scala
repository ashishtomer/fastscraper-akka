package com.fastscraping.data

import java.util

import com.fastscraping.data.bson.CrawlLink
import com.fastscraping.data.bson.CrawlLink.CrawlLinksCodec
import com.mongodb.MongoClientSettings
import org.bson.codecs.Codec
import org.bson.codecs.configuration.CodecRegistries.{fromCodecs, fromRegistries}
import org.bson.codecs.configuration.CodecRegistry

object MongoCodecRegistries {
  private val crawlLinkCodec: Codec[CrawlLink] = CrawlLinksCodec()
  private val allCodecs: util.ArrayList[CodecRegistry] = new util.ArrayList[CodecRegistry]
  private var codecRegistry: CodecRegistry = _

  def getCodecRegistries: CodecRegistry = synchronized {
    if (codecRegistry == null) {
      allCodecs.add(fromCodecs(crawlLinkCodec))
      allCodecs.add(MongoClientSettings.getDefaultCodecRegistry)
      codecRegistry = fromRegistries(allCodecs)
    }

    codecRegistry
  }

}
