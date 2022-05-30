package org.sunbird.spec

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.sunbird.job.{BaseProcessFunction, Metrics}
import org.sunbird.job.cache.RedisConnect
import org.sunbird.job.dedup.DeDupEngine


class TestEventStreamFunc(config: BaseProcessTestConfig, @transient var dedupEngine: DeDupEngine = null)
                         (implicit val mapTypeInfo: TypeInformation[Event])
  extends BaseProcessFunction[Event, Event](config) {

  override def metricsList(): List[String] = {
    List(config.telemetryEventCount) ::: deduplicationMetrics
  }

  override def open(parameters: Configuration): Unit = {
    if (dedupEngine == null) {
      val redisConnect = new RedisConnect(config,Option(config.redisHost),Option(config.redisPort))
      dedupEngine = new DeDupEngine(config,redisConnect, config.dedupStore, config.cacheExpirySeconds)
    }
  }

  override def close(): Unit = {
    super.close()
    dedupEngine.close()
  }

  override def processElement(event: Event,
                              context: ProcessFunction[Event, Event]#Context,
                              metrics: Metrics): Unit = {
    try {
      deDup[Event, Event](event.mid(), event, context, config.eventOutputTag, config.duplicateEventOutputTag, flagName = "test-dedup")(dedupEngine, metrics)
    } catch {
      case ex: Exception =>
        ex.printStackTrace()

    }
  }
}
