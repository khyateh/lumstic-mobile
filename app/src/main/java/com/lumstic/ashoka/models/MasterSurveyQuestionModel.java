package com.lumstic.ashoka.models;


public class MasterSurveyQuestionModel {
    Object object;
    int type;
    int orderNumber;

    public MasterSurveyQuestionModel(Object object, int type) {
        this.object = object;
        this.type = type;
    }

    public MasterSurveyQuestionModel(Object object, int orderNumber, int type) {
        this.object = object;
        this.orderNumber = orderNumber;
        this.type = type;
    }


    public int getOrderNumber() {
        return orderNumber;
    }

    public Object getObject() {
        return object;
    }

    public int getType() {
        return type;
    }


}
