package com.gpx.vtxdemos.verticles.eventbus;

import com.gpx.vtxdemos.verticles.intro.OrderVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultipleDeployHelper extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(OrderVerticle.class);

  @Override
  public void start(Promise<Void> promise) {
    CompositeFuture.all(deployHelper(ClusteredReceiver.class.getName()),
      deployHelper(ClusteredSender.class.getName())).succeeded();
  }

  private Future<Void> deployHelper(String name) {
    final Promise<Void> promise = Promise.promise();
    vertx.deployVerticle(name, res -> {
      if (res.failed()) {
        LOGGER.error("Failed to deploy verticle " + name);
        promise.fail(res.cause());
      } else {
        LOGGER.info("Deployed verticle " + name);
        promise.complete();
      }
    });
    return promise.future();
  }
}
