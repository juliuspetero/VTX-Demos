package com.gpx.vtxdemos.verticles.kafka;

import com.gpx.vtxdemos.models.model.Order;
import com.gpx.vtxdemos.models.model.OrderStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.kafka.client.common.PartitionInfo;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import io.vertx.kafka.client.serialization.JsonObjectSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;

public class KafkaOrderVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(KafkaOrderVerticle.class.getName());

  @Override
  public void start(Promise<Void> promise) throws Exception {
    Properties config = new Properties();
    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonObjectSerializer.class);
    config.put(ProducerConfig.ACKS_CONFIG, "1");
    KafkaProducer<String, JsonObject> producer = KafkaProducer.create(vertx, config);
    producer.partitionsFor("payex.sms", result -> {
      List<PartitionInfo> partitionInfoList = result.result();
      partitionInfoList.forEach(p -> LOGGER.info("Partition: id={}, topic={}", p.getPartition(), p.getTopic()));
    });

    Router router = Router.router(vertx);
    router.route("/*").handler(BodyHandler.create());
    router.post("/api/orders").handler(rc -> {
      String body = rc.getBodyAsString();
      Order order = Json.decodeValue(rc.getBodyAsString(), Order.class);
      KafkaProducerRecord<String, JsonObject> record = KafkaProducerRecord.create("payex.sms", null, rc.getBodyAsJson(), order.getType().ordinal());
      producer.write(record, result -> {
        if (result.succeeded()) {
          LOGGER.info("Result :: {}", result);
          order.setStatus(OrderStatus.PROCESSING);
        } else {
          Throwable t = result.cause();
          LOGGER.error("Error sent to topic: {}", t.getMessage());
          order.setStatus(OrderStatus.REJECTED);
        }
        rc.response().end(Json.encodePrettily(order));
      });
    });
    int port = 9092;
    vertx.createHttpServer().requestHandler(router).listen(port, res -> {
      if (res.succeeded()) {
        LOGGER.info("HTTP server running on port :: {} ", port);
        promise.complete();
      } else {
        LOGGER.error("Could not start an HTTP server :: {} ", port, res.cause());
        promise.fail(res.cause());
      }
    });
  }
}
