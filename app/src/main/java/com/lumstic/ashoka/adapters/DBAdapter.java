package com.lumstic.ashoka.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.lumstic.ashoka.models.Answers;
import com.lumstic.ashoka.models.Categories;
import com.lumstic.ashoka.models.Choices;
import com.lumstic.ashoka.models.Options;
import com.lumstic.ashoka.models.Questions;
import com.lumstic.ashoka.models.Records;
import com.lumstic.ashoka.models.Responses;
import com.lumstic.ashoka.models.Surveys;
import com.lumstic.ashoka.utils.CommonUtil;

import java.util.ArrayList;
import java.util.List;

public class DBAdapter {
    DBhelper dBhelper;
    SQLiteDatabase sqLiteDb;
    Context context;

    public DBAdapter(Context context) throws SQLException {
        if (dBhelper == null) {
            dBhelper = new DBhelper(context);
        }
        sqLiteDb = getSQLiteDB();
        this.context = context;
    }

    public long getMaxID() {
        long id = 0;
        String[] coloums = {DBhelper.ID};
        Cursor cursor = getSQLiteDB().query(DBhelper.TABLE_RESPONSES, coloums, null, null, null, null, null);
        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex(DBhelper.ID);
            id = cursor.getInt(index);

        }
        cursor.close();
        return id;
    }




    public String getAnswer(int responseId, int questionId, int recordId) {
        String answer = "";
        String[] coloumns = {DBhelper.CONTENT};
        String[] selectionArgs = {String.valueOf(responseId), String.valueOf(questionId), String.valueOf(recordId)};
        Cursor cursor = getSQLiteDB().query(DBhelper.TABLE_ANSWERS, coloumns, DBhelper.RESPONSE_ID + " =? " +
                "AND " +
                "" + DBhelper.QUESTION_ID + " =? AND " + DBhelper.RECORD_ID + " =?", selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            answer = cursor.getString(cursor.getColumnIndex(DBhelper.CONTENT));

        }
        cursor.close();
        return answer;
    }

    public String getImage(int responseId, int questionId, int recordId) {
        String answer = "";
        String[] coloums = {DBhelper.IMAGE};
        String[] selectionArgs = {String.valueOf(responseId), String.valueOf(questionId), String.valueOf(recordId)};
        Cursor cursor = getSQLiteDB().query(DBhelper.TABLE_ANSWERS, coloums, DBhelper.RESPONSE_ID + " =? " +
                "AND " + DBhelper.QUESTION_ID + " =? AND " + DBhelper.RECORD_ID + " =?", selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex(DBhelper.IMAGE);
            answer = cursor.getString(index);

        }
        cursor.close();
        return answer;
    }

    public List<Integer> getIdFromAnswerTable(int responseId, int questionId, int recordId) {
        int id;
        List<Integer> integers = new ArrayList<>();
        String[] coloums = {DBhelper.ID};
        String[] selectionArgs = {String.valueOf(responseId), String.valueOf(questionId), String.valueOf(recordId)};
        Cursor cursor = getSQLiteDB().query(DBhelper.TABLE_ANSWERS, coloums, DBhelper.RESPONSE_ID + " =? " +
                "AND " + DBhelper.QUESTION_ID + " =? AND " + DBhelper.RECORD_ID + " =?", selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex(DBhelper.ID);
            id = cursor.getInt(index);
            integers.add(id);


        }
        cursor.close();
        return integers;

    }


    public List<Integer> getIdFromChoicesTable(List<Integer> answerIds) {

        List<Integer> answer = new ArrayList<>();
        String[] coloums = {DBhelper.ID};
        for (int i = 0; i < answerIds.size(); i++) {
            String[] selectionArgs = {String.valueOf(answerIds.get(i))};
            Cursor cursor = getSQLiteDB().query(DBhelper.TABLE_CHOICES, coloums, DBhelper.ANSWER_ID + "=? ",
                    selectionArgs, null, null, null);

            while (cursor.moveToNext()) {
                int index = cursor.getColumnIndex(DBhelper.ID);
                answer.add(cursor.getInt(index));

            }
            cursor.close();
        }

        return answer;
    }

    public List<Integer> getOptionIds(List<Integer> ids) {
        List<Integer> integers = new ArrayList<>();
        int id;
        String[] coloums = {DBhelper.OPTION_ID};
        for (int i = 0; i < ids.size(); i++) {
            String[] selectionArgs = {String.valueOf(ids.get(i))};
            Cursor cursor = getSQLiteDB().query(DBhelper.TABLE_CHOICES, coloums, DBhelper.ANSWER_ID + " =? ",
                    selectionArgs, null, null, null);
            while (cursor.moveToNext()) {
                id = cursor.getInt(cursor.getColumnIndex(DBhelper.OPTION_ID));
                integers.add(id);

            }
            cursor.close();
        }
        return integers;

    }


    public int getOptionIdFromPrimaryId(int id) {
        int value = 0;
        String[] coloums = {DBhelper.OPTION_ID};
        String[] selectionArgs = {String.valueOf(id)};
        Cursor cursor = getSQLiteDB().query(DBhelper.TABLE_CHOICES, coloums, DBhelper.ID + " =?",
                selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            value = cursor.getInt(cursor.getColumnIndex(DBhelper.OPTION_ID));
        }
        cursor.close();

        return value;
    }


    public int deleteFromChoicesTableWhereAnswerId(int localAnswerID) {
        String[] selectionArgs = {String.valueOf(localAnswerID)};
        return getSQLiteDB().delete(DBhelper.TABLE_CHOICES, DBhelper.ANSWER_ID + " =? ", selectionArgs);
    }

    public void deleteFromChoicesTableWhereAnswerId(int localAnswerID, int optionId) {
        String[] selectionArgs = {String.valueOf(localAnswerID), String.valueOf(optionId)};
        getSQLiteDB().delete(DBhelper.TABLE_CHOICES, DBhelper.ANSWER_ID + " =? AND " +
                DBhelper.OPTION_ID + " =? ", selectionArgs);
    }

    public int deleteImagePath(int responseId, int questionId, int recordId) {
        String[] selectionArgs = {String.valueOf(responseId), String.valueOf(questionId), String.valueOf(recordId)};
        return getSQLiteDB().delete(DBhelper.TABLE_ANSWERS, DBhelper.RESPONSE_ID + " =? AND " + DBhelper
                .QUESTION_ID + " =? AND " + DBhelper.RECORD_ID + " =?", selectionArgs);
    }


    public int updateCompleteResponse(int responseId, int surveyId) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBhelper.STATUS, CommonUtil.SURVEY_STATUS_COMPLETE);
        String[] args = {String.valueOf(responseId), String.valueOf(surveyId)};
        return getSQLiteDB().update(DBhelper.TABLE_RESPONSES, contentValues, DBhelper.ID + " =? AND " +
                DBhelper.SURVEY_ID + " =?", args);
    }


    public int getIncompleteResponse(int surveyId) {
        int value = 0;
        String[] coloumns = {DBhelper.STATUS};
        String[] selectionArgs = {String.valueOf(surveyId)};
        Cursor cursor = getSQLiteDB().query(DBhelper.TABLE_RESPONSES, coloumns, DBhelper.SURVEY_ID + " =?",
                selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex(DBhelper.STATUS);
            if (CommonUtil.SURVEY_STATUS_INCOMPLETE.equals(cursor.getString(index))) {
                value++;
            }

        }
        cursor.close();
        return value;
    }

    public int getCompleteResponse(int surveyId) {
        int value = 0;
        String[] coloums = {DBhelper.STATUS};
        String[] selectionArgs = {String.valueOf(surveyId)};
        Cursor cursor = getSQLiteDB().query(DBhelper.TABLE_RESPONSES, coloums, DBhelper.SURVEY_ID + " =?",
                selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex(DBhelper.STATUS);
            if (CommonUtil.SURVEY_STATUS_COMPLETE.equals(cursor.getString(index))) {
                value++;
            }

        }
        cursor.close();
        return value;
    }

    public int getCompleteResponseFull() {
        int value = 0;
        String[] coloums = {DBhelper.STATUS};
        Cursor cursor = getSQLiteDB().query(DBhelper.TABLE_RESPONSES, coloums, null, null, null, null, null);

        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex(DBhelper.STATUS);
            if (CommonUtil.SURVEY_STATUS_COMPLETE.equals(cursor.getString(index))) {
                value++;
            }

        }
        cursor.close();
        return value;
    }

    public String getMobileIDFromResponseIDAndSurveyID(int localResponseID, int localSurveyID) {
        String latitude = null;
        String[] coloums = {DBhelper.ID, DBhelper.MOBILE_ID, DBhelper.SURVEY_ID};
        String[] selectionArgs = {String.valueOf(localResponseID), String.valueOf(localSurveyID)};
        Cursor cursor = getSQLiteDB().query(DBhelper.TABLE_RESPONSES, coloums, DBhelper.ID + " =? AND " +
                DBhelper.SURVEY_ID + " =?", selectionArgs, null, null, null);

        if (cursor != null & cursor.getCount() > 0) {
            cursor.moveToFirst();
            latitude = cursor.getString(cursor.getColumnIndex(DBhelper.MOBILE_ID));
        }
        cursor.close();
        return latitude;
    }

    public String getLatitudeFromResponseIDAndSurveyID(int localResponseID, int localSurveyID) {
        String latitude = null;
        String[] coloums = {DBhelper.ID, DBhelper.LATITUDE, DBhelper.SURVEY_ID};
        String[] selectionArgs = {String.valueOf(localResponseID), String.valueOf(localSurveyID)};
        Cursor cursor = getSQLiteDB().query(DBhelper.TABLE_RESPONSES, coloums, DBhelper.ID + " =? AND " +
                DBhelper.SURVEY_ID + " =?", selectionArgs, null, null, null);

        if (cursor != null & cursor.getCount() > 0) {
            cursor.moveToFirst();
            latitude = cursor.getString(cursor.getColumnIndex(DBhelper.LATITUDE));
        }
        cursor.close();
        return latitude;
    }

    public String getLongitudeFromResponseIDAndSurveyID(int localResponseID, int localSurveyID) {
        String longitude = null;
        String[] coloums = {DBhelper.ID, DBhelper.LONGITUDE, DBhelper.SURVEY_ID};
        String[] selectionArgs = {String.valueOf(localResponseID), String.valueOf(localSurveyID)};
        Cursor cursor = getSQLiteDB().query(DBhelper.TABLE_RESPONSES, coloums, DBhelper.ID + " =? AND " +
                DBhelper.SURVEY_ID + " =?", selectionArgs, null, null, null);

        if (cursor != null & cursor.getCount() > 0) {
            cursor.moveToFirst();
            longitude = cursor.getString(cursor.getColumnIndex(DBhelper.LONGITUDE));
        }
        cursor.close();
        return longitude;
    }

    public List<Integer> getIncompleteResponsesIds(int surveyId) {
        List<Integer> ids;
        ids = new ArrayList<>();
        String[] coloums = {DBhelper.STATUS, DBhelper.ID};
        String[] selectionArgs = {String.valueOf(surveyId)};
        Cursor cursor = getSQLiteDB().query(DBhelper.TABLE_RESPONSES, coloums, DBhelper.SURVEY_ID + " =?",
                selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex(DBhelper.STATUS);
            if (CommonUtil.SURVEY_STATUS_INCOMPLETE.equals(cursor.getString(index))) {
                ids.add(cursor.getInt(cursor.getColumnIndex(DBhelper.ID)));
            }

        }
        cursor.close();
        return ids;
    }

    public List<Integer> getCompleteResponsesIds(int surveyId) {
        List<Integer> ids;
        ids = new ArrayList<>();
        String[] coloums = {DBhelper.STATUS, DBhelper.ID};
        String[] selectionArgs = {String.valueOf(surveyId)};
        Cursor cursor = getSQLiteDB().query(DBhelper.TABLE_RESPONSES, coloums, DBhelper.SURVEY_ID + " =?",
                selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex(DBhelper.STATUS);
            if (CommonUtil.SURVEY_STATUS_COMPLETE.equals(cursor.getString(index))) {
                ids.add(cursor.getInt(cursor.getColumnIndex(DBhelper.ID)));
            }

        }
        cursor.close();
        return ids;
    }

    public List<Answers> getAnswerByResponseId(int responseId) {
        List<Answers> answersList;
        answersList = new ArrayList<>();

        String[] coloums = {DBhelper.ID, DBhelper.QUESTION_ID, DBhelper.CONTENT, DBhelper.IMAGE, DBhelper.UPDATED_AT, DBhelper.TYPE, DBhelper.RECORD_ID};
        String[] selectionArgs = {String.valueOf(responseId)};
        Cursor cursor = getSQLiteDB().query(DBhelper.TABLE_ANSWERS, coloums, DBhelper.RESPONSE_ID + " =?",
                selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            Answers answers = new Answers();
            answers.setQuestionId(cursor.getInt(cursor.getColumnIndex(DBhelper.QUESTION_ID)));
            answers.setContent(cursor.getString(cursor.getColumnIndex(DBhelper.CONTENT)));
            answers.setImage(cursor.getString(cursor.getColumnIndex(DBhelper.IMAGE)));
            answers.setRecordId(cursor.getInt(cursor.getColumnIndex(DBhelper.RECORD_ID)));
            answers.setType(cursor.getString(cursor.getColumnIndex(DBhelper.TYPE)));
            answers.setUpdatedAt(cursor.getLong(cursor.getColumnIndex(DBhelper.UPDATED_AT)));
            answers.setId(cursor.getInt(cursor.getColumnIndex(DBhelper.ID)));
            answersList.add(answers);
        }

        cursor.close();
        return answersList;
    }


    public int deleteFromResponseTableOnUpload(int surveyId, String localResponseID) {
        String[] selectionArgs = {String.valueOf(surveyId), CommonUtil.SURVEY_STATUS_COMPLETE, localResponseID};
        return getSQLiteDB().delete(DBhelper.TABLE_RESPONSES, DBhelper.SURVEY_ID + " =? AND " + DBhelper
                .STATUS + " =? AND " + DBhelper.ID + " =?", selectionArgs);
    }

    public int deleteFromResponseTable(int surveyId, String localResponseID) {
        String[] selectionArgs = {String.valueOf(surveyId), localResponseID};
        return getSQLiteDB().delete(DBhelper.TABLE_RESPONSES, DBhelper.SURVEY_ID + " =? AND " + DBhelper.ID +
                " =?", selectionArgs);
    }

    public int getChoicesCountWhereAnswerIdIs(int answerId) {
        int count = 0;
        String[] coloums = {DBhelper.OPTION_ID};
        String[] selectionArgs = {String.valueOf(answerId)};
        Cursor cursor = getSQLiteDB().query(DBhelper.TABLE_CHOICES, coloums, DBhelper.ANSWER_ID + " =? ",
                selectionArgs, null, null, null);
        while (cursor.moveToNext()) {
            count++;
        }
        cursor.close();
        return count;
    }

    public int getDropDownChoicesCountWhereAnswerIdIs(int answerId) {
        int optionID = 0;
        String[] coloums = {DBhelper.OPTION_ID};
        String[] selectionArgs = {String.valueOf(answerId)};
        Cursor cursor = getSQLiteDB().query(DBhelper.TABLE_CHOICES, coloums, DBhelper.ANSWER_ID + " =? ",
                selectionArgs, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();

            optionID = cursor.getInt(cursor.getColumnIndex(DBhelper.OPTION_ID));
        }
        cursor.close();
        return optionID;
    }

    public String getQuestionTypeWhereAnswerIdIs(int answerId) {


        String type = "";
        String[] coloums = {DBhelper.TYPE};
        String[] selectionArgs = {String.valueOf(answerId)};
        Cursor cursor = getSQLiteDB().query(DBhelper.TABLE_CHOICES, coloums, DBhelper.ANSWER_ID + " =?",
                selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex(DBhelper.TYPE);
            type = cursor.getString(index);

        }
        cursor.close();
        return type;
    }

    public String getChoicesWhereAnswerCountIsOne(int answerId) {
        String content = null;
        String[] coloums = {DBhelper.OPTION};
        String[] selectionArgs = {String.valueOf(answerId)};
        Cursor cursor = getSQLiteDB().query(DBhelper.TABLE_CHOICES, coloums, DBhelper.ANSWER_ID + " =?",
                selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            content = cursor.getString(cursor.getColumnIndex(DBhelper.OPTION));
        }
        cursor.close();
        return content;
    }

    public int getAnswerId(int responseId, int questionId, int recordId) {
        int ansID = 0;
        String[] coloums = {DBhelper.ID};
        String[] selectionArgs = {String.valueOf(responseId), String.valueOf(questionId), String.valueOf(recordId)};
        Cursor cursor = getSQLiteDB().query(DBhelper.TABLE_ANSWERS, coloums, DBhelper.RESPONSE_ID + " =? " +
                "AND " + DBhelper.QUESTION_ID + " =? AND " + DBhelper.RECORD_ID + " =?", selectionArgs, null, null, null);
        while (cursor.moveToNext()) {
            ansID = cursor.getInt(cursor.getColumnIndex(DBhelper.ID));
        }
        cursor.close();
        return ansID;
    }


    public List<Integer> getChoicesWhereAnswerCountIsMoreThanOne(int answerId) {
        List<Integer> optionList;
        optionList = new ArrayList<>();
        String[] coloums = {DBhelper.OPTION_ID};
        String[] selectionArgs = {String.valueOf(answerId)};
        Cursor cursor = getSQLiteDB().query(DBhelper.TABLE_CHOICES, coloums, DBhelper.ANSWER_ID + " =?",
                selectionArgs, null, null, null);
        while (cursor.moveToNext()) {
            optionList.add(cursor.getInt(cursor.getColumnIndex(DBhelper.OPTION_ID)));
        }
        cursor.close();
        return optionList;
    }

    public int getChoicesCount(int answerId) {
        int count = 0;
        String[] coloums = {DBhelper.OPTION_ID};
        String[] selectionArgs = {String.valueOf(answerId)};
        Cursor cursor = getSQLiteDB().query(DBhelper.TABLE_CHOICES, coloums, DBhelper.ANSWER_ID + " =?",
                selectionArgs, null, null, null);
        while (cursor.moveToNext()) {
            count++;

        }
        cursor.close();
        return count;
    }


    public boolean doesAnswerExist(int id, int responseid) {
        int count = 0;
        String[] coloums = {DBhelper.ID};
        String[] selectionArgs = {String.valueOf(id), String.valueOf(responseid)};
        Cursor cursor = getSQLiteDB().query(DBhelper.TABLE_ANSWERS, coloums, DBhelper.QUESTION_ID + " =? " +
                "AND " + DBhelper.RESPONSE_ID + " =?", selectionArgs, null, null, null);
        while (cursor.moveToNext()) {
            count++;
        }
        cursor.close();
        return count > 0;
    }


    public boolean doesAnswerExist(int qId, int responseid, int recordId) {
        int count = 0;
        String[] coloums = {DBhelper.ID};
        String[] selectionArgs = {String.valueOf(qId), String.valueOf(responseid), String.valueOf(recordId)};
        Cursor cursor = getSQLiteDB().query(DBhelper.TABLE_ANSWERS, coloums, DBhelper.QUESTION_ID + " =? " +
                "AND " + DBhelper.RESPONSE_ID + " =? AND " + DBhelper.RECORD_ID + " =?", selectionArgs, null, null, null);
        if (cursor != null) {
            count = cursor.getCount();
            cursor.close();
        }
        return count > 0;
    }

    public int deleteRatingAnswer(int id, int responseId, int recordId) {
        String[] selectionArgs = {String.valueOf(id), String.valueOf(responseId), String.valueOf(recordId)};
        return getSQLiteDB().delete(DBhelper.TABLE_ANSWERS, DBhelper.QUESTION_ID + " =? AND " + DBhelper
                .RESPONSE_ID + " =? AND " + DBhelper.RECORD_ID + " =?", selectionArgs);
    }

    public String doesAnswerExistAsNonNull(int id, int responseid, int recordId) {
        String str = "";
        String[] coloums = {DBhelper.CONTENT};
        String[] selectionArgs = {String.valueOf(id), String.valueOf(responseid), String.valueOf(recordId)};
        Cursor cursor = getSQLiteDB().query(DBhelper.TABLE_ANSWERS, coloums, DBhelper.QUESTION_ID + " =? " +
                "AND " + DBhelper.RESPONSE_ID + " =? AND " + DBhelper.RECORD_ID + " =?", selectionArgs, null, null, null);
        while (cursor.moveToNext()) {
            str = cursor.getString(cursor.getColumnIndex(DBhelper.CONTENT));
        }
        cursor.close();
        return str;
    }

    public String doesImageExistAsNonNull(int id, int responseid, int recordId) {
        String str = "";
        String[] coloums = {DBhelper.IMAGE};
        String[] selectionArgs = {String.valueOf(id), String.valueOf(responseid), String.valueOf(recordId)};
        Cursor cursor = getSQLiteDB().query(DBhelper.TABLE_ANSWERS, coloums, DBhelper.QUESTION_ID + " =? " +
                "AND " + DBhelper.RESPONSE_ID + " =? AND " + DBhelper.RECORD_ID + " =?", selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            str = cursor.getString(cursor.getColumnIndex(DBhelper.IMAGE));
        }
        cursor.close();
        return str;

    }

    public int deleteFromRecordsTableWhereRecordId(int recordId, int responseId) {
        String[] selectionArgs = {String.valueOf(recordId), String.valueOf(responseId)};
        return getSQLiteDB().delete(DBhelper.TABLE_RECORDS, DBhelper.ID + " =? AND " + DBhelper
                .RESPONSE_ID + " =?", selectionArgs);
    }


    public int deleteFromAnswerTableWithResponseID(String responseId) {

        String[] selectionArgs = {responseId};
        return getSQLiteDB().delete(DBhelper.TABLE_ANSWERS, DBhelper.RESPONSE_ID + " =?", selectionArgs);
    }

    public int deleteFromAnswerTable(int questionId, int responseId, int recordId) {

        String[] selectionArgs = {String.valueOf(questionId), String.valueOf(responseId), String.valueOf(recordId)};
        return getSQLiteDB().delete(DBhelper.TABLE_ANSWERS, DBhelper.QUESTION_ID + " =? AND " + DBhelper
                .RESPONSE_ID + " =? AND " + DBhelper.RECORD_ID + " =?", selectionArgs);
    }


    public int deleteFromAnswerTableWithRecordId(int questionId, int responseId, int recordId) {

        String[] selectionArgs = {String.valueOf(questionId), String.valueOf(responseId), String.valueOf(recordId)};
        return getSQLiteDB().delete(DBhelper.TABLE_ANSWERS, DBhelper.QUESTION_ID + " =? AND " + DBhelper
                .RESPONSE_ID + " =? AND " + DBhelper.RECORD_ID + " =?", selectionArgs);
    }


    public Cursor findNoOfEntriesFromRecordTable(int categoryId, int responseId) {

        String[] coloumns = {DBhelper.ID};
        String[] selectionArgs = {String.valueOf(categoryId), String.valueOf(responseId)};
        return getSQLiteDB().query(DBhelper.TABLE_RECORDS, coloumns, DBhelper.CATEGORY_ID + " =? AND "
                + DBhelper.RESPONSE_ID + " =?", selectionArgs, null, null, null);
    }

    public long insertDataQuestionTable(Questions questions) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBhelper.IDENTIFIER, questions.getIdentifier());
        contentValues.put(DBhelper.PARENT_ID, questions.getParentId());
        contentValues.put(DBhelper.MIN_VALUE, questions.getMinValue());
        contentValues.put(DBhelper.MAX_VALUE, questions.getMaxValue());
        contentValues.put(DBhelper.TYPE, questions.getType());
        contentValues.put(DBhelper.ID, questions.getId());
        contentValues.put(DBhelper.CONTENT, questions.getContent());
        contentValues.put(DBhelper.SURVEY_ID, questions.getSurveyId());
        contentValues.put(DBhelper.MAX_LENGTH, questions.getMaxLength());
        contentValues.put(DBhelper.MANDATORY, questions.getMandatory());
        contentValues.put(DBhelper.IMAGE_URL, questions.getImageUrl());
        contentValues.put(DBhelper.ORDER_NUMBER, questions.getOrderNumber());
        contentValues.put(DBhelper.CATEGORY_ID, questions.getCategoryId());
        return getSQLiteDB().insert(DBhelper.TABLE_QUESTIONS, null, contentValues);
    }

    public void insertDataChoicesTable(Choices choices) {
        if (!isChoiceExists(choices.getOptionId(), choices.getAnswerId())) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBhelper.OPTION_ID, choices.getOptionId());
            contentValues.put(DBhelper.ANSWER_ID, choices.getAnswerId());
            contentValues.put(DBhelper.OPTION, choices.getOption());
            contentValues.put(DBhelper.TYPE, choices.getType());
            getSQLiteDB().insert(DBhelper.TABLE_CHOICES, null, contentValues);
        }
    }

    public boolean isChoiceExists(int localOptionId, int localAnsId) {
        int count = 0;
        String[] coloums = {DBhelper.ID};
        String[] selectionArgs = {String.valueOf(localOptionId), String.valueOf(localAnsId)};
        Cursor cursor = getSQLiteDB().query(DBhelper.TABLE_CHOICES, coloums, DBhelper.OPTION_ID
                        + " =? " +
                        " AND " + DBhelper.ANSWER_ID + " =? ", selectionArgs,
                null, null, null);
        if (cursor != null) {
            count = cursor.getCount();
            cursor.close();
        }
        return count > 0;
    }

    public long insertDataOptionsTable(Options options) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBhelper.ID, options.getId());
        contentValues.put(DBhelper.QUESTION_ID, options.getQuestionId());
        contentValues.put(DBhelper.ORDER_NUMBER, options.getOrderNumber());
        contentValues.put(DBhelper.CONTENT, options.getContent());
        return getSQLiteDB().insert(DBhelper.TABLE_OPTIONS, null, contentValues);
    }

    public long insertDataSurveysTable(Surveys surveys) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBhelper.ID, surveys.getId());
        contentValues.put(DBhelper.DESCRIPTION, surveys.getDescription());
        contentValues.put(DBhelper.PUBLISHED_ON, surveys.getPublishedOn());
        contentValues.put(DBhelper.EXPIRY_DATE, surveys.getExpiryDate());
        contentValues.put(DBhelper.NAME, surveys.getName());
        return getSQLiteDB().insert(DBhelper.TABLE_SURVEYS, null, contentValues);
    }

    public long insertDataCategoriesTable(Categories categories) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBhelper.ID, categories.getId());
        contentValues.put(DBhelper.CATEGORY_ID, categories.getCategoryId());
        contentValues.put(DBhelper.ORDER_NUMBER, categories.getOrderNumber());
        contentValues.put(DBhelper.PARENT_ID, categories.getParentId());
        contentValues.put(DBhelper.SURVEY_ID, categories.getSurveyId());
        contentValues.put(DBhelper.CONTENT, categories.getContent());
        contentValues.put(DBhelper.TYPE, categories.getType());
        return getSQLiteDB().insert(DBhelper.TABLE_CATEGORIES, null, contentValues);
    }

    public long insertDataAnswersTable(Answers answers) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBhelper.RECORD_ID, answers.getRecordId());
        contentValues.put(DBhelper.IMAGE, answers.getImage());
        contentValues.put(DBhelper.UPDATED_AT, answers.getUpdatedAt());
        contentValues.put(DBhelper.CONTENT, answers.getContent());
        contentValues.put(DBhelper.RESPONSE_ID, answers.getResponseId());
        contentValues.put(DBhelper.QUESTION_ID, answers.getQuestionId());
        contentValues.put(DBhelper.TYPE, answers.getType());

        return getSQLiteDB().insert(DBhelper.TABLE_ANSWERS, null, contentValues);
    }

    public long insertDataRecordsTable(Records records) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBhelper.ID, getMaxRecordId() + 20);
        contentValues.put(DBhelper.CATEGORY_ID, records.getCategoryId());
        contentValues.put(DBhelper.RESPONSE_ID, records.getResponseId());
        return getSQLiteDB().insert(DBhelper.TABLE_RECORDS, null, contentValues);
    }

    public long getMaxRecordId() {
        long id = 0;
        String[] coloums = {DBhelper.ID};
        Cursor cursor = getSQLiteDB().query(DBhelper.TABLE_RECORDS, coloums, null, null, null, null,
                null);
        while (cursor.moveToNext()) {
            id = cursor.getInt(cursor.getColumnIndex(DBhelper.ID));
        }
        cursor.close();
        return id;
    }

    public Cursor getRecordIdsByResponseId(int responseId) {
        String[] coloums = {DBhelper.ID, DBhelper.WEB_ID, DBhelper.CATEGORY_ID};
        String[] selectionArgs = {String.valueOf(responseId)};
        return getSQLiteDB().query(DBhelper.TABLE_RECORDS, coloums, DBhelper.RESPONSE_ID + " " +
                "=?", selectionArgs, null, null, null);
    }

    public int updateRecordsTable(int recordId, int responseId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBhelper.WEB_ID, recordId);
        String[] args = {String.valueOf(recordId), String.valueOf(responseId)};
        return getSQLiteDB().update(DBhelper.TABLE_RECORDS, contentValues, DBhelper.ID + " =? " +
                "AND " + DBhelper.RESPONSE_ID + " =?", args);
    }

    public int updateAnswerTable(int responseId, int recordId, int oldRecordId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBhelper.RECORD_ID, recordId);
        String[] args = {String.valueOf(responseId), String.valueOf(oldRecordId)};
        return getSQLiteDB().update(DBhelper.TABLE_ANSWERS, contentValues, DBhelper.RESPONSE_ID + " =? " +
                "AND " + DBhelper.RECORD_ID + " =?", args);
    }

    public long insertDataResponsesTable(Responses responses) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBhelper.STATUS, responses.getStatus());
        contentValues.put(DBhelper.ORGANISATION_ID, responses.getOrganisationId());
        contentValues.put(DBhelper.WEB_ID, responses.getWebId());
        contentValues.put(DBhelper.MOBILE_ID, responses.getMobileId());
        contentValues.put(DBhelper.USER_ID, responses.getUserId());
        contentValues.put(DBhelper.LONGITUDE, responses.getLongitude());
        contentValues.put(DBhelper.LATITUDE, responses.getLatitude());
        contentValues.put(DBhelper.UPDATED_AT, responses.getUpdatedAt());
        contentValues.put(DBhelper.SURVEY_ID, responses.getSurveyId());
        return getSQLiteDB().insert(DBhelper.TABLE_RESPONSES, null, contentValues);
    }

    public void close() {
        if (sqLiteDb != null) {
            sqLiteDb.close();
        }
    }


    private synchronized SQLiteDatabase getSQLiteDB() throws SQLException {
        return dBhelper.getWritableDatabase();
    }


    public class DBhelper extends SQLiteOpenHelper {
        public static final String DATABASE_NAME = "SurveyAppDatabase";
        public static final String TABLE_ANSWERS = "answers";
        public static final String ID = "id";
        public static final String ANSWER_ID = "answer_id";
        public static final String RECORD_ID = "record_id";
        public static final String WEB_ID = "web_id";
        public static final String CATEGORY_ID = "category_id";
        private static final int DATABASE_VERSION = 1;
        private static final String TABLE_CHOICES = "choices";
        private static final String TABLE_QUESTIONS = "questions";
        private static final String TABLE_OPTIONS = "options";
        private static final String TABLE_SURVEYS = "surveys";
        private static final String TABLE_CATEGORIES = "categories";
        private static final String TABLE_RECORDS = "records";
        private static final String TABLE_RESPONSES = "responses";
        private static final String OPTION = "option";
        private static final String OPTION_ID = "option_id";
        private static final String IDENTIFIER = "identifier";
        private static final String PARENT_ID = "parent_id";
        private static final String MIN_VALUE = "min_value";
        private static final String MAX_VALUE = "max_value";
        private static final String TYPE = "type";
        private static final String CONTENT = "content";
        private static final String SURVEY_ID = "survey_id";
        private static final String MAX_LENGTH = "max_length";
        private static final String IMAGE_URL = "image_url";
        private static final String MANDATORY = "mandatory";
        private static final String ORDER_NUMBER = "order_number";
        private static final String QUESTION_ID = "question_id";
        private static final String PUBLISHED_ON = "published_on";
        private static final String NAME = "name";
        private static final String DESCRIPTION = "description";
        private static final String EXPIRY_DATE = "expiry_date";
        private static final String IMAGE = "image";
        private static final String UPDATED_AT = "updated_at";
        private static final String RESPONSE_ID = "response_id";
        private static final String MOBILE_ID = "mobile_id";
        private static final String USER_ID = "user_id";
        private static final String LONGITUDE = "longitude";
        private static final String LATITUDE = "latitude";
        private static final String STATUS = "status";

        private static final String ORGANISATION_ID = "organisation_id";
        private static final String CREATE_TABLE_CHOICES = "CREATE TABLE "
                + TABLE_CHOICES + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + OPTION_ID + " INTEGER," + OPTION + " VARCHAR(255)," + TYPE + " VARCHAR(255)," +
                ANSWER_ID + " INTEGER " + ")";
        private static final String CREATE_TABLE_ANSWERS = "CREATE TABLE "
                + TABLE_ANSWERS + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + RECORD_ID + " INTEGER," + TYPE + " VARCHAR(255) ," + UPDATED_AT + " INTEGER," + CONTENT + " VARCHAR(255)," + IMAGE + " VARCHAR(255)," + RESPONSE_ID + " INTEGER," + QUESTION_ID + " INTEGER" + ")";
        private static final String CREATE_TABLE_QUESTIONS = "CREATE TABLE "
                + TABLE_QUESTIONS + "(" + ID + " INTEGER PRIMARY KEY,"
                + IDENTIFIER + " INTEGER," + PARENT_ID + " INTEGER," + MIN_VALUE + " INTEGER," + MAX_VALUE + " INTEGER," + TYPE + " VARCHAR(255)," + CONTENT + " VARCHAR(255)," + SURVEY_ID + " INTEGER," + MAX_LENGTH + " INTEGER," + MANDATORY + " INTEGER," + IMAGE_URL + " VARCHAR(255)," + ORDER_NUMBER + " INTEGER," + CATEGORY_ID + " INTEGER" + ")";
        private static final String CREATE_TABLE_OPTIONS = "CREATE TABLE "
                + TABLE_OPTIONS + "(" + ID + " INTEGER PRIMARY KEY,"
                + ORDER_NUMBER + " INTEGER," + CONTENT + " VARCHAR(255)," + QUESTION_ID + " INTEGER" + ")";
        private static final String CREATE_TABLE_SURVEYS = "CREATE TABLE "
                + TABLE_SURVEYS + "(" + ID + " INTEGER PRIMARY KEY,"
                + PUBLISHED_ON + " VARCHAR(255)," + NAME + " VARCHAR(255)," + EXPIRY_DATE + " VARCHAR(255)," + DESCRIPTION + " VARCHAR(255)" + ")";
        private static final String CREATE_TABLE_CATEGORIES = "CREATE TABLE "
                + TABLE_CATEGORIES + "(" + ID + " INTEGER PRIMARY KEY,"
                + CONTENT + " VARCHAR(255)," + TYPE + " INTEGER," + SURVEY_ID + " INTEGER," + PARENT_ID + " INTEGER," + ORDER_NUMBER + " INTEGER," + CATEGORY_ID + " INTEGER" + ")";
        private static final String CREATE_TABLE_RECORDS = "CREATE TABLE "
                + TABLE_RECORDS + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + RESPONSE_ID + " INTEGER," + WEB_ID + "  INTEGER DEFAULT 0," + CATEGORY_ID + " INTEGER" + ")";
        private static final String CREATE_TABLE_RESPONSES = "CREATE TABLE "
                + TABLE_RESPONSES + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MOBILE_ID + " VARCHAR(255)," + USER_ID + " INTEGER," + LONGITUDE + " VARCHAR(255)," + LATITUDE + " VARCHAR(255)," + UPDATED_AT + " INTEGER," + SURVEY_ID + " INTEGER," + WEB_ID + " INTEGER," + STATUS + " VARCHAR(255)," + ORGANISATION_ID + " INTEGER" + ")";

        public DBhelper(Context mContext) {
            super(mContext, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(CREATE_TABLE_CHOICES);
            sqLiteDatabase.execSQL(CREATE_TABLE_QUESTIONS);
            sqLiteDatabase.execSQL(CREATE_TABLE_OPTIONS);
            sqLiteDatabase.execSQL(CREATE_TABLE_SURVEYS);
            sqLiteDatabase.execSQL(CREATE_TABLE_CATEGORIES);
            sqLiteDatabase.execSQL(CREATE_TABLE_RECORDS);
            sqLiteDatabase.execSQL(CREATE_TABLE_RESPONSES);
            sqLiteDatabase.execSQL(CREATE_TABLE_ANSWERS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {


        }

        @Override
        public synchronized void close() {
            if (sqLiteDb != null) {
                sqLiteDb.close();
                super.close();
            }
        }
    }

}
