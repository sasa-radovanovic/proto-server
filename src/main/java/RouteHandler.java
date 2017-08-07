
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import util.KeysUtils;

import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Route handler
 */
public class RouteHandler {

    private static RouteHandler routeHandler = new RouteHandler();

    private HashMap<String, String> variables = new HashMap<>();;


    private RouteHandler() {}

    public static RouteHandler getInstance() {
        return routeHandler;
    }

    /**
     *
     * Form router and setup routes
     *
     * @param vertx - Vertx object
     * @param routes - JSON array holding user configuration
     * @param handler - Return callback with Vertx handler
     */
    public void parseRoutes (Vertx vertx, JsonArray routes, Handler<Router> handler) {

        // Create router
        Router router = Router.router(vertx);

        // Handle body from requests
        router.route().handler(BodyHandler.create());

        // Go through user configuration
        routes.forEach(r -> {
            JsonObject routeObject = (JsonObject) r;

            System.out.println(this.getClass().getSimpleName() + " | Route " + routeObject.getString(KeysUtils.PATH));

            switch (routeObject.getString(KeysUtils.METHOD).toUpperCase()) {
                case "GET": {
                    System.out.println(this.getClass().getSimpleName() + " | Adding GET route " + routeObject.getString(KeysUtils.PATH));
                    router.get(routeObject.getString(KeysUtils.PATH)).handler(routingContext -> {
                        resolveGetResponse(routingContext, routeObject);
                    });
                    break;
                }
                case "POST": {
                    System.out.println(this.getClass().getSimpleName() + " | Adding POST route " + routeObject.getString(KeysUtils.PATH));
                    router.post(routeObject.getString(KeysUtils.PATH)).handler(routingContext -> {
                        resolvePostResponse(routingContext, routeObject);
                    });
                    break;
                }
                case "PUT": {
                    System.out.println(this.getClass().getSimpleName() + " | Adding PUT route " + routeObject.getString(KeysUtils.PATH));
                    router.put(routeObject.getString(KeysUtils.PATH)).handler(routingContext -> {
                        resolvePostResponse(routingContext, routeObject);
                    });
                    break;
                }
                case "DELETE": {
                    System.out.println(this.getClass().getSimpleName() + " | Adding DELETE route " + routeObject.getString(KeysUtils.PATH));
                    router.delete(routeObject.getString(KeysUtils.PATH)).handler(routingContext -> {
                        resolvePostResponse(routingContext, routeObject);
                    });
                    break;
                }
            }
        });

        handler.handle(router);
    }


    /**
     *
     * Handle GET requests
     *
     * @param routingContext - @RoutingContext object
     * @param routeObject - Single route configuration
     */
    private void resolveGetResponse (RoutingContext routingContext, JsonObject routeObject) {

        String retType = routeObject.getString(KeysUtils.RETURN_TYPE);
        String jsonDesignator = "";
        String[] temp = retType.split(KeysUtils.DELIMITER);
        if (temp.length > 1) {
            jsonDesignator = temp[1];
        }
        formResponse(temp, jsonDesignator, routeObject, routingContext);
    }


    /**
     *
     * Handle post (PUT/DELETE) requests
     *
     * @param routingContext - @RoutingContext object
     * @param routeObject - Single route configuration
     */
    private void resolvePostResponse(RoutingContext routingContext, JsonObject routeObject) {
        String saveObject = routeObject.getString(KeysUtils.SAVE_TO_VARIABLE);
        if (saveObject != null &&
                !saveObject.equalsIgnoreCase("")) {
            String type = saveObject.split(KeysUtils.DELIMITER)[1];
            if (type.equalsIgnoreCase(KeysUtils.OBJECT)) {
                variables.put(saveObject.split(KeysUtils.DELIMITER)[0], routingContext.getBodyAsJson().encodePrettily());
            } else if (type.equalsIgnoreCase(KeysUtils.ARRAY)) {
                variables.put(saveObject.split(KeysUtils.DELIMITER)[0], routingContext.getBodyAsJsonArray().encodePrettily());
            }
        }
        String retType = routeObject.getString(KeysUtils.RETURN_TYPE);
        String jsonDesignator = "";
        String[] temp = retType.split(KeysUtils.DELIMITER);
        if (temp.length > 1) {
            jsonDesignator = temp[1];
        }
        formResponse(temp, jsonDesignator, routeObject, routingContext);
    }


    /**
     *
     * Form API response based on Route object data
     *
     * @param returnValueConfig - String describing return value/object
     * @param jsonDesignator - object/array
     * @param routeObject - single route configuration
     * @param routingContext - @RoutingContext object
     */
    private void formResponse (String[] returnValueConfig, String jsonDesignator, JsonObject routeObject, RoutingContext routingContext) {

        // Return predefined data
        if (returnValueConfig[0].equalsIgnoreCase(KeysUtils.DATA)) {
            String body = "";
            if (jsonDesignator.equalsIgnoreCase(KeysUtils.OBJECT)) {
                body = routeObject.getJsonObject(KeysUtils.RETURN_OBJECT).encodePrettily();
            } else if (jsonDesignator.equalsIgnoreCase(KeysUtils.ARRAY)) {
                body = routeObject.getJsonArray(KeysUtils.RETURN_OBJECT).encodePrettily();
            }
            routingContext.response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .setStatusCode(routeObject.getInteger(KeysUtils.STATUS))
                    .end(body);
        // Return value from variable
        } else if (returnValueConfig[0].equalsIgnoreCase(KeysUtils.VARIABLE)) {
            String body = variables.get(returnValueConfig[1]);
            routingContext.response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .setStatusCode(routeObject.getInteger(KeysUtils.STATUS))
                    .end(body);
        // Invoke java method
        } else if (returnValueConfig[0].equalsIgnoreCase(KeysUtils.METHOD)) {
            String[] invocation = returnValueConfig[1].split(KeysUtils.METHOD_DELIMITER);
            try {
                Class c = Class.forName(invocation[0]);
                Object obj = c.newInstance();
                Method m = c.getMethod(invocation[1], RoutingContext.class);
                m.invoke(obj, routingContext);
            } catch (Exception e) {
                routingContext.response().setStatusCode(500).end(e.getMessage());
            }
        }
    }

}
