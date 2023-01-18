package com.learnwiremock.pairec.utils;

import io.vavr.collection.List;
import io.vavr.control.Option;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.*;

import static java.util.Objects.isNull;

@FieldDefaults(makeFinal = true, level = AccessLevel.PUBLIC)
@Data
@AllArgsConstructor
@Builder(toBuilder = true, builderMethodName = "newBuilder")
public final class Request {

    public final String token;
    public final Map<String, String> headers;
    public final String idEmployeMaif;
    public final String username;
    public final String numeroSocietaireMaif;
    public final List<String> authorities;

    public static Request buildRequest(Map<String, String> headers,
                                       UsernamePasswordAuthenticationToken tokenAuthent) {
        if (isNull(headers) || headers.isEmpty() || isNull(tokenAuthent)) {
            return Request.newBuilder().headers(Collections.emptyMap()).build();
        }

        final Map<String, String> headersInsensitive = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        headersInsensitive.putAll(headers);

        String token = Option.of(tokenAuthent)
                .map(UsernamePasswordAuthenticationToken::getCredentials)
                .map(Object::toString).getOrNull();

        ApiUserDetails principal = (ApiUserDetails) Option.of(tokenAuthent)
                .map(UsernamePasswordAuthenticationToken::getPrincipal)
                .getOrNull();

        if (isNull(principal)) {
            return Request.newBuilder().build();
        }
        List<String> authorities = List.ofAll(tokenAuthent.getAuthorities()).map(GrantedAuthority::getAuthority);

        return Request.newBuilder()
                .token(token)
                .headers(headersInsensitive)
                .idEmployeMaif(principal.getIdEmployeMaif())
                .username(principal.getUsername())
                .numeroSocietaireMaif(principal.getNumeroSocietaireMaif())
                .authorities(authorities)
                .build();
    }

    public Optional<String> tokenValue() {
        return Optional.ofNullable(token).map("Bearer "::concat);
    }

    public String getCodeCanal() {
        return this.getHeaders().getOrDefault(HeaderConstants.CODE_CANAL, HeaderConstants.CODE_CANAL_DEFAUT);
    }

    public String getCodeGuichet() {
        return this.getHeaders().getOrDefault(HeaderConstants.CODE_GUICHET, HeaderConstants.CODE_GUICHET_DEFAUT);
    }

}
