package com.gpx.vtxdemos.verticles.intro;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class HelloVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> promise) {
    // config().getInteger("http.port", 9090)
    vertx.createHttpServer()
      .requestHandler(r -> r.response().end("Welcome to Vert.x Intro"))
      .listen(9090,
        result -> {
          if (result.succeeded()) {
            promise.complete();
          } else {
            promise.fail(result.cause());
          }
        });
  }


}
