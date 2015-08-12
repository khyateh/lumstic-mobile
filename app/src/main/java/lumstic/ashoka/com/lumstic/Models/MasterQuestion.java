package lumstic.ashoka.com.lumstic.Models;


public class MasterQuestion {
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

    Questions questions;
    int recordID;

    public MasterQuestion(Questions questions, int recordID) {
        this.questions = questions;
        this.recordID = recordID;
    }

    public MasterQuestion(Questions questions, int recordID, int ansAndroidID) {
        this.questions = questions;
        this.recordID = recordID;
        this.ansAndroidID = ansAndroidID;
    }

    public int getAnsAndroidID() {
        return ansAndroidID;
    }

    int ansAndroidID;

}
