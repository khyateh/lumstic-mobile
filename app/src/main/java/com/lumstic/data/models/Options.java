package com.lumstic.data.models;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Options implements Serializable,Comparable<Options>  {
    int orderNumber;
    int id;
    int questionId;
    String content;
    List<Questions> questionsList = new ArrayList<>();
    List<Categories> categoriesList = new ArrayList<>();

//TODO implement equals
    public boolean equals(Object obj){
        boolean isequal = false;
        if(this == obj){
            isequal = true;
        }
        if((obj != null) && !(obj.getClass() != this.getClass())){
            isequal = true;
        }
        Options optionObj = (Options)obj;
        if(this.getOrderNumber() == optionObj.getOrderNumber()){
            isequal = true;
        }
        return isequal;
    }

    public int hashCode() {
        return this.getOrderNumber();
    }

    public int compareTo(Options optionsobj) {

        int retCompareResult = 0;
        if(this.getOrderNumber() > optionsobj.getOrderNumber()){
            retCompareResult = 1;
        }else if(this.getOrderNumber() < optionsobj.getOrderNumber()){retCompareResult = -1;}
            return retCompareResult;
    }




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
