package com.lumstic.data.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Questions implements Serializable {

    int id;
    String imageUrl;
    int orderNumber;
    int identifier;
    int parentId;
    int minValue;
    //TODO jyothi Feb 7 number to E format issue.
    //int maxValue;
    double maxValue;

    int surveyId;
    int maxLength;
    int mandatory;
    int categoryId;
    String type;
    String content;
    List<Options> options = new ArrayList<>();

    public List<Options> getOptions() {
        return options;
    }

    public void setOptions(List<Options> options) {
        this.options = options;
        Collections.sort(options);

    }

    public int getIdentifier() {
        return identifier;
    }

    public void setIdentifier(int identifier) {
        this.identifier = identifier;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public int getMinValue() {
        return minValue;
    }

    public void setMinValue(int minValue) {
        this.minValue = minValue;
    }

    //TODO jyothi Feb 7 2017 number to E format issue.
   /* public int getMaxValue() {
        return maxValue;
    }*/
    public double getMaxValue() {
        return maxValue;
    }
//TODO jyothi Feb 7 2017 to fix number to E format
    /*public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }*/
    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSurveyId() {
        return surveyId;
    }

    public void setSurveyId(int surveyId) {
        this.surveyId = surveyId;
    }
//TODO jyothi Feb 7 number to E format issue.


    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public int getMandatory() {
        return mandatory;
    }

    public void setMandatory(int mandatory) {
        this.mandatory = mandatory;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
