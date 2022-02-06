package com.gpx.vtxdemos.verticles.intro;

import com.gpx.vtxdemos.models.Article;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class ArticleVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> promise) throws Exception {
    Router router = Router.router(vertx);
    router.get("/api/article/:id")
      .handler(this::getArticles);
    vertx.createHttpServer()
      .requestHandler(router)
      .listen(config().getInteger("http.port", 8080), result -> {
        if (result.succeeded()) {
          promise.complete();
        } else {
          promise.fail(result.cause());
        }
      });
  }

  private void getArticles(RoutingContext routingContext) {
    String articleId = routingContext.request()
      .getParam("id");
    Article article = new Article(articleId,
      "This is an intro to vertx", "baeldung", "01-02-2017", 1578);

    String rt = Json.encodePrettily(article);

    routingContext.response()
      .putHeader("content-type", "application/json")
      .setStatusCode(200)
      .end(Json.encodePrettily(article));
  }
}
