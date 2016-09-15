package com.lumstic.data.models;

public class TagModel {
    int qID;
    int recordID;
    Questions ques;

    public TagModel(int qID, int recordID, Questions ques) {
        this.qID = qID;
        this.recordID = recordID;
        this.ques = ques;
    }

    public TagModel(int qID, int recordID) {
        this.qID = qID;
        this.recordID = recordID;
    }

    public int getqID() {
        return qID;
    }

    public void setqID(int qID) {
        this.qID = qID;
    }

    public int getRecordID() {
        return recordID;
    }

    public void setRecordID(int recordID) {
        this.recordID = recordID;
    }

    public Questions getQues() {
        return ques;
    }

    public void setQues(Questions ques) {
        this.ques = ques;
    }
}
