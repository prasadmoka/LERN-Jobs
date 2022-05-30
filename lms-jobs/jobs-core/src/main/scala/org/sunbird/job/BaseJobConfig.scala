package org.sunbird.job

import com.typesafe.config.Config
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.typeutils.TypeExtractor
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig

import java.io.Serializable
import java.util.Properties

class BaseJobConfig(val config: Config, val jobName: String) extends Serializable {

  implicit val metricTypeInfo: TypeInformation[String] = TypeExtractor.getForClass(classOf[String])

  val kafkaBrokerServers: String = config.getString("kafka.broker-servers")
  val zookeeper: String = config.getString("kafka.zookeeper")
  val kafkaProducerBrokerServers: String = config.getString("kafka.producer.broker-servers")
  val kafkaConsumerBrokerServers: String = config.getString("kafka.consumer.broker-servers")
  // Producer Properties
  val kafkaProducerMaxRequestSize: Int = config.getInt("kafka.producer.max-request-size")
  val kafkaProducerBatchSize: Int = config.getInt("kafka.producer.batch.size")
  val kafkaProducerLingerMs: Int = config.getInt("kafka.producer.linger.ms")
  val kafkaProducerCompression: String = if (config.hasPath("kafka.producer.compression")) config.getString("kafka.producer.compression") else "snappy"
  val groupId: String = config.getString("kafka.groupId")
  val restartAttempts: Int = config.getInt("task.restart-strategy.attempts")
  val delayBetweenAttempts: Long = config.getLong("task.restart-strategy.delay")
  val parallelism: Int = config.getInt("task.parallelism")

  val kafkaConsumerParallelism: Int = config.getInt("task.consumer.parallelism")
  // Only for Tests
  val kafkaAutoOffsetReset: Option[String] = if (config.hasPath("kafka.auto.offset.reset")) Option(config.getString("kafka.auto.offset.reset")) else None

  // Checkpointing config
  val enableCompressedCheckpointing: Boolean = config.getBoolean("task.checkpointing.compressed")
  val checkpointingInterval: Int = config.getInt("task.checkpointing.interval")
  val checkpointingPauseSeconds: Int = config.getInt("task.checkpointing.pause.between.seconds")
  val enableDistributedCheckpointing: Option[Boolean] = if (config.hasPath("job.enable.distributed.checkpointing")) Option(config.getBoolean("job.enable.distributed.checkpointing")) else None
  val checkpointingBaseUrl: Option[String] = if (config.hasPath("job.statebackend.base.url")) Option(config.getString("job.statebackend.base.url")) else None
  // By default checkpointing timeout is 10 mins
  val checkpointingTimeout: Long = if (config.hasPath("task.checkpointing.timeout")) config.getLong("task.checkpointing.timeout") else 600000L

  // Redis
  val redisHost: String = Option(config.getString("redis.host")).getOrElse("localhost")
  val redisPort: Int = Option(config.getInt("redis.port")).getOrElse(6379)

  // LMS Cassandra DB Config
  val lmsDbHost: String = config.getString("lms-cassandra.host")
  val lmsDbPort: Int = config.getInt("lms-cassandra.port")

  def kafkaConsumerProperties: Properties = {
    val properties = new Properties()
    //properties.setProperty("bootstrap.servers", kafkaBrokerServers)
    properties.setProperty("bootstrap.servers", kafkaConsumerBrokerServers)
    properties.setProperty("group.id", groupId)
    properties.setProperty(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed")
    kafkaAutoOffsetReset.map {
      properties.setProperty("auto.offset.reset", _)
    }
    properties
  }

  def kafkaProducerProperties: Properties = {
    val properties = new Properties()
    //properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokerServers)
    properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProducerBrokerServers)
    properties.put(ProducerConfig.LINGER_MS_CONFIG, new Integer(kafkaProducerLingerMs))
    properties.put(ProducerConfig.BATCH_SIZE_CONFIG, new Integer(kafkaProducerBatchSize))
    properties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, kafkaProducerCompression)
    properties.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, new Integer(kafkaProducerMaxRequestSize))
    properties
  }

  def getString(key: String, default: String): String = {
    if (config.hasPath(key)) config.getString(key) else default
  }

  def getInt(key: String, default: Int): Int = {
    if (config.hasPath(key)) config.getInt(key) else default
  }

  def getBoolean(key: String, default: Boolean): Boolean = {
    if (config.hasPath(key)) config.getBoolean(key) else default
  }
}
