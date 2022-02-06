package com.gpx.vtxdemos.verticles.kafka;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.kafka.client.common.TopicPartition;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class KafkaOrderProcessorVerticle extends AbstractVerticle {
  private static final Logger LOGGER = LoggerFactory.getLogger(KafkaOrderVerticle.class.getName());

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new KafkaOrderProcessorVerticle());
  }

  @Override
  public void start(Promise<Void> promise) throws Exception {

    Properties config = new Properties();
    config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
    config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    config.put(ConsumerConfig.GROUP_ID_CONFIG, "single-order");
    config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
    config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
    TopicPartition tp = new TopicPartition().setPartition(0).setTopic("payex.sms");
    KafkaConsumer<String, String> consumer = KafkaConsumer.create(vertx, config);
    consumer.assign(tp, ar -> {
      if (ar.succeeded()) {
        LOGGER.info("Subscribed");
        consumer.assignment(done1 -> {
          if (done1.succeeded()) {
            for (TopicPartition topicPartition : done1.result()) {
              LOGGER.info("Partition: topic={}, number={}", topicPartition.getTopic(), topicPartition.getPartition());
            }
          }
        });
      } else {
        LOGGER.error("Could not subscribe: err={}", ar.cause().getMessage());
      }
    });

    consumer.handler(record -> {
      LOGGER.info("Processing: key={}, value={}, partition={}, offset={}", record.key(), record.value(), record.partition(), record.offset());
    });
  }

}
