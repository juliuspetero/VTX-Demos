package com.gpx.vtxdemos.verticles.eventbus;

import com.gpx.vtxdemos.verticles.intro.OrderVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusteredReceiver extends AbstractVerticle {
  private static final Logger LOGGER = LoggerFactory.getLogger(OrderVerticle.class);

  @Override
  public void start() throws Exception {
    final EventBus eventBus = vertx.eventBus();
    eventBus.consumer("ADDRESS", receivedMessage -> {
      LOGGER.debug("Received message: " + receivedMessage.body());
      receivedMessage.reply("DEFAULT_REPLY_MESSAGE");
    });
    LOGGER.info("Receiver ready!");
  }
}
