package lumstic.example.com.lumstic.Models;

/**
 * Created by work on 17/4/15.
 */
public class Responses {
    int userId;
    int updatedAt;
    int id;
    int surveyId;
    int webId;
    int organisationId;
    String mobileId;
    String longitude;
    String latitude;
    String status;

    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public int getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(int updatedAt) {
        this.updatedAt = updatedAt;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getSurveyId() {
        return surveyId;
    }
    public void setSurveyId(int surveyId) {
        this.surveyId = surveyId;
    }
    public int getWebId() {
        return webId;
    }
    public void setWebId(int webId) {
        this.webId = webId;
    }
    public int getOrganisationId() {
        return organisationId;
    }
    public void setOrganisationId(int organisationId) {
        this.organisationId = organisationId;
    }
    public String getMobileId() {
        return mobileId;
    }
    public void setMobileId(String mobileId) {
        this.mobileId = mobileId;
    }
    public String getLongitude() {
        return longitude;
    }
    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
    public String getLatitude() {
        return latitude;
    }
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

}
