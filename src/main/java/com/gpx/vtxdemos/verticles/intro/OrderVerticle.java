package com.gpx.vtxdemos.verticles.intro;

import com.gpx.vtxdemos.models.model.Order;
import com.gpx.vtxdemos.models.model.OrderStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
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

public class OrderVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(OrderVerticle.class);

  @Override
  public void start(Promise<Void> promise) throws Exception {
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    router.post("/api/post").consumes("application/json").produces("application/json")
      .handler(this::handleOrder);
    int port = 9090;
    vertx.createHttpServer()
      .requestHandler(router)
      .listen(config().getInteger("http.port", port), result -> {
        if (result.succeeded()) {
          promise.complete();
          System.out.println("HTTP server started on port " + port);
        } else {
          promise.fail(result.cause());
          System.out.println("Failed to start!!!!!!");
        }
      });
  }

  public void handleOrder(RoutingContext rc) {
    Properties config = new Properties();
    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonObjectSerializer.class);
    config.put(ProducerConfig.ACKS_CONFIG, "1");
    KafkaProducer<String, JsonObject> producer = KafkaProducer.create(vertx, config);
    producer.partitionsFor("orders-out", done -> {
      List<PartitionInfo> result = done.result();
      done.result().forEach(p -> LOGGER.info("Partition: id={}, topic={}", p.getPartition(), p.getTopic()));
    });

    Order o = Json.decodeValue(rc.getBodyAsString(), Order.class);
    KafkaProducerRecord<String, JsonObject> record = KafkaProducerRecord.create("orders-out", null, rc.getBodyAsJson(), o.getType().ordinal());
    producer.write(record, done -> {
      if (done.succeeded()) {
        Void recordMetadata = done.result();
//        LOGGER.info("Record sent: msg={}, destination={}, partition={}, offset={}", record.value(), recordMetadata.getTopic(), recordMetadata.getPartition(), recordMetadata.getOffset());
//        o.setId(recordMetadata.getOffset());
        o.setStatus(OrderStatus.PROCESSING);
      } else {
        Throwable t = done.cause();
        LOGGER.error("Error sent to topic: {}", t.getMessage());
        o.setStatus(OrderStatus.REJECTED);
      }
      rc.response().end(Json.encodePrettily(o));
    });

  }
}
