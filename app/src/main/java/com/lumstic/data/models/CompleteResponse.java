package com.lumstic.data.models;


public class CompleteResponse {
    String responseNumber;
    String responseText;

    public CompleteResponse(String responseNumber, String responseText) {
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
