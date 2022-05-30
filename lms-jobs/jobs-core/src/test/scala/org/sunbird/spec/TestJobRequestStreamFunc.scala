package org.sunbird.spec

import org.apache.flink.streaming.api.functions.ProcessFunction
import org.sunbird.job.domain.reader.JobRequest
import org.sunbird.job.{BaseProcessFunction, Metrics}

class TestJobRequestStreamFunc(config: BaseProcessTestConfig) extends BaseProcessFunction[TestJobRequest, TestJobRequest](config) {

  override def metricsList(): List[String] = {
    val metrics= List(config.jobRequestEventCount)
    metrics
  }

  override def processElement(event: TestJobRequest, context: ProcessFunction[TestJobRequest, TestJobRequest]#Context, metrics: Metrics): Unit = {
    metrics.get(config.jobRequestEventCount)
    metrics.reset(config.jobRequestEventCount)
    metrics.incCounter(config.jobRequestEventCount)
    context.output(config.jobRequestOutputTag, event)
  }
}

class TestJobRequest(map: java.util.Map[String, Any], partition: Int, offset: Long) extends JobRequest(map, partition, offset) {

}