package com.lumstic.ashoka.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
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

import java.util.ArrayList;
import java.util.List;

public class DBAdapter {
    DBhelper dBhelper;
    SQLiteDatabase sqLiteDatabase;
    Context context;

    public DBAdapter(Context context) {
        dBhelper = new DBhelper(context);
        sqLiteDatabase = dBhelper.getWritableDatabase();
        this.context = context;
    }

    public long getMaxID() {
        long id = 0;
        String[] coloums = {DBhelper.ID};
        Cursor cursor = sqLiteDatabase.query(DBhelper.TABLE_responses, coloums, null, null, null, null, null);
        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex(DBhelper.ID);
            id = cursor.getInt(index);

        }
        return id;
    }

    public long deleteOption(Options options) {
        String[] selectionArgs = {String.valueOf(options.getId())};
        return sqLiteDatabase.delete(DBhelper.TABLE_choices, DBhelper.OPTION_ID + "=? ",
                selectionArgs);
    }

    public int getAnswerIdByRecordIdandQuestionId(int responseId, int questionId, int recordId) {
        int answerId = 0;
        String[] coloumns = {DBhelper.ID};
        String[] selectionArgs = {String.valueOf(responseId), String.valueOf(questionId), String.valueOf(recordId)};
        Cursor cursor = sqLiteDatabase.query(DBhelper.TABLE_answers, coloumns, DBhelper.RESPONSE_ID + " =? AND " + DBhelper.QUESTION_ID + " =? AND " + DBhelper.RECORD_ID + " =?", selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            answerId = cursor.getInt(cursor.getColumnIndex(DBhelper.ID));

        }
        return answerId;
    }

    public String getAnswer(int responseId, int questionId, int recordId) {
        String answer = "";
        String[] coloumns = {DBhelper.CONTENT};
        String[] selectionArgs = {String.valueOf(responseId), String.valueOf(questionId), String.valueOf(recordId)};
        Cursor cursor = sqLiteDatabase.query(DBhelper.TABLE_answers, coloumns, DBhelper.RESPONSE_ID + " =? AND " + DBhelper.QUESTION_ID + " =? AND " + DBhelper.RECORD_ID + " =?", selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex(DBhelper.CONTENT);
            answer = cursor.getString(index);

        }
        return answer;
    }

    public String getImage(int responseId, int questionId, int recordId) {
        String answer = "";
        String[] coloums = {DBhelper.IMAGE};
        String[] selectionArgs = {String.valueOf(responseId), String.valueOf(questionId), String.valueOf(recordId)};
        Cursor cursor = sqLiteDatabase.query(DBhelper.TABLE_answers, coloums, DBhelper.RESPONSE_ID + " =? AND " + DBhelper.QUESTION_ID + " =? AND " + DBhelper.RECORD_ID + " =?", selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex(DBhelper.IMAGE);
            answer = cursor.getString(index);

        }

        return answer;
    }

    public List<Integer> getIdFromAnswerTable(int responseId, int questionId, int recordId) {
        int id;

        List<Integer> integers = new ArrayList<>();
        String[] coloums = {DBhelper.ID};
        String[] selectionArgs = {String.valueOf(responseId), String.valueOf(questionId), String.valueOf(recordId)};
        Cursor cursor = sqLiteDatabase.query(DBhelper.TABLE_answers, coloums, DBhelper.RESPONSE_ID + " =? AND " + DBhelper.QUESTION_ID + " =? AND " + DBhelper.RECORD_ID + " =?", selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex(DBhelper.ID);
            id = cursor.getInt(index);
            integers.add(id);


        }
        return integers;

    }


    public List<Integer> getIdFromChoicesTable(List<Integer> answerIds) {
        List<Integer> answer = new ArrayList<>();


        String[] coloums = {DBhelper.ID};


        for (int i = 0; i < answerIds.size(); i++) {
            String[] selectionArgs = {String.valueOf(answerIds.get(i))};
            Cursor cursor = sqLiteDatabase.query(DBhelper.TABLE_choices, coloums, DBhelper.ANSWER_ID + "=? ", selectionArgs, null, null, null);

            while (cursor.moveToNext()) {
                int index = cursor.getColumnIndex(DBhelper.ID);
                answer.add(cursor.getInt(index));

            }
        }
        return answer;
    }

    public List<Integer> getOptionIds(List<Integer> ids) {
        List<Integer> integers = new ArrayList<>();

        int id = 0;
        String[] coloums = {DBhelper.OPTION_ID};

        for (int i = 0; i < ids.size(); i++) {


            String[] selectionArgs = {String.valueOf(ids.get(i))};
            Cursor cursor = sqLiteDatabase.query(DBhelper.TABLE_choices, coloums, DBhelper.ANSWER_ID + " =? ", selectionArgs, null, null, null);

            while (cursor.moveToNext()) {
                int index = cursor.getColumnIndex(DBhelper.OPTION_ID);
                id = cursor.getInt(index);
                integers.add(id);

            }
        }


        return integers;

    }


    public int getOptionIdFromPrimaryId(int Id) {
        int value = 0;
        String[] coloums = {DBhelper.OPTION_ID};
        String[] selectionArgs = {String.valueOf(Id)};
        Cursor cursor = sqLiteDatabase.query(DBhelper.TABLE_choices, coloums, DBhelper.ID + " =?", selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex(DBhelper.OPTION_ID);
            value = cursor.getInt(index);

        }

        return value;
    }


    public int deleteFromChoicesTableWhereOptionId(int optionId) {
        String[] selectionArgs = {String.valueOf(optionId)};
        return sqLiteDatabase.delete(DBhelper.TABLE_choices, DBhelper.OPTION_ID + " =? ", selectionArgs);
    }

    public int deleteFromChoicesTableWhereAnswerId(int localAnswerID) {
        String[] selectionArgs = {String.valueOf(localAnswerID)};
        int id1 = sqLiteDatabase.delete(DBhelper.TABLE_choices, DBhelper.ANSWER_ID + " =? ", selectionArgs);
        return id1;
    }

    public int deleteFromChoicesTableWhereAnswerId(int localAnswerID, int optionId) {
        String[] selectionArgs = {String.valueOf(localAnswerID), String.valueOf(optionId)};
        int id1 = sqLiteDatabase.delete(DBhelper.TABLE_choices, DBhelper.ANSWER_ID + " =? AND " +
                DBhelper.OPTION_ID + " =? ", selectionArgs);
        return id1;
    }

    public int deleteImagePath(int responseId, int questionId, int recordId) {
        String[] selectionArgs = {String.valueOf(responseId), String.valueOf(questionId), String.valueOf(recordId)};
        return sqLiteDatabase.delete(DBhelper.TABLE_answers, DBhelper.RESPONSE_ID + " =? AND " + DBhelper.QUESTION_ID + " =? AND " + DBhelper.RECORD_ID + " =?", selectionArgs);
    }


    public int UpdateCompleteResponse(int responseId, int surveyId) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBhelper.STATUS, "complete");
        String[] args = {String.valueOf(responseId), String.valueOf(surveyId)};
        return sqLiteDatabase.update(DBhelper.TABLE_responses, contentValues, DBhelper.ID + " =? AND " + DBhelper.SURVEY_ID + " =?", args);
    }


    public int getIncompleteResponse(int surveyId) {
        int value = 0;
        String[] coloumns = {DBhelper.STATUS};
        String[] selectionArgs = {String.valueOf(surveyId)};
        Cursor cursor = sqLiteDatabase.query(DBhelper.TABLE_responses, coloumns, DBhelper.SURVEY_ID + " =?", selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex(DBhelper.STATUS);
            if (cursor.getString(index).equals("incomplete")) {
                value++;
            }

        }

        return value;
    }

    public int getCompleteResponse(int surveyId) {
        int value = 0;
        String[] coloums = {DBhelper.STATUS};
        String[] selectionArgs = {String.valueOf(surveyId)};
        Cursor cursor = sqLiteDatabase.query(DBhelper.TABLE_responses, coloums, DBhelper.SURVEY_ID + " =?", selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex(DBhelper.STATUS);
            if (cursor.getString(index).equals("complete")) {
                value++;
            }

        }

        return value;
    }

    public int getCompleteResponseFull() {
        int value = 0;
        String[] coloums = {DBhelper.STATUS};
        Cursor cursor = sqLiteDatabase.query(DBhelper.TABLE_responses, coloums, null, null, null, null, null);

        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex(DBhelper.STATUS);
            if (cursor.getString(index).equals("complete")) {
                value++;
            }

        }

        return value;
    }

    public String getMobileIDFromResponseIDAndSurveyID(int localResponseID, int localSurveyID) {
        String latitude = null;
        String[] coloums = {DBhelper.ID, DBhelper.MOBILE_ID, DBhelper.SURVEY_ID};
        String[] selectionArgs = {String.valueOf(localResponseID), String.valueOf(localSurveyID)};
        Cursor cursor = sqLiteDatabase.query(DBhelper.TABLE_responses, coloums, DBhelper.ID + " =? AND " + DBhelper.SURVEY_ID + " =?", selectionArgs, null, null, null);

        if (cursor != null & cursor.getCount() > 0) {
            cursor.moveToFirst();
            latitude = cursor.getString(cursor.getColumnIndex(DBhelper.MOBILE_ID));
        }

        return latitude;
    }

    public String getLatitudeFromResponseIDAndSurveyID(int localResponseID, int localSurveyID) {
        String latitude = null;
        String[] coloums = {DBhelper.ID, DBhelper.LATITUDE, DBhelper.SURVEY_ID};
        String[] selectionArgs = {String.valueOf(localResponseID), String.valueOf(localSurveyID)};
        Cursor cursor = sqLiteDatabase.query(DBhelper.TABLE_responses, coloums, DBhelper.ID + " =? AND " + DBhelper.SURVEY_ID + " =?", selectionArgs, null, null, null);

        if (cursor != null & cursor.getCount() > 0) {
            cursor.moveToFirst();
            latitude = cursor.getString(cursor.getColumnIndex(DBhelper.LATITUDE));
        }

        return latitude;
    }

    public String getLongitudeFromResponseIDAndSurveyID(int localResponseID, int localSurveyID) {
        String longitude = null;
        String[] coloums = {DBhelper.ID, DBhelper.LONGITUDE, DBhelper.SURVEY_ID};
        String[] selectionArgs = {String.valueOf(localResponseID), String.valueOf(localSurveyID)};
        Cursor cursor = sqLiteDatabase.query(DBhelper.TABLE_responses, coloums, DBhelper.ID + " =? AND " + DBhelper.SURVEY_ID + " =?", selectionArgs, null, null, null);

        if (cursor != null & cursor.getCount() > 0) {
            cursor.moveToFirst();
            longitude = cursor.getString(cursor.getColumnIndex(DBhelper.LONGITUDE));
        }

        return longitude;
    }

    public List<Integer> getIncompleteResponsesIds(int surveyId) {
        List<Integer> ids;
        ids = new ArrayList<Integer>();
        String[] coloums = {DBhelper.STATUS, DBhelper.ID};
        String[] selectionArgs = {String.valueOf(surveyId)};
        Cursor cursor = sqLiteDatabase.query(DBhelper.TABLE_responses, coloums, DBhelper.SURVEY_ID + " =?", selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex(DBhelper.STATUS);
            if (cursor.getString(index).equals("incomplete")) {

                int index2 = cursor.getColumnIndex(DBhelper.ID);
                ids.add(cursor.getInt(index2));
            }

        }

        return ids;
    }

    public List<Integer> getCompleteResponsesIds(int surveyId) {
        List<Integer> ids;
        ids = new ArrayList<>();
        String[] coloums = {DBhelper.STATUS, DBhelper.ID};
        String[] selectionArgs = {String.valueOf(surveyId)};
        Cursor cursor = sqLiteDatabase.query(DBhelper.TABLE_responses, coloums, DBhelper.SURVEY_ID + " =?", selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex(DBhelper.STATUS);
            if (cursor.getString(index).equals("complete")) {

                int index2 = cursor.getColumnIndex(DBhelper.ID);
                ids.add(cursor.getInt(index2));
            }

        }

        return ids;
    }

    public List<Answers> getAnswerByResponseId(int responseId) {
        List<Answers> answersList;
        answersList = new ArrayList<Answers>();

        String[] coloums = {DBhelper.ID, DBhelper.QUESTION_ID, DBhelper.CONTENT, DBhelper.IMAGE, DBhelper.UPDATED_AT, DBhelper.TYPE, DBhelper.RECORD_ID};
        String[] selectionArgs = {String.valueOf(responseId)};
        Cursor cursor = sqLiteDatabase.query(DBhelper.TABLE_answers, coloums, DBhelper.RESPONSE_ID + " =?", selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            int index1 = cursor.getColumnIndex(DBhelper.QUESTION_ID);
            int index5 = cursor.getColumnIndex(DBhelper.ID);
            int index2 = cursor.getColumnIndex(DBhelper.CONTENT);
            int index3 = cursor.getColumnIndex(DBhelper.IMAGE);
            int index6 = cursor.getColumnIndex(DBhelper.TYPE);
            int index4 = cursor.getColumnIndex(DBhelper.UPDATED_AT);
            int index7 = cursor.getColumnIndex(DBhelper.RECORD_ID);
            Answers answers = new Answers();
            answers.setQuestion_id(cursor.getInt(index1));
            answers.setContent(cursor.getString(index2));
            answers.setImage(cursor.getString(index3));
            answers.setRecordId(cursor.getInt(index7));
            answers.setType(cursor.getString(index6));
            answers.setUpdated_at(cursor.getLong(index4));
            answers.setId(cursor.getInt(index5));
            answersList.add(answers);
        }


        return answersList;
    }


    public int deleteFromResponseTableOnUpload(int surveyId, String localResponseID) {
        String[] selectionArgs = {String.valueOf(surveyId), "complete", localResponseID};
        return sqLiteDatabase.delete(DBhelper.TABLE_responses, DBhelper.SURVEY_ID + " =? AND " + DBhelper.STATUS + " =? AND " + DBhelper.ID + " =?", selectionArgs);
    }

    public int deleteFromResponseTable(int surveyId, String localResponseID) {
        String[] selectionArgs = {String.valueOf(surveyId), localResponseID};
        return sqLiteDatabase.delete(DBhelper.TABLE_responses, DBhelper.SURVEY_ID + " =? AND " + DBhelper.ID + " =?", selectionArgs);
    }

    public int getChoicesCountWhereAnswerIdIs(int answerId) {
        int count = 0;
        String[] coloums = {DBhelper.OPTION_ID};
        String[] selectionArgs = {String.valueOf(answerId)};
        Cursor cursor = sqLiteDatabase.query(DBhelper.TABLE_choices, coloums, DBhelper.ANSWER_ID + " =? ", selectionArgs, null, null, null);
        while (cursor.moveToNext()) {
            count++;
        }
        return count;
    }

    public int getDropDownChoicesCountWhereAnswerIdIs(int answerId) {
        int optionID = 0;
        String[] coloums = {DBhelper.OPTION_ID};
        String[] selectionArgs = {String.valueOf(answerId)};
        Cursor cursor = sqLiteDatabase.query(DBhelper.TABLE_choices, coloums, DBhelper.ANSWER_ID + " =? ", selectionArgs, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();

            optionID = cursor.getInt(cursor.getColumnIndex(DBhelper.OPTION_ID));
        }
        return optionID;
    }

    public String getQuestionTypeWhereAnswerIdIs(int answerId) {


        String type = "";
        String[] coloums = {DBhelper.TYPE};
        String[] selectionArgs = {String.valueOf(answerId)};
        Cursor cursor = sqLiteDatabase.query(DBhelper.TABLE_choices, coloums, DBhelper.ANSWER_ID + " =?", selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex(DBhelper.TYPE);
            type = cursor.getString(index);

        }
        return type;
    }

    public String getChoicesWhereAnswerCountIsOne(int answerId) {


        String content = null;
        String[] coloums = {DBhelper.OPTION};
        String[] selectionArgs = {String.valueOf(answerId)};
        Cursor cursor = sqLiteDatabase.query(DBhelper.TABLE_choices, coloums, DBhelper.ANSWER_ID + " =?", selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex(DBhelper.OPTION);
            content = cursor.getString(index);


        }


        return content;


    }

    public int getAnswerId(int responseId, int questionId, int recordId) {


        int ansID = 0;
        String[] coloums = {DBhelper.ID};
        String[] selectionArgs = {String.valueOf(responseId), String.valueOf(questionId), String.valueOf(recordId)};
        Cursor cursor = sqLiteDatabase.query(DBhelper.TABLE_answers, coloums, DBhelper.RESPONSE_ID + " =? AND " + DBhelper.QUESTION_ID + " =? AND " + DBhelper.RECORD_ID + " =?", selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex(DBhelper.ID);
            ansID = cursor.getInt(index);


        }


        return ansID;


    }


    public List<Integer> getChoicesWhereAnswerCountIsMoreThanOne(int answerId) {


        List<Integer> optionList;
        optionList = new ArrayList<>();
        String[] coloums = {DBhelper.OPTION_ID};
        String[] selectionArgs = {String.valueOf(answerId)};
        Cursor cursor = sqLiteDatabase.query(DBhelper.TABLE_choices, coloums, DBhelper.ANSWER_ID + " =?", selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex(DBhelper.OPTION_ID);
            optionList.add(cursor.getInt(index));


        }


        return optionList;


    }

    public int getChoicesCount(int answerId) {

        int count = 0;

        String[] coloums = {DBhelper.OPTION_ID};
        String[] selectionArgs = {String.valueOf(answerId)};
        Cursor cursor = sqLiteDatabase.query(DBhelper.TABLE_choices, coloums, DBhelper.ANSWER_ID + " =?", selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            count++;

        }


        return count;


    }


    public boolean doesAnswerExist(int id, int responseid) {
        int count = 0;

        String[] coloums = {DBhelper.ID};
        String[] selectionArgs = {String.valueOf(id), String.valueOf(responseid)};
        Cursor cursor = sqLiteDatabase.query(DBhelper.TABLE_answers, coloums, DBhelper.QUESTION_ID + " =? AND " + DBhelper.RESPONSE_ID + " =?", selectionArgs, null, null, null);
        while (cursor.moveToNext()) {
            count++;
        }
        if (count > 0)
            return true;
        else
            return false;
    }


    public boolean doesAnswerExist(int qId, int responseid, int recordId) {
        int count = 0;

        String[] coloums = {DBhelper.ID};
        String[] selectionArgs = {String.valueOf(qId), String.valueOf(responseid), String.valueOf(recordId)};
        Cursor cursor = sqLiteDatabase.query(DBhelper.TABLE_answers, coloums, DBhelper.QUESTION_ID + " =? AND " + DBhelper.RESPONSE_ID + " =? AND " + DBhelper.RECORD_ID + " =?", selectionArgs, null, null, null);


        if (cursor != null) {
            count = cursor.getCount();
        }
        if (count > 0)
            return true;
        else
            return false;
    }

    public int deleteRatingAnswer(int id, int responseId, int recordId) {
        String[] selectionArgs = {String.valueOf(id), String.valueOf(responseId), String.valueOf(recordId)};
        return sqLiteDatabase.delete(DBhelper.TABLE_answers, DBhelper.QUESTION_ID + " =? AND " + DBhelper.RESPONSE_ID + " =? AND " + DBhelper.RECORD_ID + " =?", selectionArgs);
    }

    public String doesAnswerExistAsNonNull(int id, int responseid, int recordId) {


        String str = "";
        String[] coloums = {DBhelper.CONTENT};
        String[] selectionArgs = {String.valueOf(id), String.valueOf(responseid), String.valueOf(recordId)};
        Cursor cursor = sqLiteDatabase.query(DBhelper.TABLE_answers, coloums, DBhelper.QUESTION_ID + " =? AND " + DBhelper.RESPONSE_ID + " =? AND " + DBhelper.RECORD_ID + " =?", selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex(DBhelper.CONTENT);
            str = cursor.getString(index);
        }

        return str;

    }

    public String doesImageExistAsNonNull(int id, int responseid, int recordId) {


        String str = "";
        String[] coloums = {DBhelper.IMAGE};
        String[] selectionArgs = {String.valueOf(id), String.valueOf(responseid), String.valueOf(recordId)};
        Cursor cursor = sqLiteDatabase.query(DBhelper.TABLE_answers, coloums, DBhelper.QUESTION_ID + " =? AND " + DBhelper.RESPONSE_ID + " =? AND " + DBhelper.RECORD_ID + " =?", selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex(DBhelper.IMAGE);
            str = cursor.getString(index);
        }

        return str;

    }

    public int deleteFromRecordsTableWhereRecordId(int recordId, int responseId) {
        String[] selectionArgs = {String.valueOf(recordId), String.valueOf(responseId)};
        return sqLiteDatabase.delete(DBhelper.TABLE_records, DBhelper.ID + " =? AND " + DBhelper
                .RESPONSE_ID + " =?", selectionArgs);
    }


    public int deleteFromAnswerTableWithResponseID(String responseId) {

        String[] selectionArgs = {responseId};
        return sqLiteDatabase.delete(DBhelper.TABLE_answers, DBhelper.RESPONSE_ID + " =?", selectionArgs);
    }

    public int deleteFromAnswerTable(int questionId, int responseId, int recordId) {

        String[] selectionArgs = {String.valueOf(questionId), String.valueOf(responseId), String.valueOf(recordId)};
        return sqLiteDatabase.delete(DBhelper.TABLE_answers, DBhelper.QUESTION_ID + " =? AND " + DBhelper.RESPONSE_ID + " =? AND " + DBhelper.RECORD_ID + " =?", selectionArgs);
    }


    public int deleteFromAnswerTableWithRecordId(int questionId, int responseId, int recordId) {

        String[] selectionArgs = {String.valueOf(questionId), String.valueOf(responseId), String.valueOf(recordId)};
        return sqLiteDatabase.delete(DBhelper.TABLE_answers, DBhelper.QUESTION_ID + " =? AND " + DBhelper.RESPONSE_ID + " =? AND " + DBhelper.RECORD_ID + " =?", selectionArgs);
    }


    public Cursor findNoOfEntriesFromRecordTable(int categoryId, int responseId) {

        String[] coloumns = {DBhelper.ID};
        String[] selectionArgs = {String.valueOf(categoryId), String.valueOf(responseId)};
        return sqLiteDatabase.query(DBhelper.TABLE_records, coloumns, DBhelper.CATEGORY_ID + " =? AND "
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
        return sqLiteDatabase.insert(DBhelper.TABLE_questions, null, contentValues);
    }

    public void insertDataChoicesTable(Choices choices) {


        if (!isChoiceExists(choices.getOptionId(), choices.getAnswerId())) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBhelper.OPTION_ID, choices.getOptionId());
            contentValues.put(DBhelper.ANSWER_ID, choices.getAnswerId());
            contentValues.put(DBhelper.OPTION, choices.getOption());
            contentValues.put(DBhelper.TYPE, choices.getType());
            sqLiteDatabase.insert(DBhelper.TABLE_choices, null, contentValues);
        }
    }

    public boolean isChoiceExists(int localOptionId, int localAnsId) {
        int count = 0;

        String[] coloums = {DBhelper.ID};
        String[] selectionArgs = {String.valueOf(localOptionId), String.valueOf(localAnsId)};
        Cursor cursor = sqLiteDatabase.query(DBhelper.TABLE_choices, coloums, DBhelper.OPTION_ID
                        + " =? " +
                        " AND " + DBhelper.ANSWER_ID + " =? ", selectionArgs,
                null, null, null);


        if (cursor != null) {
            count = cursor.getCount();
        }
        if (count > 0)
            return true;
        else
            return false;
    }

    public long insertDataOptionsTable(Options options) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBhelper.ID, options.getId());
        contentValues.put(DBhelper.QUESTION_ID, options.getQuestionId());
        contentValues.put(DBhelper.ORDER_NUMBER, options.getOrderNumber());
        contentValues.put(DBhelper.CONTENT, options.getContent());
        return sqLiteDatabase.insert(DBhelper.TABLE_options, null, contentValues);
    }

    public long insertDataSurveysTable(Surveys surveys) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBhelper.ID, surveys.getId());
        contentValues.put(DBhelper.DESCRIPTION, surveys.getDescription());
        contentValues.put(DBhelper.PUBLISHED_ON, surveys.getPublishedOn());
        contentValues.put(DBhelper.EXPIRY_DATE, surveys.getExpiryDate());
        contentValues.put(DBhelper.NAME, surveys.getName());
        return sqLiteDatabase.insert(DBhelper.TABLE_surveys, null, contentValues);
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
        return sqLiteDatabase.insert(DBhelper.TABLE_categories, null, contentValues);
    }

    public long insertDataAnswersTable(Answers answers) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBhelper.RECORD_ID, answers.getRecordId());
        contentValues.put(DBhelper.IMAGE, answers.getImage());

        contentValues.put(DBhelper.UPDATED_AT, answers.getUpdated_at());
        contentValues.put(DBhelper.CONTENT, answers.getContent());
        contentValues.put(DBhelper.RESPONSE_ID, answers.getResponseId());
        contentValues.put(DBhelper.QUESTION_ID, answers.getQuestion_id());
        contentValues.put(DBhelper.TYPE, answers.getType());

        return sqLiteDatabase.insert(DBhelper.TABLE_answers, null, contentValues);
    }

    public long insertDataRecordsTable(Records records) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBhelper.ID, getMaxRecordId() + 20);
        contentValues.put(DBhelper.CATEGORY_ID, records.getCategoryId());
        contentValues.put(DBhelper.RESPONSE_ID, records.getResponseId());
        return sqLiteDatabase.insert(DBhelper.TABLE_records, null, contentValues);
    }

    public long getMaxRecordId() {
        long id = 0;
        String[] coloums = {DBhelper.ID};
        Cursor cursor = sqLiteDatabase.query(DBhelper.TABLE_records, coloums, null, null, null, null,
                null);
        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex(DBhelper.ID);
            id = cursor.getInt(index);

        }
        return id;
    }

    public Cursor getRecordIdsByResponseId(int responseId) {
        List<Integer> ids;
        ids = new ArrayList<>();
        String[] coloums = {DBhelper.ID, DBhelper.WEB_ID, DBhelper.CATEGORY_ID};
        String[] selectionArgs = {String.valueOf(responseId)};
        Cursor cursor = sqLiteDatabase.query(DBhelper.TABLE_records, coloums, DBhelper.RESPONSE_ID + " " +
                "=?", selectionArgs, null, null, null);


        return cursor;
    }

    public int updateRecordsTable(int recordId, int responseId) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBhelper.WEB_ID, recordId);

        String[] args = {String.valueOf(recordId), String.valueOf(responseId)};
        return sqLiteDatabase.update(DBhelper.TABLE_records, contentValues, DBhelper.ID + " =? " +
                "AND " + DBhelper.RESPONSE_ID + " =?", args);
    }

    public int updateAnswerTable(int responseId, int recordId, int oldRecordId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBhelper.RECORD_ID, recordId);
        String[] args = {String.valueOf(responseId), String.valueOf(oldRecordId)};
        return sqLiteDatabase.update(DBhelper.TABLE_answers, contentValues, DBhelper.RESPONSE_ID + " =? " +
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
        return sqLiteDatabase.insert(DBhelper.TABLE_responses, null, contentValues);
    }

    public class DBhelper extends SQLiteOpenHelper {
        public static final String DATABASE_NAME = "SurveyAppDatabase";
        public static final String TABLE_answers = "answers";
        public static final String ID = "id";
        public static final String ANSWER_ID = "answer_id";
        public static final String RECORD_ID = "record_id";
        public static final String WEB_ID = "web_id";
        private static final int DATABASE_VERSION = 1;
        private static final String TABLE_sqlite_sequence = "sqlite_sequence";
        private static final String TABLE_choices = "choices";
        private static final String TABLE_questions = "questions";
        private static final String TABLE_options = "options";
        private static final String TABLE_surveys = "surveys";
        private static final String TABLE_categories = "categories";
        private static final String TABLE_records = "records";
        private static final String TABLE_responses = "responses";
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
        public static final String CATEGORY_ID = "category_id";
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
        private static final String CREATE_TABLE_choices = "CREATE TABLE "
                + TABLE_choices + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + OPTION_ID + " INTEGER," + OPTION + " VARCHAR(255)," + TYPE + " VARCHAR(255)," +
                ANSWER_ID + " INTEGER " + ")";
        private static final String CREATE_TABLE_answers = "CREATE TABLE "
                + TABLE_answers + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + RECORD_ID + " INTEGER," + TYPE + " VARCHAR(255) ," + UPDATED_AT + " INTEGER," + CONTENT + " VARCHAR(255)," + IMAGE + " VARCHAR(255)," + RESPONSE_ID + " INTEGER," + QUESTION_ID + " INTEGER" + ")";
        private static final String CREATE_TABLE_questions = "CREATE TABLE "
                + TABLE_questions + "(" + ID + " INTEGER PRIMARY KEY,"
                + IDENTIFIER + " INTEGER," + PARENT_ID + " INTEGER," + MIN_VALUE + " INTEGER," + MAX_VALUE + " INTEGER," + TYPE + " VARCHAR(255)," + CONTENT + " VARCHAR(255)," + SURVEY_ID + " INTEGER," + MAX_LENGTH + " INTEGER," + MANDATORY + " INTEGER," + IMAGE_URL + " VARCHAR(255)," + ORDER_NUMBER + " INTEGER," + CATEGORY_ID + " INTEGER" + ")";
        private static final String CREATE_TABLE_options = "CREATE TABLE "
                + TABLE_options + "(" + ID + " INTEGER PRIMARY KEY,"
                + ORDER_NUMBER + " INTEGER," + CONTENT + " VARCHAR(255)," + QUESTION_ID + " INTEGER" + ")";
        private static final String CREATE_TABLE_surveys = "CREATE TABLE "
                + TABLE_surveys + "(" + ID + " INTEGER PRIMARY KEY,"
                + PUBLISHED_ON + " VARCHAR(255)," + NAME + " VARCHAR(255)," + EXPIRY_DATE + " VARCHAR(255)," + DESCRIPTION + " VARCHAR(255)" + ")";
        private static final String CREATE_TABLE_categories = "CREATE TABLE "
                + TABLE_categories + "(" + ID + " INTEGER PRIMARY KEY,"
                + CONTENT + " VARCHAR(255)," + TYPE + " INTEGER," + SURVEY_ID + " INTEGER," + PARENT_ID + " INTEGER," + ORDER_NUMBER + " INTEGER," + CATEGORY_ID + " INTEGER" + ")";
        private static final String CREATE_TABLE_records = "CREATE TABLE "
                + TABLE_records + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + RESPONSE_ID + " INTEGER," + WEB_ID + "  INTEGER DEFAULT 0," + CATEGORY_ID + " INTEGER" + ")";
        private static final String CREATE_TABLE_responses = "CREATE TABLE "
                + TABLE_responses + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MOBILE_ID + " VARCHAR(255)," + USER_ID + " INTEGER," + LONGITUDE + " VARCHAR(255)," + LATITUDE + " VARCHAR(255)," + UPDATED_AT + " INTEGER," + SURVEY_ID + " INTEGER," + WEB_ID + " INTEGER," + STATUS + " VARCHAR(255)," + ORGANISATION_ID + " INTEGER" + ")";
        private Context mcontext;

        public DBhelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mcontext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(CREATE_TABLE_choices);
            sqLiteDatabase.execSQL(CREATE_TABLE_questions);
            sqLiteDatabase.execSQL(CREATE_TABLE_options);
            sqLiteDatabase.execSQL(CREATE_TABLE_surveys);
            sqLiteDatabase.execSQL(CREATE_TABLE_categories);
            sqLiteDatabase.execSQL(CREATE_TABLE_records);
            sqLiteDatabase.execSQL(CREATE_TABLE_responses);
            sqLiteDatabase.execSQL(CREATE_TABLE_answers);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {


        }
    }

}
