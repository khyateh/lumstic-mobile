package com.lumstic.ashoka.models;


public class MasterQuestion {
    Questions questions;
    int recordID;
    int ansAndroidID;

    public MasterQuestion(Questions questions, int recordID) {
        this.questions = questions;
        this.recordID = recordID;
    }

    public MasterQuestion(Questions questions, int recordID, int ansAndroidID) {
        this.questions = questions;
        this.recordID = recordID;
        this.ansAndroidID = ansAndroidID;
    }

    public Questions getQuestions() {
        return questions;
    }

    public void setQuestions(Questions questions) {
        this.questions = questions;
    }

    public int getRecordID() {
        return recordID;
    }

    public void setRecordID(int recordID) {
        this.recordID = recordID;
    }

    public int getAnsAndroidID() {
        return ansAndroidID;
    }

}
