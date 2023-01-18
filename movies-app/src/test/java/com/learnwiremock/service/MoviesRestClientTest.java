package com.learnwiremock.service;

import com.github.jenspiegsa.wiremockextension.ConfigureWireMock;
import com.github.jenspiegsa.wiremockextension.InjectServer;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.learnwiremock.dto.Movie;
import com.learnwiremock.exception.MovieErrorResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.learnwiremock.constants.MovieAppConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(WireMockExtension.class)
public class MoviesRestClientTest {

    private MoviesRestClient moviesRestClient;
    private WebClient webClient;

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
        String baseUrl = String.format("http://localhost:%s/", port);
        webClient = WebClient.create(baseUrl);
        moviesRestClient = new MoviesRestClient(webClient);
    }

    @Test
    @Order(1)
    @DisplayName("Create the very first stub!")
    void retrieveAllMovies_anyUrl() {
        //given
        stubFor(get(anyUrl())
                .willReturn(aResponse()
                        .withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("all-movies.json")));

        //when
        List<Movie> movieList = moviesRestClient.retrieveAllMovies();
        System.out.println("movieList : " + movieList);

        //then
        assertFalse(movieList.isEmpty());
    }

    @Test
    @Order(2)
    void retrieveAllMovies_matchesUrl() { // 2 - même chose, mais en matchant une url exacte
        //given
        stubFor(get(urlPathEqualTo(GET_ALL_MOVIES_V1))
                .willReturn(aResponse()
                        .withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("all-movies.json")));
        //when
        List<Movie> movieList = moviesRestClient.retrieveAllMovies();
        System.out.println("movieList : " + movieList);

        //then
        assertFalse(movieList.isEmpty());
    }

    @Test
    @Order(3)
    void retrieveMovieById() { // 3 - utilisation de urlPathMatching
        //given
        stubFor(get(urlPathMatching("/movieservice/v1/movie/[0-9]"))
                .willReturn(aResponse()
                        .withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("movie.json")));
        Integer movieId = 9;

        //when
        Movie movie = moviesRestClient.retrieveMovieById(movieId);

        //then
        assertEquals("Batman Begins", movie.getName());
    }

    @Test
    @Order(4)
    void retrieveMovieById_responseTemplating() {
        //given
        stubFor(get(urlPathMatching("/movieservice/v1/movie/[0-9]"))
                .willReturn(aResponse()
                        .withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("movie-template.json")));
        Integer movieId = 8;

        //when
        Movie movie = moviesRestClient.retrieveMovieById(movieId);

        //then
        assertEquals("Batman Begins", movie.getName());
        assertEquals(8, movie.getMovie_id());
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

        //when
        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveMovieById(movieId));
    }

    @Test
    @Order(6)
    void retrieveMovieByName() {
        //given
        String avengers = "Avengers";
        stubFor(get(urlEqualTo(MOVIE_BY_NAME_QUERY_PARAM_V1 + "?movie_name=" + avengers))
                .willReturn(aResponse()
                        .withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("avengers.json")));
        //when
        List<Movie> movieList = moviesRestClient.retrieveMovieByName(avengers);

        //then
        String expectedCastName = "Robert Downey Jr, Chris Evans , Chris HemsWorth";
        assertEquals(4, movieList.size());
        assertEquals(expectedCastName, movieList.get(0).getCast());
    }

    @Test
    @Order(7)
    void retrieveMovieByName_responseTemplating() {
        //given
        String avengers = "Avengers";
        stubFor(get(urlEqualTo(MOVIE_BY_NAME_QUERY_PARAM_V1 + "?movie_name=" + avengers))
                .willReturn(aResponse()
                        .withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("movie-byname-template.json")));
        //when
        List<Movie> movieList = moviesRestClient.retrieveMovieByName(avengers);

        //then
        String expectedCastName = "Robert Downey Jr, Chris Evans , Chris HemsWorth";
        assertEquals(4, movieList.size());
        assertEquals(expectedCastName, movieList.get(0).getCast());
    }

    @Test
    @Order(8)
    void retrieveMovieByName_approach2() {
        //given
        String avengers = "Avengers";
        stubFor(get(urlPathEqualTo(MOVIE_BY_NAME_QUERY_PARAM_V1))
                .withQueryParam("movie_name", equalTo(avengers))
                .willReturn(aResponse()
                        .withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("avengers.json")));
        //when
        List<Movie> movieList = moviesRestClient.retrieveMovieByName(avengers);

        //then
        String expectedCastName = "Robert Downey Jr, Chris Evans , Chris HemsWorth";
        assertEquals(4, movieList.size());
        assertEquals(expectedCastName, movieList.get(0).getCast());
    }

    @Test
    void retrieveMovieByName_Not_Found() {
        //given
        String movieName = "ABC";
        stubFor(get(urlEqualTo(MOVIE_BY_NAME_QUERY_PARAM_V1+"?movie_name="+movieName))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.NOT_FOUND.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("404-movieName.json")));

        //whenretrieveMovieByYear
        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveMovieByName(movieName));
    }


    @Test
    void retrieveMovieByYear() {
        //given
        Integer year = 2012;
        stubFor(get(urlEqualTo(MOVIE_BY_YEAR_QUERY_PARAM_V1+"?year="+year))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("year-template.json")));
        //when
        List<Movie> movieList = moviesRestClient.retrieveMovieByYear(year);

        //then
        assertEquals(2, movieList.size());
    }

    @Test
    void retrieveMovieByYear_Not_Found() {
        //given
        Integer year = 1950;
        stubFor(get(urlEqualTo(MOVIE_BY_YEAR_QUERY_PARAM_V1+"?year="+year))
                .withQueryParam("year", equalTo(year.toString()))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.NOT_FOUND.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("404-movieyear.json")));
        //when
        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveMovieByYear(year));
    }

    @Test
    @Order(9)
    void addMovie() {
        //given
        Movie toyStory = new Movie(null, "Toy Story 4", 2019, "Tom Hanks, Tim Allen", LocalDate.of(2019, 06, 20));
        stubFor(post(urlPathEqualTo(ADD_MOVIE_V1))
                .willReturn(WireMock.aResponse()
                        .withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("add-movie.json")));

        //when
        Movie movie = moviesRestClient.addNewMovie(toyStory);

        //then
        assertNotNull(movie.getMovie_id());
    }

    @Test
    @Order(10)
    void addNewMovie() {
        //given
        Movie toyStory = new Movie(null, "Toy Story 4", 2019, "Tom Hanks, Tim Allen", LocalDate.of(2019, 06, 20));
        stubFor(post(urlPathEqualTo(ADD_MOVIE_V1))
                .withRequestBody(matchingJsonPath(("$.name")))
                .withRequestBody(matchingJsonPath(("$.cast")))
                .willReturn(WireMock.aResponse()
                        .withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("add-movie.json")));

        //when
        Movie movie = moviesRestClient.addNewMovie(toyStory);

        //then
        assertNotNull(movie.getMovie_id());
    }

    @Test
    @Order(11)
    void addNewMovieWithRequestBody() {
        //given
        Movie toyStory = new Movie(null, "Toy Story 4", 2019, "Tom Hanks, Tim Allen", LocalDate.of(2019, 06, 20));
        stubFor(post(urlPathEqualTo(ADD_MOVIE_V1))
                .withRequestBody(matchingJsonPath(("$.name"), equalTo("Toy Story 4")))
                .withRequestBody(matchingJsonPath(("$.cast"), containing("Tom")))
                .willReturn(WireMock.aResponse()
                        .withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("add-movie.json")));

        //when
        Movie movie = moviesRestClient.addNewMovie(toyStory);

        //then
        assertNotNull(movie.getMovie_id());
    }

    @Test
    @Order(12)
    void addMovie_responseTemplating() {
        //given
        Movie toyStory = new Movie(null, "Toy Story 4", 2019, "Tom Hanks, Tim Allen", LocalDate.of(2019, 06, 20));
        stubFor(post(urlPathEqualTo(ADD_MOVIE_V1))
                .withRequestBody(matchingJsonPath(("$.name"), equalTo("Toy Story 4")))
                .withRequestBody(matchingJsonPath(("$.cast"), containing("Tom")))
                .willReturn(WireMock.aResponse()
                        .withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("add-movie-template.json")));

        //when
        Movie movie = moviesRestClient.addNewMovie(toyStory);
        System.out.println(movie);

        //then
        assertNotNull(movie.getMovie_id());
    }

    @Test
    @Order(13)
    void addNewMovie_InvalidInput() {
        //given
        stubFor(post(urlPathEqualTo(ADD_MOVIE_V1))
                .withRequestBody(matchingJsonPath(("$.cast"), containing("Tom")))
                .willReturn(WireMock.aResponse()
                        .withStatus(BAD_REQUEST.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("404-invalid-input.json")));
        //when
        Movie toyStory = new Movie(
                null,
                null,
                2019,
                "Tom Hanks, Tim Allen",
                LocalDate.of(2019, 06, 20));
        String expectedErrorMessage = "Please pass all the input fields : [name]";
        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.addNewMovie(toyStory), expectedErrorMessage);
    }

    @Test
    void updateMovie() {
        //given
        Integer movieId = 3;
        String cast = "ABC";
        Movie darkNightRises = new Movie(null, null, null, cast, null);

        stubFor(put(urlPathMatching("/movieservice/v1/movie/[0-9]+"))
                .withRequestBody(matchingJsonPath(("$.cast"), containing(cast)))
                .willReturn(WireMock.aResponse()
                        .withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("updatemovie-template.json")));

        //when
        Movie updatedMovie = moviesRestClient.updateMovie(movieId, darkNightRises);

        //then
        String updatedCastName = "Christian Bale, Heath Ledger , Michael Caine, Tom Hardy";
        assertTrue(updatedMovie.getCast().contains(cast));
    }

    @Test
    void updateMovie_Not_Found() {
        //given
        Integer movieId = 100;
        String cast = "ABC";
        Movie darkNightRises = new Movie(null, null, null, cast, null);

        // On crée un stub qui ne renvoi rien dans le body
        stubFor(put(urlPathMatching("/movieservice/v1/movie/[0-9]+"))
                .withRequestBody(matchingJsonPath(("$.cast"), containing(cast)))
                .willReturn(WireMock.aResponse()
                        .withStatus(NOT_FOUND.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));

        //when
        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.updateMovie(movieId, darkNightRises));
    }

    @Test
    void deleteMovie() {
        //given
        Movie toyStory = new Movie(null, "Toy Story 5", 2019, "Tom Hanks, Tim Allen", LocalDate.of(2019, 06, 20));

        stubFor(post(urlPathEqualTo(ADD_MOVIE_V1))
                .withRequestBody(matchingJsonPath(("$.name"), equalTo("Toy Story 5")))
                .withRequestBody(matchingJsonPath(("$.cast"), containing("Tom")))
                .willReturn(WireMock.aResponse()
                        .withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("add-movie-template.json")));
        Movie addedMovie = moviesRestClient.addNewMovie(toyStory);

        String expectedErrorMessage = "Movie Deleted Successfully";
        stubFor(delete(urlPathMatching("/movieservice/v1/movie/[0-9]+"))
                .willReturn(aResponse()
                        .withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(expectedErrorMessage)));
        //when
        String response = moviesRestClient.deleteMovie(addedMovie.getMovie_id().intValue());

        //then
        assertEquals(expectedErrorMessage, response);
    }

    @Test
    void deleteMovie_notFound() {
        //given
        Integer movieId = 100;
        stubFor(delete(urlPathMatching("/movieservice/v1/movie/[0-9]+"))
                .willReturn(aResponse()
                        .withStatus(NOT_FOUND.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));

        //when
        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.deleteMovie(movieId));
    }

}
