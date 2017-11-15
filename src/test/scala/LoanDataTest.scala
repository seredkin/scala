import com.example.jooq_dsl.Tables
import com.example.config.RootConfig
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.jooq.DSLContext
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient

@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[com.example.Application], classOf[RootConfig]),
  webEnvironment = WebEnvironment.RANDOM_PORT)
class LoanDataTest {

  @Autowired var dsl: DSLContext = _
  @Autowired
  var webClient: WebTestClient = _

  @Test def testLoanData(): Unit = {
    val meta = dsl.meta()
    val count:Integer = dsl.fetchCount(Tables.LOAN)
    assertThat(count, Matchers.greaterThan(new Integer(1)))
    /*
        webClient.get.uri("/logs").exchange().returnResult(classOf[String])
          .getResponseBody.buffer(3).blockLast().forEach( t => print("\tMESSAGE\t"+t.toString))
    */

  }

  @Test def testAccounts(): Unit = {
    val str = webClient.get.uri("/accounts").exchange().returnResult(classOf[String]).getResponseBody.blockLast()
    println("ACCOUNTS: "+str)

  }
}

