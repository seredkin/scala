import com.example.config.{RootConfig, WebConfig}
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient

@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[com.example.Application], classOf[RootConfig],
  classOf[WebConfig]), webEnvironment = WebEnvironment.RANDOM_PORT)
class LogEntryTest {

  @Autowired
  var webClient: WebTestClient = _

  @Test def testLogs(): Unit = {

    webClient.get.uri("/logs").exchange().returnResult(classOf[String])
      .getResponseBody.buffer(3).blockLast().forEach( t => print("\tMESSAGE\t"+t.toString))

  }

  @Test def testAccounts(): Unit = {
    val str = webClient.get.uri("/accounts").exchange().returnResult(classOf[String]).getResponseBody.blockLast()
    println("ACCOUNTS: "+str)

  }
}

