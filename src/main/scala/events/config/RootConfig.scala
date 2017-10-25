package events.config

import java.util.Collections
import javax.sql.DataSource

import com.fasterxml.jackson.databind.SerializationFeature
import org.jooq
import org.jooq.impl.{DataSourceConnectionProvider, DefaultConfiguration, DefaultDSLContext, ThreadLocalTransactionProvider}
import org.jooq.{ConnectionProvider, DSLContext, SQLDialect, TransactionProvider}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCache
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Primary}
import org.springframework.core.env.Environment
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.jdbc.datasource.{DataSourceTransactionManager, TransactionAwareDataSourceProxy}


@Configuration
@ComponentScan(basePackages = Array("events"))
@EnableCaching
class RootConfig (@Autowired val env: Environment) {

  val logger = com.typesafe.scalalogging.Logger(classOf[RootConfig])

  @Bean @Primary def mapperBuilder: Jackson2ObjectMapperBuilder = {
  Jackson2ObjectMapperBuilder.json.failOnUnknownProperties (false).featuresToEnable (SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    /*.featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)*/
}
  @Bean def cacheManager: CacheManager = {
  // configure and return an implementation of Spring's CacheManager SPI
  val cacheManager: SimpleCacheManager = new SimpleCacheManager
  cacheManager.setCaches (Collections.singletonList(new ConcurrentMapCache ("default") ) )
  logger.info("Initialized CacheManager [default]")
  cacheManager
}


  @Bean def transactionManager(@Autowired defaultDS: DataSource): DataSourceTransactionManager = {
  new DataSourceTransactionManager (defaultDS)
}
  @Bean def dsl(@Autowired default: DataSource): DSLContext = {
  new DefaultDSLContext (jooqConfig (connectionProvider(default)) )
}
  @Bean @Primary def connectionProvider(default: DataSource): ConnectionProvider = {
  new DataSourceConnectionProvider (new TransactionAwareDataSourceProxy (default))
}
  @Bean @Primary def threadLocalTransactionProvider(@Autowired default: DataSource): TransactionProvider = {
  new ThreadLocalTransactionProvider (connectionProvider(default))
}
  @Bean def jooqConfig (connectionProvider: ConnectionProvider): jooq.Configuration = {
  new DefaultConfiguration().derive(SQLDialect.POSTGRES_9_5)
}
}
