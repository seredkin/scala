import org.junit.FixMethodOrder
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import tweets.TweetConfig
import org.junit.Test

@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[TweetConfig]), webEnvironment = WebEnvironment.RANDOM_PORT)
//@WebAppConfiguration
@FixMethodOrder(MethodSorters.JVM)
class LogEntryTest {

  @Autowired
  var webClient: WebTestClient = _

  @Test def testLogs(): Unit = {

    webClient.get.uri("/logs").exchange().returnResult(classOf[String])
      .getResponseBody.buffer(3).blockLast().forEach( t => print("\tMESSAGE\t"+t.toString))

  }
}

