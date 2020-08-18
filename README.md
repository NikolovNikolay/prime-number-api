Prime Numbers API
=================

The API will check if a provided number in range between `2 and 10 000 000` is prime number or find the next prime number that is bigger than the provided number.

Set-up
------
1. Checkout the project locally
2. Install Docker if you already have not done that
3. Navigate to project directory
4. Build project with Gradle: `./gradlew clean build --stacktrace`
5. Run project: `docker-compose up --build` or (`./gradlew clean build && docker-compose up --build`). This will map the following external ports:
   * API on port `8080`
   * PostgreSQL on port `5432`
   * Prometheus on port `9090`
   * Redis on port `6379`  
   It is assumed those ports are available. ()
6. Project should be listening on local port`8080`

API documentation
-----------------
After running the project you can access Swagger here -> [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

Application metrics
-------------------
Prometheus is accessible here -> [http://localhost:9090](http://localhost:9090)

* Prime numbers calculation time metric -> [http://localhost:9090/graph?g0.range_input=1h&g0.expr=primenumber_calculation_seconds_max&g0.tab=0](http://localhost:9090/graph?g0.range_input=1h&g0.expr=primenumber_calculation_seconds_max&g0.tab=0)
* API request count -> [http://localhost:9090/graph?g0.range_input=1h&g0.expr=request_counter_total&g0.tab=0](http://localhost:9090/graph?g0.range_input=1h&g0.expr=request_counter_total&g0.tab=0)
* Provided numbers count -> [http://localhost:9090/graph?g0.range_input=1h&g0.expr=request_counter_total&g0.tab=0](http://localhost:9090/graph?g0.range_input=1h&g0.expr=request_counter_total&g0.tab=0)

##Next steps
1. There is a PostgreSQL database running on port `5432`. It has a generated schema with `User`, `Permission` tables and their many-to-many relation.
Having this, authentication/authorization request filters and authentication provider can be easily set-up, if we want to let only the registered users to access the API.
2. Prometheus supports alerting. They can be set-up for some critical errors.
3. We can attach Grafana to the Prometheus datasource.
4. Calculations of prime numbers can be extended to support `Integer.MAX_VALUE` or a bigger `Long` instead of `Integer`.