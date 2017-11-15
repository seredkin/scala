package com.example

class Consumer {

  import org.apache.kafka.clients.consumer.ConsumerRecord
  import org.slf4j.LoggerFactory
  import org.springframework.kafka.annotation.KafkaListener
  import java.util.concurrent.CountDownLatch

  private val LOGGER = LoggerFactory.getLogger(classOf[Consumer])

  private val latch = new CountDownLatch(1)

  def getLatch: CountDownLatch = latch

  @KafkaListener(topics = Array("${kafka.topic.boot}")) def receive(consumerRecord: ConsumerRecord[_, _]): Unit = {
    LOGGER.info("received payload='{}'", consumerRecord.toString)
    latch.countDown()
  }
}
