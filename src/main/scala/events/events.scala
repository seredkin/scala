package events

import javax.sql.DataSource

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import org.joda.time.DateTime
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.jooq.impl.DSL._
import org.reactivestreams.Publisher
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.{EnableAutoConfiguration, SpringBootApplication}
import org.springframework.boot.{ApplicationRunner, SpringApplication}
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.data.annotation.{CreatedDate, Id}
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.RequestPredicates._
import org.springframework.web.reactive.function.server.RouterFunctions._
import org.springframework.web.reactive.function.server.ServerResponse._
import org.springframework.web.reactive.function.server.{RouterFunction, ServerResponse}
import reactor.core.publisher.Flux

import scala.beans.BeanProperty
import scala.collection.JavaConverters

@SpringBootApplication
class Application {

  @Bean
  def init(tr: TweetRepository, ler: LogEntryRepository): ApplicationRunner = args => {
    val viktor = Author("viktorklang")
    val jonas = Author("jboner")
    val josh = Author("starbuxman")
    val tweets = Flux.just(
      Tweet("Woot, Konrad will be talking about #Enterprise #Integration done right! #akka #alpakka", viktor),
      Tweet("#scala implicits can easily be used to model Capabilities, but can they encode Obligations easily?\n\n* Easy as in: ergonomically.", viktor),
      Tweet("This is so cool! #akka", viktor),
      Tweet("Cross Data Center replication of Event Sourced #Akka Actors is soon available (using #CRDTs, and more).", jonas),
      Tweet("a reminder: @SpringBoot lets you pair-program with the #Spring team.", josh),
      Tweet("whatever your next #platform is, don't build it yourself. \n\nEven companies with the $$ and motivation to do it fail. a LOT.", josh)
    )

    val logs = Flux.just(LogEntry("Log message 01"), LogEntry("Log message 02"), LogEntry("Log message 03"))
    ler.deleteAll().thenMany(ler.saveAll(logs)).thenMany(ler.findAll())
      .subscribe(l => println(s"""${l.id} \t ${l.message}"""))

    tr
      .deleteAll()
      .thenMany(tr.saveAll(tweets))
      .thenMany(tr.findAll())
      .subscribe(t => println(
        s"""=====================================================
           |@${t.author.handle} ${t.hashtags}
           |${t.text}
         """.stripMargin
      ))

  }
}

object Application extends App {
  SpringApplication.run(classOf[Application], args: _*)
}


@Configuration
class AkkaConfiguration {

  @Bean def actorSystem(): ActorSystem = ActorSystem.create("bootifulScala")

  @Bean def actorMaterializer(): ActorMaterializer = ActorMaterializer.create(this.actorSystem())
}

@Service
class TweetService(tr: TweetRepository, ler: LogEntryRepository, am: ActorMaterializer, dsl: DSLContext, ds:DataSource) {
  def accounts(): String  = {
    "Amount of user accounts "+DSL.using(ds.getConnection).fetchCount(table(name("brokerage", "account")))
  }


  def tweets(): Publisher[Tweet] = tr.findAll()

  def hashtags(): Publisher[HashTag] =
    Source
      .fromPublisher(tweets())
      .map(t => JavaConverters.asScalaSet(t.hashtags).toSet)
      .reduce((a, b) => a ++ b)
      .mapConcat(identity)
      .runWith(Sink.asPublisher(true)) {
        am
      }

  def logs(): Publisher[LogEntry] = ler.findAll()
}

@Configuration
@EnableMongoAuditing
class TweetRouteConfiguration(tweetService: TweetService) {

  @Bean
  def routes(): RouterFunction[ServerResponse] =
    route(GET("/events"), _ => ok().body(tweetService.tweets(), classOf[Tweet]))
      .andRoute(GET("/hashtags/unique"), _ => ok().body(tweetService.hashtags(), classOf[HashTag]))
      .andRoute(GET("/logs"), _ => ok().body(tweetService.logs(), classOf[LogEntry]))
      .andRoute(GET("/accounts"), _ => ok().syncBody(tweetService.accounts(), classOf[String]))



}

/*
@RestController
class TweetRestController(ts: TweetService) {

  @GetMapping(Array("/hashtags/unique"))
  def hashtags(): Publisher[HashTag] = ts.hashtags()

  @GetMapping(Array("/tweets"))
  def tweets(): Publisher[Tweet] = ts.tweets()


}
*/


trait TweetRepository extends ReactiveMongoRepository[Tweet, String]

@Document
case class Author(@BeanProperty @Id handle: String)

@Document
case class HashTag(@BeanProperty @Id tag: String)

@Document
case class LogEntry(@BeanProperty message: String) {
  @BeanProperty
  @Id var id: String = _

  @BeanProperty
  @CreatedDate var created: DateTime = null

}

trait LogEntryRepository extends ReactiveMongoRepository[LogEntry, String]

@Document
case class Tweet(@BeanProperty @Id text: String, @BeanProperty author: Author) {

  @BeanProperty
  var hashtags: java.util.Set[HashTag] = JavaConverters.setAsJavaSet(
    text
      .split(" ")
      .collect {
        case t if t.startsWith("#") => HashTag(t.replaceAll("[^#\\w]", "").toLowerCase())
      }
      .toSet
  )

}