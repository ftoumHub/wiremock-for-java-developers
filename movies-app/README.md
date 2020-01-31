# MovieApp

-   This is a application which connects to the Movies RESTFUL Service.

## How to enable JUnit5?

-   Please make the below changes to enable JUnit5 in your project.

### build.gradle

-   Add the below code to enable Junit5 as a test platform.

```youtrack
test {
    useJUnitPlatform() // enables Junit5
}
```
-   Add this dependency to use the Junit% 

```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-engine</artifactId>
    <version>5.5.1</version>
    <scope>test</scope>
</dependency>

```

# Movies RestFul WebService

## How to Run the app?

- Run the below command in your machine. You must have java8 or higher to run this application.

```
cd movies-restful-service
java -jar movies-restful-service.jar
```

## Swagger Link

The below link will launch the swagger of the movies-restful-web-service.

http://localhost:8081/movieservice/swagger-ui.html#/