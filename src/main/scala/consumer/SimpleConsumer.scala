package consumer

import java.time.Duration
import java.util.Collections

import com.google.gson.{JsonParser, JsonSyntaxException}
import common.topics.KafkaTopics
import consumer.config.{KafkaConsumerConfig, SimpleConsumerProps}
import org.apache.kafka.clients.consumer.KafkaConsumer

import scala.jdk.CollectionConverters._

object SimpleConsumer
    extends App
    with KafkaTopics
    with KafkaConsumerConfig
    with SimpleConsumerProps {

  setProperties

  val consumer = new KafkaConsumer[String, String](properties)

  consumer.subscribe(Collections.singleton(INPUT_TOPIC))

  def parseJson(jsonString: String) = {
    JsonParser
      .parseString(jsonString)
      .getAsJsonObject
      .get("uid")
      .getAsString()
  }

  var mapOfIds: Map[String, Long] = Map()

  def lookUpMap(map: Map[String, Long], id: String): Map[String, Long] = {
    if (map.contains(id)) map.updated(id, map(id) + 1) else map + (id -> 1)
  }

  while (true) {
    val records = consumer.poll(Duration.ofMillis(100)).asScala

    for (record <- records) {
      try {
        val uid = parseJson(record.value())
        mapOfIds = lookUpMap(mapOfIds, uid)
        println("Unique ids count: " + mapOfIds.keySet.size)
      } catch {
        case e: JsonSyntaxException => println("Couldn't parse json")
      }

    }

  }

}
