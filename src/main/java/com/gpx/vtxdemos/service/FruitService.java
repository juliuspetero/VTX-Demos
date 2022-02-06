package com.gpx.vtxdemos.service;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

import java.time.Instant;

public class FruitService {

  private final MongoClient mongo;

  public FruitService(MongoClient mongo) {
    this.mongo = mongo;
  }

  public Future<String> addApple(int mass, Instant expiration) {
    Future<String> future = insertFruit("apple", mass, expiration);
    return future;
  }

  public Future<Void> addBanana(int mass, Instant expiration) {
    return insertFruit("banana", mass, expiration).mapEmpty();
  }

  private Future<String> insertFruit(String type, int mass, Instant expiration) {
    Promise<String> promise = Promise.promise();
    mongo.insert("fruits", new JsonObject()
      .put("type", type)
      .put("mass", mass)
      .put("expiration", new JsonObject().put("$date", expiration)), promise);
    return promise.future();
  }
}
