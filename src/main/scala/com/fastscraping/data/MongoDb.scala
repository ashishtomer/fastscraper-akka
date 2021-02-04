package com.fastscraping.data

import com.mongodb.MongoClientSettings
import org.mongodb.scala.model.FindOneAndUpdateOptions
import org.mongodb.scala.{Document, MongoClient, MongoCollection, Observable, Observer, ServerAddress, SingleObservable}

import scala.jdk.CollectionConverters._

//Single instance for whole application
//Each website which is being scraped can have its own database
//Or the client can use the databases as needed
class MongoDb(address: String, port: Int, clientDb: String = "fastscraper") extends Database {
  private lazy val db = MongoDb.getSingleton(address, port).getDatabase(clientDb)

  private def WithCollection[T](collectionName: String)(f: MongoCollection[Document] => T): T = {
    f(db.getCollection(collectionName))
  }

  private def ObserveDbTransaction[T](id: String)(f: => Observable[T]) = {
    f.subscribe(new Observer[T] {
      override def onNext(result: T): Unit = println(s"[db=$db][id=$id]MongoDb transaction completed with: $result")

      override def onError(e: Throwable): Unit = println(s"[db=$db][id=$id]Error while completing transaction ${e.getMessage}")

      override def onComplete(): Unit = println(s"[db=$db][id=$id]Mongodb transaction completed")
    })
  }

  private val doUpsert = FindOneAndUpdateOptions().upsert(true)
  private val id = "_id"

  override def saveText(index: String, documentId: String, column: String, text: String) = {
    WithCollection(index){ collection =>
      ObserveDbTransaction(documentId) {
        collection.findOneAndUpdate(
          Document(id -> documentId), //Find with id -- which usually should be page url
          Document(id -> documentId, column -> text), //Document to update
          doUpsert //Do upsert
        )
      }
    }
  }
}

object MongoDb {
  private var mongoClient: MongoClient = null

  private def getSingleton(address: String, port: Int) = if(mongoClient != null) {
    mongoClient
  } else {
    synchronized {
      if(mongoClient == null) {
        mongoClient = MongoClient(
          MongoClientSettings.builder()
            .applyToClusterSettings(builder => builder.hosts(List(new ServerAddress(address, port)).asJava))
            .build()
        )
      }

      mongoClient
    }
  }

  def apply(address: String, port: Int, clientDb: String): MongoDb = new MongoDb(address, port, clientDb)

}
