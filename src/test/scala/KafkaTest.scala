import java.util.concurrent.TimeUnit

import com.example._
import org.assertj.core.api.Assertions.assertThat
import org.junit.runner.RunWith
import org.junit.{ClassRule, Test}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.rule.KafkaEmbedded
import org.springframework.test.context.junit4.SpringRunner

@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[com.example.config.RootConfig]))
class KafkaTest {

  @Autowired var sender:Producer = _
  @Autowired var consumer:Consumer = _

  @Test
  @throws[Exception]
  def testReceive(): Unit = {
    sender.send(KafkaTest.BOOT_TOPIC, "Testing Kafka producer")
    consumer.getLatch.await(10000, TimeUnit.MILLISECONDS)
    assertThat(consumer.getLatch.getCount).isEqualTo(0)
  }
}

object KafkaTest {
  val BOOT_TOPIC = "boot.t"
  @ClassRule var embeddedKafka = new KafkaEmbedded(1, true, BOOT_TOPIC)
}