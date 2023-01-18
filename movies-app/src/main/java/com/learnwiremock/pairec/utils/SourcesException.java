package com.learnwiremock.pairec.utils;

import java.io.Serializable;
import java.util.List;

public class SourcesException implements Serializable {
    private static final long serialVersionUID = 3543665878842546889L;
    private String originObjectName;
    private String originTargetPath;
    private String errorCode;
    private String message;
    private String description;
    private List<Param> params;

    public SourcesException() {
    }

    public String getOriginObjectName() {
        return this.originObjectName;
    }

    public void setOriginObjectName(String originObjectName) {
        this.originObjectName = originObjectName;
    }

    public String getOriginTargetPath() {
        return this.originTargetPath;
    }

    public void setOriginTargetPath(String originTargetPath) {
        this.originTargetPath = originTargetPath;
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
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

    public List<Param> getParams() {
        return this.params;
    }

    public void setParams(List<Param> params) {
        this.params = params;
    }
}