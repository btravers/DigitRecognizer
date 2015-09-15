package com.zenika.digit_recognizer;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server extends AbstractVerticle {

    Logger logger = LoggerFactory.getLogger(Server.class);

    HttpServer server;
    Router router;
    FileSystem fs;

    @Override
    public void start(Future<Void> fut) {
        server = vertx.createHttpServer();
        router = Router.router(vertx);
        fs = vertx.fileSystem();

        List<String> images = fs.readDirBlocking("../images");

        router.route(HttpMethod.GET, "/").handler(routingHandler -> {
            logger.info("HTTP GET / => Returning the index page ");
            routingHandler.response().sendFile("web/dist/index.html");
        });

        router.route(HttpMethod.GET, "/digits/:id").handler(routingContext -> {
            String id = routingContext.request().getParam("id");
            logger.info("HTTP GET /digits/" + id + " => Returning the image corresponding to id " + id);
            routingContext.response().sendFile(images.get(Integer.parseInt(id)));
        });

        router.route(HttpMethod.GET, "/value/:id").handler(routingContext -> {
            String id = routingContext.request().getParam("id");
            logger.info("HTTP GET /value/" + id + " => Returning the value corresponding to id " + id);
            String image = images.get(Integer.parseInt(id));

            Pattern pattern = Pattern.compile(".*_(\\d)\\.png");
            Matcher match = pattern.matcher(image);
            if (match.matches()) {
                routingContext.response().end(match.group(1));
            }
        });

        router.route("/scripts/*").handler(StaticHandler.create()
            .setWebRoot("web/dist/scripts")
            .setCachingEnabled(false));

        router.route("/styles/*").handler(StaticHandler.create()
            .setWebRoot("web/dist/styles")
            .setCachingEnabled(false));

        server.requestHandler(router::accept)
            .listen(result -> {
                if (result.succeeded()) {
                    logger.info("Server is now listening");
                    fut.complete();
                } else {
                    logger.error("Failed to bind!");
                    fut.fail(result.cause());
                }
            });
    }

    @Override
    public void stop() {
        logger.info("Server is shutting down");
        server.close();
    }

}
