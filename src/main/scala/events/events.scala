package events

import com.example.jooq_dsl.Tables
import com.example.jooq_dsl.tables.pojos.Loan
import org.joda.time.DateTime
import org.jooq.DSLContext
import org.reactivestreams.Publisher
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.{ApplicationRunner, SpringApplication}
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.RequestPredicates._
import org.springframework.web.reactive.function.server.RouterFunctions._
import org.springframework.web.reactive.function.server.ServerResponse._
import org.springframework.web.reactive.function.server.{RouterFunction, ServerResponse}
import reactor.core.CoreSubscriber
import reactor.core.publisher.Mono

import scala.beans.BeanProperty
import scala.collection.JavaConverters

@SpringBootApplication
class Application {

  @Bean
  def init(): ApplicationRunner = args => {
    println("App init")
  }
}

object Application extends App {
  SpringApplication.run(classOf[Application], args: _*)
}


@Service
class LoanService(dsl: DSLContext) {
  def accounts(): String  = "Amount of LoanData records "+dsl.fetchCount(Tables.LOAN)



  def tweets(): Publisher[Tweet] = new Mono[Tweet] {
    override def subscribe(coreSubscriber: CoreSubscriber[_ >: Tweet]):Unit = Tweet("test", Author("Jack"))
  }

  def loans(): Publisher[Loan] = new Mono[Loan] {
    override def subscribe(coreSubscriber: CoreSubscriber[_ >: Loan]): Unit = new Loan()
  }
}

@Configuration
class RouteConfiguration(tweetService: LoanService) {

  @Bean
  def routes(): RouterFunction[ServerResponse] =
    route(GET("/events"), _ => ok().body(tweetService.tweets(), classOf[Tweet]))
      .andRoute(GET("/loans"), _ => ok().body(tweetService.loans(), classOf[Loan]))
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


case class Author(@BeanProperty handle: String)

case class HashTag(@BeanProperty tag: String)

case class LogEntry(@BeanProperty message: String) {
  @BeanProperty
  var id: String = _

  @BeanProperty
  var created: DateTime = null

}


case class Tweet(@BeanProperty text: String, @BeanProperty author: Author) {

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