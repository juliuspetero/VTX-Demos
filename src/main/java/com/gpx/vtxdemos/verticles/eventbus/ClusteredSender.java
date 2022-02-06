package com.gpx.vtxdemos.verticles.eventbus;

import com.gpx.vtxdemos.verticles.intro.OrderVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusteredSender extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(OrderVerticle.class);

  @Override
  public void start(Promise<Void> future) throws Exception {
    final Router router = Router.router(vertx);
    router.route("/").handler(routingContext -> {
      HttpServerResponse response = routingContext.response();
      response.putHeader("content-type", "text/html").end("<h1>Hello from non-clustered messenger example!</h1>");
    });
    router.post("/send/:message").handler(this::sendMessage);
    vertx.createHttpServer()
      .requestHandler(router)
      .listen(config()
        .getInteger("http.server.port", 8989), result -> {
        if (result.succeeded()) {
          LOGGER.info("HTTP server running on port " + 8989);
          future.complete();
        } else {
          LOGGER.error("Could not start a HTTP server", result.cause());
          future.fail(result.cause());
        }
      });
  }

  private void sendMessage(RoutingContext routingContext) {
    final EventBus eventBus = vertx.eventBus();
    final String message = routingContext.request().getParam("message");
    eventBus.send("ADDRESS", message);
    routingContext.response().end(message);
  }
}
