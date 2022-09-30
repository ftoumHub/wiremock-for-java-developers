## Running WireMock as StandAlone

- Check this section in **PluralSight** it really explained very well.

### How to run it as standalone?

```
java -jar wiremock-standalone-2.23.2.jar
```

- Go to the below link to access the local instance of running wire mock.

```
http://localhost:8080/__admin/docs
```

## Recording and Proxying

- Recording is the concept where the the call will be actually made to the API having **wiremock** in between recording the API interactions.

### Why record an API ?

- Stub Accuracy.
- Manual Stub creations may be prone to error.
- Stub creation will be faster.

### Recording the wiremock using UI
- Start up the wiremock stand alone in your local.

- Go to the below link.

```
http://localhost:8080/__admin/recorder/
```

- In the **Target URL** provide the below url and click on the **Record** button.

```
https://www.pluralsight.com
```

- Type in **localhost:8080** , this will take you to the pluralsight website. By then all the rest api calls thats made in that screen would have been captured by the Wiremock.

- Click on the stop button and the a message of number of stubs that are recorded will be displayed.

- The one advantage of doing this will help avoid the manual effort of building these stubs.
