package com.example

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{CountDownLatch, TimeUnit}

import com.example.KafkaApp.log
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.boot.{ApplicationRunner, CommandLineRunner, SpringApplication}
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.kafka.annotation.{EnableKafka, KafkaListener}
import org.springframework.kafka.core.KafkaTemplate

@SpringBootApplication
@EnableKafka
class KafkaApp(kafkaTemplate: KafkaTemplate[String, String]) extends CommandLineRunner{
  private val latch = new CountDownLatch(3)

  private val myInteger: AtomicInteger = new AtomicInteger(0)

  @Bean
  def init(): ApplicationRunner = args => {
    println("App init")
  }

  @throws[Exception]
  override def run(args: String*): Unit = {
    this.kafkaTemplate.send("topic1", "foo1")
    this.kafkaTemplate.send("topic1", "foo2")
    this.kafkaTemplate.send("topic1", "foo3")
    latch.await(30, TimeUnit.SECONDS)
    log.info(s"Received ${latch.getCount}")
    System.exit(0)
  }

  @KafkaListener(topics = Array("topic1"))
  def listen(cr: ConsumerRecord[String, String]): Unit = {
    log.info(s"Received Kafka message ${myInteger.intValue()} of \t${cr.value()}")
    latch.countDown()
    myInteger.incrementAndGet()
  }

}

object KafkaApp extends App {
  val log = LoggerFactory.getLogger(classOf[Application])
  SpringApplication.run(classOf[KafkaApp], args: _*)
}

