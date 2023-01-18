package com.learnwiremock.pairec.utils;

import java.io.Serializable;

public class Param implements Serializable {

    private static final long serialVersionUID = -4885960735257023016L;
    private String paramName;
    private String paramField;
    private String paramMessage;

    public String getParamName() {
        return this.paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getParamField() {
        return this.paramField;
    }

    public void setParamField(String paramField) {
        this.paramField = paramField;
    }

    public String getParamMessage() {
        return this.paramMessage;
    }

    public void setParamMessage(String paramMessage) {
        this.paramMessage = paramMessage;
    }

    public Param(String paramName, String paramField, String paramMessage) {
        this.paramName = paramName;
        this.paramField = paramField;
        this.paramMessage = paramMessage;
    }

    public Param(String paramName, String paramMessage) {
        this.paramName = paramName;
        this.paramField = paramName;
        this.paramMessage = paramMessage;
    }
}
