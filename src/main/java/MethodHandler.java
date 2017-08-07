import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.Date;

/**
 * Showcase of handling route in a Java method
 */
public class MethodHandler {


    /**
     *
     * Routing context holds everything you need, you reed request and send response using it
     *
     * @param routingContext - @RoutingContext object
     */
    public void handleMethod(RoutingContext routingContext) {

        Date d = new Date();
        JsonObject jo = new JsonObject();
        jo.put("date", d.toString());

        System.out.println(this.getClass().getSimpleName() + " | Handling request in a method");

        routingContext.response()
                .putHeader("content-type", "application/json; charset=utf-8")
                .setStatusCode(200)
                .end(jo.encodePrettily());
    }
}
