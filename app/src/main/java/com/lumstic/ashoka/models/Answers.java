package com.lumstic.ashoka.models;

public class Answers {
    int recordId;
    long updatedAt;
    int id;
    int responseId;
    int questionId;
    String image;
    String content;
    String type;

    public Answers() {
    }

    public Answers(int recordId, int responseId, int questionId, String content, long updatedAt) {
        this.recordId = recordId;
        this.responseId = responseId;
        this.questionId = questionId;
        this.content = content;
        this.updatedAt = updatedAt;
    }

    public Answers(int recordId, int responseId, int questionId, String content, long updatedAt, String type) {
        this.recordId = recordId;
        this.responseId = responseId;
        this.questionId = questionId;
        this.content = content;
        this.updatedAt = updatedAt;
        this.type = type;
    }

    public Answers(int recordId, int responseId, int questionId, String content, long updatedAt, String type, String image) {
        this.recordId = recordId;
        this.responseId = responseId;
        this.questionId = questionId;
        this.content = content;
        this.updatedAt = updatedAt;
        this.type = type;
        this.image = image;
    }


    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }


    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getResponseId() {
        return responseId;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
