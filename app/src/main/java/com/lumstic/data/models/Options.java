package com.lumstic.data.models;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Options implements Serializable {
    int orderNumber;
    int id;
    int questionId;
    String content;
    List<Questions> questionsList = new ArrayList<>();
    List<Categories> categoriesList = new ArrayList<>();


    public List<Questions> getQuestionsList() {
        return questionsList;
    }

    public void setQuestionsList(List<Questions> questionsList) {
        this.questionsList = questionsList;
    }

    public List<Categories> getCategoriesList() {
        return categoriesList;
    }

    public void setCategoriesList(List<Categories> categories) {
        this.categoriesList = categories;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
