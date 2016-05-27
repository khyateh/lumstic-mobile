package com.lumstic.ashoka.models;

import java.io.Serializable;
import java.util.List;

public class Surveys implements Serializable {

    int id;
    String publishedOn;
    String name;
    String description;
    String expiryDate;
    List<Questions> questionsList;
    List<Categories> categoriesList;
    List<Respondent> respondentList;

    public List<Respondent> getRespondentList() {
        return respondentList;
    }

    public void setRespondentList(List<Respondent> respondentList) {
        this.respondentList = respondentList;
    }

    private int completedSurvey, incompleteSurvey, uploadedSurvey;

    public int getCompletedSurvey() {
        return completedSurvey;
    }

    public void setCompletedSurvey(int completedSurvey) {
        this.completedSurvey = completedSurvey;
    }

    public int getUploadedSurvey() {
        return uploadedSurvey;
    }

    public void setUploadedSurvey(int uploadedSurvey) {
        this.uploadedSurvey = uploadedSurvey;
    }

    public int getIncompleteSurvey() {
        return incompleteSurvey;
    }

    public void setIncompleteSurvey(int incompleteSurvey) {
        this.incompleteSurvey = incompleteSurvey;
    }

    public List<Categories> getCategoriesList() {
        return categoriesList;
    }

    public void setCategoriesList(List<Categories> categoriesList) {
        this.categoriesList = categoriesList;
    }

    public List<Questions> getQuestionsList() {
        return questionsList;
    }

    public void setQuestionsList(List<Questions> questionsList) {
        this.questionsList = questionsList;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPublishedOn() {
        return publishedOn;
    }

    public void setPublishedOn(String publishedOn) {
        this.publishedOn = publishedOn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }
}
