package com.learnwiremock.service;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.learnwiremock.constants.MovieAppConstants;
import com.learnwiremock.dto.Movie;
import libs.wiremock.WireMockExtension;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.SocketUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.learnwiremock.constants.MovieAppConstants.GET_ALL_MOVIES_V1;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.*;


public class MoviesWireRestClientTest {

    MoviesRestClient moviesRestClient = null;
    WebClient webClient = null;

    private static int port = SocketUtils.findAvailableTcpPort();

    @RegisterExtension
    static WireMockExtension server = new WireMockExtension(port);

    @BeforeEach
    void setUp() {
        final String baseUrl = String.format("http://localhost:%s/", port);
        System.out.println("baseUrl : " + baseUrl);
        webClient = WebClient.create(baseUrl);
        moviesRestClient = new MoviesRestClient(webClient);
    }

    @Test
    void retrieveAllMoviesFromAnyUrl() {

        //given
        stubFor(get(anyUrl())
            .willReturn(WireMock.aResponse()
                .withStatus(OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBodyFile("all-movies.json")));

        //when
        List<Movie> movieList = moviesRestClient.retrieveAllMovies();
        System.out.println("movieList : " + movieList);

        //then
        assertTrue(!movieList.isEmpty());
    }

    @Test
    void retrieveAllMovies_matchesUrl() {

        //given
        stubFor(get(urlPathEqualTo(GET_ALL_MOVIES_V1))
                .willReturn(WireMock.aResponse()
                        .withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("all-movies.json")));
        //when
        List<Movie> movieList = moviesRestClient.retrieveAllMovies();

        //then
        assertTrue(!movieList.isEmpty());
    }

}
