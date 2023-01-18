Utilisation de java 8 avec jenv:

brew install --cask homebrew/cask-versions/adoptopenjdk8

jenv add /Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/
openjdk64-1.8.0.292 added
1.8.0.292 added
1.8 added
1.8.0.292 already present, skip installation

La command jenv versions donne ensuite :
* system (set by /Users/75774P/.jenv/version)
  1.8
  1.8.0.292
  11.0
  11.0.16.1
  17.0
  17.0.5
  openjdk64-1.8.0.292
  openjdk64-11.0.16.1
  openjdk64-17.0.5

# Movies RestFul WebService

## How to Run the app?

- Run the below command in your machine. You must have java8 or higher to run this application.

```
java -jar -Dserver.port=8083 movies-restful-service/movies-restful-service-java8.jar
```

## Swagger Link

The below link will launch the swagger of the movies-restful-web-service.

http://localhost:8083/movieservice/swagger-ui.html#/

## Resources :

https://github.com/code-with-dilip/learn-wiremock

https://blog.testproject.io/2020/01/20/increasing-test-efficiency-with-service-virtualization/

https://blog.testproject.io/2020/02/06/mocking-at-the-unit-test-level-with-mockito-part-2/

https://blog.testproject.io/2020/02/24/getting-started-with-service-virtualization-using-wiremock-part-3/

https://blog.testproject.io/2020/03/09/simulating-rich-behaviour-using-advanced-wiremock-features-part-4/