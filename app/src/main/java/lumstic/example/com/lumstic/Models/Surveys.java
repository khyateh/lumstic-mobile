package lumstic.example.com.lumstic.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.List;

/**
 * Created by work on 17/4/15.
 */
public class Surveys implements Serializable {

    int id;
    String publishedOn;
    String name;
    String description;
    String expiryDate;
    List<Questions> questions;
    List<Categories> categories;
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
    public List<Categories> getCategories() {
        return categories;
    }
    public void setCategories(List<Categories> categories) {
        this.categories = categories;
    }
    public List<Questions> getQuestions() {
        return questions;
    }
    public void setQuestions(List<Questions> questions) {
        this.questions = questions;
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
