package com.learnwiremock.pairec;

import com.learnwiremock.pairec.utils.ClientApiException;
import com.learnwiremock.pairec.utils.HeaderConstants;
import com.learnwiremock.pairec.utils.Request;
import io.vavr.CheckedFunction0;
import io.vavr.control.Option;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@AllArgsConstructor
public class HttpExecutor implements Executor {
    private static final String MSG_NO_TOKEN = "No token found in HTTP request.";
    private static final List<Pattern> REQUIRED_HEADERS = Arrays.asList(
            Pattern.compile("^.*Authorization.*$", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^.*MAIF.*$", Pattern.CASE_INSENSITIVE));

    private final Executor executor;
    private final RestTemplate restTemplate;

    @Override
    public void execute(Runnable command) {
        executor.execute(command);
    }

    public <T> CompletionStage<T> async(CheckedFunction0<T> func) {
        return CompletableFuture.supplyAsync(() -> Try.of(func).get(), this);
    }

    public <T, B> CompletionStage<Try<T>> callAsync(String url,
                                                    HttpMethod method,
                                                    HttpHeaders headers,
                                                    B body,
                                                    Class<T> reponseType,
                                                    Function<Throwable, Try<ResponseEntity<T>>> recover,
                                                    Request request, Object... uriVariables) {
        headers.setAll(findMaifRequiredHeaders(request));
        String token = request.tokenValue().orElse(null);
        headers.set(HttpHeaders.AUTHORIZATION, token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        return this.async(() -> Try.of(() -> this.restTemplate.exchange(url, method, new HttpEntity<>(body, headers), reponseType, uriVariables))
                        .recoverWith(recover))
                .thenApply(tryResponse -> tryResponse.map(ResponseEntity::getBody));
    }

    public <T, B> CompletionStage<Try<T>> callAsync(String url,
                                                    HttpMethod method,
                                                    HttpHeaders headers,
                                                    B body,
                                                    Class<T> reponseType,
                                                    Function<Throwable, Try<ResponseEntity<T>>> recover,
                                                    Request request, Map<String, ?> uriVariables) {
        headers.setAll(findMaifRequiredHeaders(request));
        String token = request.tokenValue().orElse(null);
        headers.set(HttpHeaders.AUTHORIZATION, token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        return this.async(() -> Try.of(() -> this.restTemplate.exchange(url, method, new HttpEntity<>(body, headers), reponseType, uriVariables))
                        .recoverWith(recover))
                .thenApply(tryResponse -> tryResponse.map(ResponseEntity::getBody));
    }

    public <T, B> CompletionStage<Try<Option<T>>> callAsyncOption(String url,
                                                                  HttpMethod method,
                                                                  B body,
                                                                  Class<T> reponseType,
                                                                  Function<Throwable, Try<Option<ResponseEntity<T>>>> recover,
                                                                  Request request, Object... uriVariables) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAll(findMaifRequiredHeaders(request));
        String token = request.tokenValue().orElseThrow(() -> new ClientApiException(HttpStatus.INTERNAL_SERVER_ERROR, MSG_NO_TOKEN));
        headers.set(HttpHeaders.AUTHORIZATION, token);
        Option.of(body).forEach(b -> headers.setContentType(MediaType.APPLICATION_JSON));

        return this.async(
                        () -> Try.of(() -> Option.of(this.restTemplate.exchange(url, method, new HttpEntity<>(body, headers), reponseType, uriVariables)))
                                .recoverWith(HttpClientErrorException.class, e -> {
                                    if (HttpStatus.NOT_FOUND.value() == e.getRawStatusCode()) {
                                        return Try.success(Option.none());
                                    }
                                    return Try.failure(e);
                                }).recoverWith(recover))
                .thenApply(tryResponse -> tryResponse.map(oReponse -> oReponse.map(ResponseEntity::getBody)));
    }

    public <T, B> CompletionStage<Try<Option<T>>> callAsyncOption(String url, HttpMethod method, B body,
                                                                  Class<T> reponseType, Function<Throwable, Try<Option<ResponseEntity<T>>>> recover, Request request, Map<String, ?> uriVariables) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAll(findMaifRequiredHeaders(request));
        String token = request.tokenValue().orElseThrow(() -> new ClientApiException(HttpStatus.INTERNAL_SERVER_ERROR, MSG_NO_TOKEN));
        headers.set(HttpHeaders.AUTHORIZATION, token);
        Option.of(body).forEach(b -> headers.setContentType(MediaType.APPLICATION_JSON));

        return this.async(
                        () -> Try.of(() -> Option.of(this.restTemplate.exchange(url, method, new HttpEntity<>(body, headers), reponseType, uriVariables)))
                                .recoverWith(HttpClientErrorException.class, e -> {
                                    if (HttpStatus.NOT_FOUND.value() == e.getRawStatusCode()) {
                                        return Try.success(Option.none());
                                    }
                                    return Try.failure(e);
                                }).recoverWith(recover))
                .thenApply(tryResponse -> tryResponse.map(oReponse -> oReponse.map(ResponseEntity::getBody)));
    }

    public <T> CompletionStage<Try<T>> callAsync(
            String url,
            Class<T> reponseType,
            Function<Throwable, Try<ResponseEntity<T>>> recover,
            Object... uriVariables) {
        return callAsync(url, HttpMethod.GET, new HttpHeaders(), null, reponseType, recover, Request.buildRequest(null, null), uriVariables);
    }

    public <T> CompletionStage<Try<T>> callAsync(
            String url,
            Class<T> reponseType,
            Function<Throwable, Try<ResponseEntity<T>>> recover,
            Request request,
            Object... uriVariables) {
        return callAsync(url, HttpMethod.GET, new HttpHeaders(), null, reponseType, recover, request, uriVariables);
    }

    public <T> CompletionStage<Try<T>> callAsync(
            String url,
            Class<T> reponseType,
            Function<Throwable, Try<ResponseEntity<T>>> recover,
            Request request,
            Map<String, ?> uriVariables) {
        return callAsync(url, HttpMethod.GET, new HttpHeaders(), null, reponseType, recover, request, uriVariables);
    }

    public <T> CompletionStage<Try<T>> callAsync(String url, Class<T> reponseType, Object... uriVariables) {
        return callAsync(url, HttpMethod.GET, new HttpHeaders(), null, reponseType, Try::failure, Request.buildRequest(null, null), uriVariables);
    }

    public <T> CompletionStage<Try<T>> callAsync(String url, Class<T> reponseType, Request request, Object... uriVariables) {
        return callAsync(url, HttpMethod.GET, new HttpHeaders(), null, reponseType, Try::failure, request, uriVariables);
    }

    public <T> CompletionStage<Try<T>> callAsync(String url, Class<T> reponseType, Request request, Map<String, ?> uriVariables) {
        return callAsync(url, HttpMethod.GET, new HttpHeaders(), null, reponseType, Try::failure, request, uriVariables);
    }

    public <T, B> CompletionStage<Try<T>> callAsync(String url, B body, Class<T> reponseType, Request request, Object... uriVariables) {
        return callAsync(url, HttpMethod.POST, new HttpHeaders(), body, reponseType, Try::failure, request, uriVariables);
    }

    public <T, B> CompletionStage<Try<T>> callAsync(String url, B body, HttpHeaders headers, Class<T> reponseType, Request request,
                                                    Object... uriVariables) {
        return callAsync(url, HttpMethod.POST, headers, body, reponseType, Try::failure, request, uriVariables);
    }

    public <T, B> CompletionStage<Try<T>> callAsync(String url, B body, Class<T> reponseType, Request request, Map<String, ?> uriVariables) {
        return callAsync(url, HttpMethod.POST, new HttpHeaders(), body, reponseType, Try::failure, request, uriVariables);
    }

    public <T, B> CompletionStage<Try<T>> callAsync(String url, B body, HttpHeaders headers, Class<T> reponseType, Request request,
                                                    Map<String, ?> uriVariables) {
        return callAsync(url, HttpMethod.POST, headers, body, reponseType, Try::failure, request, uriVariables);
    }

    public <T, B> CompletionStage<Try<T>> callAsync(String url, B body, Class<T> reponseType, Function<Throwable, Try<ResponseEntity<T>>> recover,
                                                    Request request, Object... uriVariables) {
        return callAsync(url, HttpMethod.POST, new HttpHeaders(), body, reponseType, recover, request, uriVariables);
    }

    public <T, B> CompletionStage<Try<T>> callAsync(String url, B body, Class<T> reponseType, Function<Throwable, Try<ResponseEntity<T>>> recover,
                                                    Request request, Map<String, ?> uriVariables) {
        return callAsync(url, HttpMethod.POST, new HttpHeaders(), body, reponseType, recover, request, uriVariables);
    }

    public <T> CompletionStage<Try<Option<T>>> callAsyncOption(String url, Class<T> reponseType, Request request, Object... uriVariables) {
        return callAsyncOption(url, HttpMethod.GET, null, reponseType, Try::failure, request,
                uriVariables);
    }

    public <T> CompletionStage<Try<Option<T>>> callAsyncOption(String url, Class<T> reponseType, Request request, Map<String, ?> uriVariables) {
        return callAsyncOption(url, HttpMethod.GET, null, reponseType, Try::failure, request,
                uriVariables);
    }

    public <T> CompletionStage<Try<Option<T>>> callAsyncOption(String url, Class<T> reponseType,
                                                               Function<Throwable, Try<Option<ResponseEntity<T>>>> recover,
                                                               Request request, Object... uriVariables) {
        return callAsyncOption(url, HttpMethod.GET, null, reponseType, recover, request, uriVariables);
    }

    public <T, B> CompletionStage<Try<T>> callAsync(
            String url,
            HttpMethod method,
            B body,
            ParameterizedTypeReference<T> typeRefefrence,
            Request request,
            Object... uriVariables) {
        return this.async(() -> Try.of(() -> {
            HttpHeaders headers = new HttpHeaders();
            headers.setAll(findMaifRequiredHeaders(request));
            request.tokenValue().ifPresent(value -> headers.set(HttpHeaders.AUTHORIZATION, value));
            Optional.ofNullable(body).ifPresent(b -> headers.setContentType(MediaType.APPLICATION_JSON));

            ResponseEntity<T> exchange = restTemplate.exchange(url, method, new HttpEntity<>(body, headers), typeRefefrence, uriVariables);
            return exchange.getBody();
        }));
    }

    public <T, B> CompletionStage<Try<T>> callAsync(
            String url,
            HttpMethod method,
            B body,
            ParameterizedTypeReference<T> typeRefefrence,
            Request request,
            Map<String, ?> uriVariables) {
        return this.async(() -> Try.of(() -> {
            HttpHeaders headers = new HttpHeaders();
            headers.setAll(findMaifRequiredHeaders(request));
            request.tokenValue().ifPresent(value -> headers.set(HttpHeaders.AUTHORIZATION, value));
            Optional.ofNullable(body).ifPresent(b -> headers.setContentType(MediaType.APPLICATION_JSON));

            ResponseEntity<T> exchange = restTemplate.exchange(url, method, new HttpEntity<>(body, headers), typeRefefrence, uriVariables);
            return exchange.getBody();
        }));
    }

    public static Request transformRequestSync(Request request) {
        final Map<String, String> headers = new HashMap<>();
        if (Objects.nonNull(request.getHeaders())) {
            headers.putAll(request.getHeaders());
        }
        headers.put(HeaderConstants.MAIF_WAIT, HeaderConstants.MAIF_WAIT_ASYNC);
        return request.toBuilder()
                .headers(headers)
                .build();
    }

    private static Map<String, String> findMaifRequiredHeaders(Request request) {
        return request.getHeaders().entrySet()
                .stream()
                .filter(header -> REQUIRED_HEADERS.stream().anyMatch(pattern -> pattern.matcher(header.getKey()).matches()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}

