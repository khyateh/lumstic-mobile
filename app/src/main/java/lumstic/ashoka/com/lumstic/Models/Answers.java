package lumstic.ashoka.com.lumstic.Models;

import java.util.ArrayList;

public class Answers {
    int recordId;
    int webId;
    long updated_at;
    int id;
    int responseId;
    int question_id;
    String image;
    String content;
    String type;
    ArrayList<Integer> optionIds;

    public ArrayList<Integer> getOptionIds() {
        return optionIds;
    }

    public void setOptionIds(ArrayList<Integer> optionIds) {
        this.optionIds = optionIds;
    }

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public int getWebId() {
        return webId;
    }

    public void setWebId(int webId) {
        this.webId = webId;
    }

    public long getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(long updated_at) {
        this.updated_at = updated_at;
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

    public void setResponseId(int responseId) {
        this.responseId = responseId;
    }

    public int getQuestion_id() {
        return question_id;
    }

    public void setQuestion_id(int question_id) {
        this.question_id = question_id;
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
