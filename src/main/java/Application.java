import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import util.KeysUtils;


/**
 * Main application file
 */
public class Application extends AbstractVerticle {

    @Override
    public void start(Future<Void> fut) {

        System.out.println(this.getClass().getSimpleName() + " | start");
        Vertx.currentContext().processArgs();
        JsonObject conf = Vertx.currentContext().config();

        System.out.println(this.getClass().getSimpleName() + " | found config file...");

        System.out.println(this.getClass().getSimpleName() + " | Preparing port...");

        Integer port = conf.getInteger(KeysUtils.PORT, 8080);

        if (port == null) {
            throw new RuntimeException("No port provided");
        }

        System.out.println(this.getClass().getSimpleName() + " | Deploying to port " + port);

        System.out.println(this.getClass().getSimpleName() + " | Preparing routes...");

        JsonArray routes = conf.getJsonArray(KeysUtils.ROUTES);

        if (routes == null || routes.size() == 0) {
            throw new RuntimeException("No routes provided");
        }

        RouteHandler.getInstance().parseRoutes(vertx, routes, router -> {
            if (router != null) {
                System.out.println(this.getClass().getSimpleName() + " | Routes prepared...");

                vertx
                        .createHttpServer()
                        .requestHandler(router::accept)
                        .listen(
                                // Retrieve the port from the configuration,
                                // default to 8080.
                                port,
                                result -> {
                                    if (result.succeeded()) {
                                        System.out.println(this.getClass().getSimpleName() + " | Proto Server started");
                                        fut.complete();
                                    } else {
                                        fut.fail(result.cause());
                                    }
                                }
                        );

            } else {
                throw new RuntimeException("Error parsing routes");
            }
        });


    }
}
