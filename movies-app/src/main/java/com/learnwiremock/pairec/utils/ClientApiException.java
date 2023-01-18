package com.learnwiremock.pairec.utils;

import java.util.List;

import org.springframework.http.HttpStatus;

public class ClientApiException extends RuntimeException {

    private static final long serialVersionUID = -3613886114273986504L;
    private String code;
    private String message;
    private String description;
    private HttpStatus codeHttp;
    private List<SourcesException> sources;
    private List<Param> params;
    private Object meta;

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<SourcesException> getSources() {
        return this.sources;
    }

    public void setSources(List<SourcesException> sources) {
        this.sources = sources;
    }

    public Object getMeta() {
        return this.meta;
    }

    public void setMeta(Object meta) {
        this.meta = meta;
    }

    public HttpStatus getCodeHttp() {
        return this.codeHttp;
    }

    public void setCodeHttp(HttpStatus codeHttp) {
        this.codeHttp = codeHttp;
    }

    public List<Param> getParams() {
        return this.params;
    }

    public void setParams(List<Param> params) {
        this.params = params;
    }

    public ClientApiException() {
    }

    public ClientApiException(HttpStatus codeHttp, String message) {
        this.message = message;
        this.codeHttp = codeHttp;
    }

    public ClientApiException(String code) {
        this.code = code;
    }

    public ClientApiException(String code, HttpStatus codeHttp) {
        this.code = code;
        this.codeHttp = codeHttp;
    }

    public ClientApiException(String code, HttpStatus codeHttp, List<Param> params) {
        this.code = code;
        this.codeHttp = codeHttp;
        this.params = params;
    }

    public ClientApiException(String code, HttpStatus codeHttp, Throwable cause) {
        super(cause);
        this.code = code;
        this.codeHttp = codeHttp;
    }

    public ClientApiException(String code, HttpStatus codeHttp, String description) {
        this.code = code;
        this.codeHttp = codeHttp;
        this.description = description;
    }

    public ClientApiException(String code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    public ClientApiException(String code, HttpStatus codeHttp, String description, List<Param> params) {
        this.code = code;
        this.codeHttp = codeHttp;
        this.description = description;
        this.params = params;
    }

    public ClientApiException(String code, HttpStatus codeHttp, List<Param> params, Throwable cause) {
        super(cause);
        this.code = code;
        this.codeHttp = codeHttp;
        this.params = params;
    }

    public ClientApiException(String code, String message, String description, HttpStatus codeHttp) {
        this.code = code;
        this.message = message;
        this.description = description;
        this.codeHttp = codeHttp;
    }

    public ClientApiException(String code, Throwable cause, List<SourcesException> sources) {
        super(cause);
        this.code = code;
        this.sources = sources;
    }

    public ClientApiException(String code, String message, String description, HttpStatus codeHttp, List<SourcesException> sources, List<Param> params, Object meta, Throwable cause) {
        super(cause);
        this.code = code;
        this.message = message;
        this.description = description;
        this.codeHttp = codeHttp;
        this.sources = sources;
        this.meta = meta;
        this.params = params;
    }
}
