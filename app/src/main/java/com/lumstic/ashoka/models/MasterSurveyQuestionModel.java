package com.lumstic.ashoka.models;


public class MasterSurveyQuestionModel {
    Object object;
    int type;

    public MasterSurveyQuestionModel(Object object, int type) {
        this.object = object;
        this.type = type;
    }

    public Object getObject() {
        return object;
    }

    public int getType() {
        return type;
    }


}
