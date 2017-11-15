package com.example

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import org.slf4j.LoggerFactory

@Component
class Producer(@Autowired kafka: KafkaTemplate[String, String]) {


  private val LOGGER = LoggerFactory.getLogger(classOf[Producer])

  def send(topic: String, payload: String): Unit = {
    LOGGER.info(s"sending payload='$payload' to topic='$topic'")
    kafka.send(topic, payload)
  }

}

