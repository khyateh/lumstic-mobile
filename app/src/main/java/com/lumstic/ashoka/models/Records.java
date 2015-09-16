package com.lumstic.ashoka.models;


public class Records {
    int id;

    public Records(int categoryId, int responseId) {
        this.categoryId = categoryId;
        this.responseId = responseId;
    }

    int responseId;
    int categoryId;
    int webId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public int getResponseId() {
        return responseId;
    }

    public void setResponseId(int responseId) {
        this.responseId = responseId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getWebId() {
        return webId;
    }

    public void setWebId(int webId) {
        this.webId = webId;
    }
}
