package com.gpx.vtxdemos;

import com.gpx.vtxdemos.verticles.kafka.KafkaOrderVerticle;
import io.vertx.core.Vertx;

public class App {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new KafkaOrderVerticle());
  }
}
