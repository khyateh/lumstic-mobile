package com.lumstic.ashoka.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class Preferences {
    private String accessToken = "access_token";
    private String userId = "user_id";
    private String organizationId = "organization_id";
    private String baseUrl = "base_url";
    private String userBaseUrl = "user_base_url";
    private String latitude = "latitude";
    private String longitude = "longitude";
    private String username = "username";
    private String password = "password";
    private String accessTokenCreatedAt = "access_token_created_at";
    private String surveyData = "SURVEY_DATA";
    private Context context;
    private long lastSucessfulUpload;

    public long getLastSucessfulUpload() {
        return lastSucessfulUpload;
    }

    public void setLastSucessfulUpload(long lastSucessfulUpload) {
        this.lastSucessfulUpload = lastSucessfulUpload;
    }

    public Preferences(Context mContext) {
        super();
        this.context = mContext;
    }

    protected SharedPreferences getSharedPreferences(String key) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    private String getString(String key, String def) {
        SharedPreferences prefs = getSharedPreferences(key);
        return prefs.getString(key, def);
    }

    private void setString(String key, String val) {
        SharedPreferences prefs = getSharedPreferences(key);
        Editor e = prefs.edit();
        e.putString(key, val);
        e.commit();
    }

    private boolean getBoolean(String key, boolean def) {
        SharedPreferences prefs = getSharedPreferences(key);
        return prefs.getBoolean(key, def);
    }

    private void setBoolean(String key, boolean val) {
        SharedPreferences prefs = getSharedPreferences(key);
        Editor e = prefs.edit();
        e.putBoolean(key, val);
        e.commit();
    }


    public String getBaseUrl() {
        return getString(baseUrl, null);
    }

    public void setBaseUrl(String baseUrl) {
        setString(this.baseUrl, baseUrl);
    }

    public Boolean getBackPressed() {
        return getBoolean("back_pressed", false);
    }

    public void setBackPressed(Boolean backPressed) {
        setBoolean("back_pressed", backPressed);
    }

    public String getAccessToken() {
        return getString(accessToken, null);
    }

    public void setAccessToken(String authToken) {
        setString(accessToken, authToken);
    }

    public String getUserId() {
        return getString(userId, null);
    }

    public void setUserId(String userId) {
        setString(this.userId, userId);
    }

    public String getOrganizationId() {
        return getString(organizationId, null);
    }


    public void setOrganizationId(String organizationId) {
        setString(this.organizationId, organizationId);
    }


    public String getSurveyData() {
        return getString(surveyData, null);
    }

    public void setSurveyData(String surveyData) {
        setString(this.surveyData, surveyData);
    }


    public String getLatitude() {
        return getString(latitude, null);
    }

    public void setLatitude(String lat) {
        setString(latitude, lat);
    }

    public String getLongitude() {
        return getString(longitude, null);
    }

    public void setLongitude(String lng) {
        setString(longitude, lng);
    }

    public String getUsername() {
        return getString(username, null);
    }

    public void setUsername(String localUsername) {
        setString(username, localUsername);
    }

    public String getPassword() {
        return getString(password, null);
    }

    public void setPassword(String localPassword) {
        setString(password, localPassword);
    }

    public String getAccessTokenCreatedAt() {
        return getString(accessTokenCreatedAt, null);
    }

    public void setAccessTokenCreatedAt(String localAccessTokenCreatedAt) {
        setString(accessTokenCreatedAt, localAccessTokenCreatedAt);
    }

    public String getUserBaseUrl() {
        return getString(userBaseUrl, null);
    }

    public void setUserBaseUrl(String baseUrl) {
        setString(userBaseUrl, baseUrl);
    }
}