package com.lumstic.ashoka.models;

public class IncompleteResponse {
    String responseNumber;
    String responseText;

    public IncompleteResponse(String responseNumber, String responseText) {
        this.responseNumber = responseNumber;
        this.responseText = responseText;
    }

    public String getResponseNumber() {
        return responseNumber;
    }

    public void setResponseNumber(String responseNumber) {
        this.responseNumber = responseNumber;
    }

    public String getResponseText() {
        return responseText;
    }

    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }
}