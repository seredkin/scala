package com.example.config

import javax.sql.DataSource

import com.fasterxml.jackson.databind.SerializationFeature
import org.jooq.SQLDialect
import org.jooq.impl.DefaultDSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Primary}
import org.springframework.core.env.Environment
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder


@Configuration
@ComponentScan(basePackages = Array("com.example"))
@EnableCaching
class RootConfig(@Autowired val env: Environment) {

  val logger = com.typesafe.scalalogging.Logger(classOf[RootConfig])

  @Bean
  @Primary def mapperBuilder: Jackson2ObjectMapperBuilder = {
    Jackson2ObjectMapperBuilder.json.failOnUnknownProperties(false).featuresToEnable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    /*.featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)*/
  }

  @Bean def dsl(@Autowired default: DataSource)= new DefaultDSLContext(default, SQLDialect.POSTGRES_9_5)
}
