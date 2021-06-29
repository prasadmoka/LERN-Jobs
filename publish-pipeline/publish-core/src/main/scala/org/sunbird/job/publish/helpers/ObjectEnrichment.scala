package org.sunbird.job.publish.helpers

import org.sunbird.job.publish.config.PublishConfig
import org.sunbird.job.publish.core.{ExtDataConfig, ObjectData}
import org.sunbird.job.publish.util.CloudStorageUtil
import org.sunbird.job.util.{CassandraUtil, Neo4JUtil}

trait ObjectEnrichment extends FrameworkDataEnrichment with ThumbnailGenerator {


  def enrichObject(obj: ObjectData)(implicit neo4JUtil: Neo4JUtil, cassandraUtil: CassandraUtil, readerConfig: ExtDataConfig, cloudStorageUtil: CloudStorageUtil, config: PublishConfig): ObjectData = {
    val newObj = enrichFrameworkData(obj)
    val enObj = enrichObjectMetadata(newObj).getOrElse(newObj)
    generateThumbnail(enObj).getOrElse(enObj)
  }

  def enrichObjectMetadata(obj: ObjectData)(implicit neo4JUtil: Neo4JUtil, cassandraUtil: CassandraUtil, readerConfig: ExtDataConfig, config: PublishConfig): Option[ObjectData]

}
