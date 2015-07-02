package lumstic.example.com.lumstic.Models;

/**
 * Created by work on 14/4/15.
 */
public class Survey {
    private String surveyName;
    private int completedSurvey,incompleteSurvey,uploadedSurvey;
    private String endDate;

    public Survey(String surveyName, int completedSurvey, int incompleteSurvey, int uploadedSurvey, String endDate) {
        this.surveyName = surveyName;
        this.completedSurvey = completedSurvey;
        this.incompleteSurvey = incompleteSurvey;
        this.uploadedSurvey = uploadedSurvey;
        this.endDate = endDate;
    }

    public String getSurveyName() {
        return surveyName;
    }

    public void setSurveyName(String surveyName) {
        this.surveyName = surveyName;
    }

    public int getCompletedSurvey() {
        return completedSurvey;
    }

    public void setCompletedSurvey(int completedSurvey) {
        this.completedSurvey = completedSurvey;
    }

    public int getIncompleteSurvey() {
        return incompleteSurvey;
    }

    public void setIncompleteSurvey(int incompleteSurvey) {
        this.incompleteSurvey = incompleteSurvey;
    }

    public int getUploadedSurvey() {
        return uploadedSurvey;
    }

    public void setUploadedSurvey(int uploadedSurvey) {
        this.uploadedSurvey = uploadedSurvey;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}
