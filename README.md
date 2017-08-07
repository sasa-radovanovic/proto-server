
# proto-server
Fast prototype tool - define your endpoints in json

Although there are more than a few tools similar (or better) to this one, I am putting this one here if anyone needs it. Basically, the idea behind it is that if you need backend server (with API endpoints) in place but you do not want to set up express, spring or any other server, you can just define your endpoints in a single configuration file and have it up and running in seconds. This can be particularly useful if you're doing frontend development.


How to use it?
-------------

Clone this repository. If you do not want to do any changes (check Custom endpoint handling below), you can just take already prepared 'fat jar' from target directory. You need to have Java 8 installed on your machine and then you just invoke executable jar by providing path to your config file:

> java -jar target/proto-server-1.0-fat.jar -conf src/main/conf/config.json

####  Configuration file

Configuration can be on whatever path you want it. [The one from example above is available in src/main/conf directory and you can use it as a role mode.](https://github.com/sasa-radovanovic/proto-server/blob/master/src/main/conf/config.json)

The file should consist of following:

> {
  "port": 8083,
  "routes": [
	...
 
 If you do not define port - server will run on default port (8080).
 
####  Define routes (endpoints)

One route in array is composed of:

>    "path": "/api/test", (required)
      "method": "GET", (required)
      "returnType": "method|MethodHandler.handleMethod" (required)
	 "saveToVariable": "var1|object", (optional)
	 "returnObject": 
	        "saved1": "sasa",
	        "saved2": "sasa2"
     } (optional)

"path" - give a path to your endpoint (in the above example it will be available at http://localhost:8083/api/test)

"method" - POST/GET/PUT/DELETE, you can define the same path twice (once as GET and once as POST i.e.)

"returnType" - There are 3 available options:
1.	predefined JSON data - "data|object" or "data|array"
2.	In-memory variable - "variable|var1"
3.	Java method accepting RoutingContext object - "method|MethodHandler.handleMethod"  (check Custom endpoint handling below)

"saveToVariable" - On POST/PUT objects you may want to save your body to in-memory variable which can be retrieved as a return value in a same request or any other one. I.e. if you defined "var1|object" = JSON object from your request body will be saved to in memory variable called "var1" which you can retrieve in other endpoint by stating: "returnType": "variable|var1"

"returnObject" - if you defined returnType as data|object or data|array you need to provide the data (JSON object or array) which will be returned as a response. 

####  Custom endpoint handling
----------

Using Java reflection API you can handle any endpoint in your own method. In this case you should add your class to source, i.e. Awesome. In this class you define a public method which takes RoutingContext as a parameter:

```java
 public class Awesome {

    public void myHandler(RoutingContext routingContext) {
    ...
```

In configuration file as a return type you will add:

>  "returnType": "method|Awesome.myHandler"

And your method will be automatically invoked on a given route after a clean "mvn clean install" build and app start (java -jar proto-server-1.0-fat.jar).

Use cases
-------------

Besides developing frontend and having backend server in place, this server can be used in small-scale production level apps (with addition of authentication, authorization, db...).


Tech stack
--------------------

App was developed using Vertx 3.4.


Credits
--------------------

Sasa Radovanovic
sasa1kg@yahoo.com
sasa.radovanovic@live.com

Don't forget - Eat, sleep, fly, repeat! :)
