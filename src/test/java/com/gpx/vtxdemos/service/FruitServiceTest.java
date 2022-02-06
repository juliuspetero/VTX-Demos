package com.gpx.vtxdemos.service;

import com.noenv.wiremongo.WireMongo;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@ExtendWith(VertxExtension.class)
public class FruitServiceTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(FruitServiceTest.class.getName());
  private WireMongo wiremongo;
  private FruitService fruitService;

  @BeforeEach
  public void setUp() {
    wiremongo = new WireMongo();
    fruitService = new FruitService(wiremongo.getClient());
  }

  @AfterEach
  public void tearDown(VertxTestContext testContext) {
    testContext.completeNow();
  }

  @Test
  public void testAddApple(VertxTestContext testContext) {
    testContext.verify(
      () -> Assertions.assertAll(() -> {
        Instant expiration = Instant.now().plus(5, ChronoUnit.DAYS);
        wiremongo.insert()
          .inCollection("fruits")
          .withDocument(new JsonObject()
            .put("type", "apple")
            .put("mass", 161)
            .put("expiration", new JsonObject().put("$date", expiration)))
          .returnsObjectId();
        testContext.assertComplete(fruitService.addApple(161, expiration));
        fruitService.addApple(161, expiration).onComplete(testContext.succeedingThenComplete());
      }, () -> {
        Instant expiration = Instant.now().plus(5, ChronoUnit.DAYS);
        wiremongo.insert()
          .inCollection("fruits")
          .withDocument(new JsonObject()
            .put("type", "apple")
            .put("mass", 162)
            .put("expiration", new JsonObject().put("$date", expiration)))
          .returnsObjectId();
        Future<String> future = fruitService.addApple(162, expiration);

        LOGGER.info("This is executed when the future is successful");
        future.onComplete(testContext.succeeding(buffer -> {
          LOGGER.info("Result from WireMongo mock :: {}", buffer);
          Assertions.assertNotNull(buffer);
        }));
        LOGGER.info("The test context will pass when the future is successful");
        future.onComplete(testContext.succeedingThenComplete());

        future = fruitService.addApple(165, expiration);
        LOGGER.info("The test context will pass when the future has failed");
        future.onComplete(testContext.failingThenComplete());
        LOGGER.info("This is executed when the future failed");
        future.onComplete(testContext.failing(buffer -> {
          LOGGER.info("Exception failure from WireMongo :: {} ", buffer.getLocalizedMessage(), buffer);
          Assertions.assertNotNull(buffer);
        }));
      })
    );
    testContext.completeNow();
  }

  @Test
  void start_server(Vertx vertx) {
    vertx.createHttpServer()
      .requestHandler(req -> req.response().end("Ok"))
      .listen(16969, ar -> {
        boolean status = ar.succeeded();
        Assertions.assertFalse(ar.succeeded());
      });
  }

  @Test
  void start_http_server(Vertx vertx, VertxTestContext testContext) throws Throwable {
    vertx.createHttpServer()
      .requestHandler(req -> req.response().end())
      .listen(16969)
      .onComplete(testContext.succeedingThenComplete());
    Assertions.assertTrue(testContext.awaitCompletion(5, TimeUnit.SECONDS));
    if (testContext.failed()) {
      throw testContext.causeOfFailure();
    }
  }
}
