package com.fastscraping.actor.message

import com.fastscraping.model.WebpageIdentifier

sealed trait WorkerActorMessage

case class ScrapeWebpage(
                          link: String,
                          jobId: String,
                          webpageIdentifier: Seq[WebpageIdentifier]
                        ) extends WorkerActorMessage
