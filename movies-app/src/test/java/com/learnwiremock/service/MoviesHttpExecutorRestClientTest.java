package com.learnwiremock.service;

import com.github.jenspiegsa.wiremockextension.ConfigureWireMock;
import com.github.jenspiegsa.wiremockextension.InjectServer;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.learnwiremock.dto.Movie;
import com.learnwiremock.exception.MovieErrorResponse;
import com.learnwiremock.pairec.HttpExecutor;
import com.learnwiremock.pairec.utils.Request;
import io.vavr.collection.List;
import io.vavr.control.Either;
import io.vavr.control.Try;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownHttpStatusCodeException;

import java.util.concurrent.Executors;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.learnwiremock.constants.MovieAppConstants.GET_ALL_MOVIES_V1;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(WireMockExtension.class)
public class MoviesHttpExecutorRestClientTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MoviesHttpExecutorRestClientTest.class);

    private HttpExecutor httpExecutor;
    private String baseUrl;

    @InjectServer
    WireMockServer wireMockServer;

    @ConfigureWireMock
    Options options = wireMockConfig()
            .port(8088)
            .notifier(new ConsoleNotifier(true)) // permet d'afficher la valeur des stubs
            .extensions(new ResponseTemplateTransformer(true));

    @BeforeEach
    void setUp() {
        int port = wireMockServer.port();
        baseUrl = String.format("http://localhost:%s", port);
        httpExecutor = new HttpExecutor(
                Executors.newSingleThreadExecutor(),
                new RestTemplate()
        );
    }

    @Test
    @Order(1)
    @DisplayName("Create the very first stub!")
    void retrieveAllMovies_matchesUrl() {
        //given
        stubFor(get(urlPathEqualTo(GET_ALL_MOVIES_V1))
                .willReturn(aResponse()
                        .withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("all-movies.json")));

        String uri = baseUrl + "/movieservice/v1/allMovies";
        //when
        List<Movie> movieList = httpExecutor
                .callAsync(uri, Movie[].class)
                .thenApply(tryPres -> tryPres.map(List::of))
                .toCompletableFuture().join().get();
        System.out.println("movieList : " + movieList);

        //then
        assertTrue(movieList.size() > 0);
    }

    @Test
    @Order(2)
    void retrieveMovieById() {
        stubFor(get(urlPathMatching("/movieservice/v1/movie/[0-9]"))
                .willReturn(aResponse()
                        .withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("movie.json")));

        Integer movieId = 9;
        String uri = String.format("http://localhost:8088//movieservice/v1/movie/%d", movieId);
        //when
        Movie movie = httpExecutor
                .callAsync(uri, Movie.class)
                .toCompletableFuture().join()
                .onFailure(err -> LOGGER.error("Failed to fetch movie : ", err))
                .onSuccess(ok -> LOGGER.info("Successfully called movie endpoint"))
                .get();

        System.out.println("movie : " + movie);

        //then
        assertEquals("Batman Begins", movie.getName());
    }

    @Test
    @Order(5)
    void retrieveMovieById_NotFound_andThrow() {
        //given
        stubFor(get(urlPathMatching("/movieservice/v1/movie/[0-9]+"))
                .willReturn(aResponse()
                        .withStatus(NOT_FOUND.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("404-movieId.json")));
        Integer movieId = 100;
        String uri = String.format("http://localhost:8088//movieservice/v1/movie/%d", movieId);

        //when
        assertThrows(MovieErrorResponse.class, () -> getFailed_to_fetch_movie(uri));
    }

    private Try<Movie> getFailed_to_fetch_movie(String uri) {
        return httpExecutor
                .callAsync(uri, Movie.class, Request.buildRequest(null, null))
                .toCompletableFuture().join()
                .onFailure(err -> {
                    LOGGER.error("Failed to fetch movie");
                    if (err instanceof HttpClientErrorException) {
                        HttpClientErrorException httpClientErr = (HttpClientErrorException) err;
                        LOGGER.error("HttpClientErrorException - \nError Response Body is {}", httpClientErr.getResponseBodyAsString());
                        throw new MovieErrorResponse(httpClientErr.getStatusText(), httpClientErr);
                    } else if (err instanceof HttpServerErrorException) {
                        HttpServerErrorException httpServerErr = (HttpServerErrorException) err;
                        LOGGER.error("HttpServerErrorException - \nError Response Body is {}", httpServerErr.getResponseBodyAsString());
                        throw new MovieErrorResponse(httpServerErr.getStatusText(), httpServerErr);
                    } else if (err instanceof UnknownHttpStatusCodeException) {
                        LOGGER.error("UnknownHttpStatusCodeException : ", err);
                    }
                });
    }

    @Test
    @Order(5)
    void retrieveMovieById_NotFound() {
        //given
        stubFor(get(urlPathMatching("/movieservice/v1/movie/[0-9]+"))
                .willReturn(aResponse()
                        .withStatus(NOT_FOUND.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("404-movieId.json")));
        Integer movieId = 100;
        String uri = String.format("http://localhost:8088//movieservice/v1/movie/%d", movieId);

        Try<Movie> movieTry = httpExecutor
                .callAsync(uri, Movie.class, Request.buildRequest(null, null))
                .toCompletableFuture().join();

        //when
        assertTrue(movieTry.isFailure());
        assertTrue(movieTry.getCause() instanceof HttpClientErrorException);
    }

    @Test
    @Order(5)
    void retrieveMovieById_NotFound_butOk() {
        //given
        stubFor(get(urlPathMatching("/movieservice/v1/movie/[0-9]+"))
                .willReturn(aResponse()
                        .withStatus(NOT_FOUND.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("404-movieId.json")));
        Integer movieId = 100;
        String uri = String.format("http://localhost:8088//movieservice/v1/movie/%d", movieId);

        Try<Movie> movieTry = httpExecutor
                .callAsync(uri, Movie.class, e -> {
                    if (e instanceof HttpClientErrorException) {
                        return Try.of(() -> new ResponseEntity<>(OK));
                    }
                    return Try.failure(e);
                })
                .toCompletableFuture().join();

        //when
        assertTrue(movieTry.isSuccess());
        assertNull(movieTry.get());
    }

    @Test
    @Order(5)
    void retrieveMovieById_NotFound_toEither() {
        //given
        stubFor(get(urlPathMatching("/movieservice/v1/movie/[0-9]+"))
                .willReturn(aResponse()
                        .withStatus(NOT_FOUND.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("404-movieId.json")));
        Integer movieId = 100;
        String uri = String.format("http://localhost:8088//movieservice/v1/movie/%d", movieId);

        Try<Movie> movieTry = httpExecutor
                .callAsync(uri, Movie.class, Request.buildRequest(null, null))
                .toCompletableFuture().join();

        //Either<Throwable, Movie> movies = movieTry.toEither();
        Either<IllegalArgumentException, Movie> moviesWithLeft = movieTry.toEither(new IllegalArgumentException());

        //when
        assertTrue(moviesWithLeft.isLeft());
        assertTrue(moviesWithLeft.getLeft() instanceof IllegalArgumentException);
    }
}
