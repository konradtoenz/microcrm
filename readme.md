# Micro CRM

This is a little demo application simulating a CRM tool.
It's based on [Spring Boot](https://projects.spring.io/spring-boot/) and has a simple web frontend.

The application persists its data in an in-memory H2 database, so data will be lost after application shutdown.

## Run the Application

Built it with `gradlew assemble` (resulting in a runnable jar in `build/libs`),
or run it directly with `gradlew bootRun`.

The application will be available at `http://localhost:8080`.

## Configure the Application

Like [any Spring Boot application](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-external-config-command-line-args)
configuration properties can be set on the command line when starting the runnable jar. Every key-value pair starting
with `--` will be added to the configuration.

Example:  
`java -jar microcrm-0.0.1.jar --server.port=9000`  
will start the server on port 9000.

### Configuration Properties

* `server.port`  
The HTTP port the server is started on.

* `spring.datasource.url`  
JDBC URL of an external H2 database (currently not other database than H2 is supported).  
The Schema will be set up automatically by [Flyway](https://flywaydb.org/).

* `spring.datasource.username`, `spring.datasource.password`
Credentials for the external database.