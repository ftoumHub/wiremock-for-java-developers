package com.learnwiremock.service;

import com.learnwiremock.dto.Movie;
import com.learnwiremock.exception.MovieErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.function.Supplier;

import static com.learnwiremock.constants.MovieAppConstants.*;

@Slf4j
public class MoviesRestClient {

    private WebClient webClient;

    public MoviesRestClient(WebClient _webClient) {
        this.webClient = _webClient;
    }

    public List<Movie> retrieveAllMovies() {
        return withExceptionHandling(
                () -> webClient.get().uri(GET_ALL_MOVIES_V1)
                        .retrieve() // actual call is made to the api
                        .bodyToFlux(Movie.class) //body is converted to flux(Represents multiple items)
                        .collectList() // collecting the httpResponse as a list\
                        .block()
        );
    }

    public Movie retrieveMovieById(Integer movieId) {
        return withExceptionHandling(
                () -> webClient.get()
                        .uri(MOVIE_BY_ID_PATH_PARAM_V1, movieId) //mapping the movie id to the url
                        .retrieve()
                        .bodyToMono(Movie.class) //body is converted to Mono(Represents single item)
                        .block()
        );
    }

    public List<Movie> retrieveMovieByName(String movieName) {

        String retrieveByNameUri = UriComponentsBuilder.fromUriString(MOVIE_BY_NAME_QUERY_PARAM_V1)
                .queryParam("movie_name", movieName)
                .buildAndExpand()
                .toUriString();

        return withExceptionHandling(
                () -> webClient.get()
                        .uri(retrieveByNameUri)
                        .retrieve()
                        .bodyToFlux(Movie.class)
                        .collectList()
                        .block()
        );
    }

    /**
     * This method makes a REST call to the Movies RESTFUL Service and retrieves a list of Movies as a response based on the year.
     *
     * @param year - Integer (Example : 2012,2013 etc.,)
     * @return - List<Movie>
     */
    public List<Movie> retrieveMovieByYear(Integer year) {
        String retrieveByYearUri = UriComponentsBuilder.fromUriString(MOVIE_BY_YEAR_QUERY_PARAM_V1)
                .queryParam("year", year)
                .buildAndExpand()
                .toUriString();

        return withExceptionHandling(
                () -> webClient.get()
                        .uri(retrieveByYearUri)
                        .retrieve()
                        .bodyToFlux(Movie.class)
                        .collectList()
                        .block()
        );
    }

    public Movie addNewMovie(Movie newMovie) {
        return withExceptionHandling(
                () -> {
                    Movie movie = webClient.post().uri(ADD_MOVIE_V1)
                            .syncBody(newMovie)
                            .retrieve()
                            .bodyToMono(Movie.class)
                            .block();
                    log.info("New Movie SuccessFully addded {} ", movie);
                    return movie;
                });
    }

    public Movie updateMovie(Integer movieId, Movie movie) {
        return withExceptionHandling(
                () -> {
                    Movie updatedMovie = webClient.put()
                            .uri(MOVIE_BY_ID_PATH_PARAM_V1, movieId)
                            .syncBody(movie)
                            .retrieve()
                            .bodyToMono(Movie.class)
                            .block();
                    log.info(" Movie SuccessFully updated {} ", updatedMovie);
                    return updatedMovie;
                });
    }

    public String deleteMovieById(Integer movieId) {
        return withExceptionHandling(
                () -> webClient.delete()
                        .uri( MOVIE_BY_ID_PATH_PARAM_V1, movieId)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block()
        );
    }

    private <T> T withExceptionHandling(Supplier<T> action) {
        try {
            return action.get();
        } catch (WebClientResponseException ex) {
            log.error("WebClientResponseException - Error Message is : {} , and the Error Response Body is {}", ex, ex.getResponseBodyAsString());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        } catch (Exception ex) {
            log.error("Exception - The Error Message is {} ", ex.getMessage());
            throw new MovieErrorResponse(ex);
        }
    }
}