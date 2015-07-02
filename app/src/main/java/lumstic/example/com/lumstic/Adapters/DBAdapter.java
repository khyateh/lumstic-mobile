package lumstic.example.com.lumstic.Adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import lumstic.example.com.lumstic.Models.Answers;
import lumstic.example.com.lumstic.Models.Categories;
import lumstic.example.com.lumstic.Models.Choices;
import lumstic.example.com.lumstic.Models.Options;
import lumstic.example.com.lumstic.Models.Questions;
import lumstic.example.com.lumstic.Models.Records;
import lumstic.example.com.lumstic.Models.Responses;
import lumstic.example.com.lumstic.Models.Surveys;

public class DBAdapter {
    DBhelper dBhelper;
    SQLiteDatabase sqLiteDatabase;
    Context context;

    public DBAdapter(Context context) {
        dBhelper = new DBhelper(context);
        sqLiteDatabase = dBhelper.getWritableDatabase();
        this.context=context;
    }

    public long getMaxID() {
        long id = 0;
        String[] coloums={DBhelper.ID};
        Cursor cursor=sqLiteDatabase.query(DBhelper.TABLE_responses,coloums,null,null,null,null,null);
        while(cursor.moveToNext()){
            int index=cursor.getColumnIndex(DBhelper.ID);
            id=cursor.getInt(index);

        }
        return id;
    }

    public long getMaxIDAnswersTabele() {
        long id = 0;
        String[] coloums={DBhelper.ID};
        Cursor cursor=sqLiteDatabase.query(DBhelper.TABLE_answers,coloums,null,null,null,null,null);
        while(cursor.moveToNext()){
            int index=cursor.getColumnIndex(DBhelper.ID);
            id=cursor.getInt(index);

        }
        return id;
    }

    public long deleteOption(Options options){

        int id = options.getId();
        String[] selectionArgs={String.valueOf(options.getId())};
        int id1= sqLiteDatabase.delete(DBhelper.TABLE_choices, DBhelper.OPTION_ID + "=?", selectionArgs);
        return id1;
    }





    public String getAnswer(int responseId,int questionId)
    {
        String answer="";
        String[] coloums={DBhelper.CONTENT};
        String[] selectionArgs={String.valueOf(responseId),String.valueOf(questionId)};
        Cursor cursor=sqLiteDatabase.query(DBhelper.TABLE_answers, coloums, DBhelper.RESPONSE_ID + " =? AND " + DBhelper.QUESTION_ID + " =?", selectionArgs, null, null, null);

        while(cursor.moveToNext()){
            int index=cursor.getColumnIndex(DBhelper.CONTENT);
            answer=cursor.getString(index);

        }

        return answer;
    }

    public String getImage(int responseId,int questionId)
    {
        String answer="";
        String[] coloums={DBhelper.IMAGE};
        String[] selectionArgs={String.valueOf(responseId),String.valueOf(questionId)};
        Cursor cursor=sqLiteDatabase.query(DBhelper.TABLE_answers, coloums, DBhelper.RESPONSE_ID + " =? AND " + DBhelper.QUESTION_ID + " =?", selectionArgs, null, null, null);

        while(cursor.moveToNext()){
            int index=cursor.getColumnIndex(DBhelper.IMAGE);
            answer=cursor.getString(index);

        }

        return answer;
    }


    public List<Integer> getIdFromAnswerTable(int responseId,int questionId)
    {
        int id=0;

        List<Integer> integers= new ArrayList<Integer>();
        String[] coloums={DBhelper.ID};
        String[] selectionArgs={String.valueOf(responseId),String.valueOf(questionId)};
        Cursor cursor=sqLiteDatabase.query(DBhelper.TABLE_answers, coloums, DBhelper.RESPONSE_ID + " =? AND " + DBhelper.QUESTION_ID + " =?", selectionArgs, null, null, null);

        while(cursor.moveToNext()){
            int index=cursor.getColumnIndex(DBhelper.ID);
            id=cursor.getInt(index);

            integers.add(id);



        }
        return integers;

    }


    public  List<Integer> getOptionIds(List<Integer>  ids){
        List<Integer> integers= new ArrayList<Integer>();

        int id=0;
        String[] coloums={DBhelper.OPTION_ID};

        for(int i=0;i<ids.size();i++){


            String[] selectionArgs={String.valueOf(ids.get(i))};
            Cursor cursor=sqLiteDatabase.query(DBhelper.TABLE_choices, coloums, DBhelper.ANSWER_ID + " =?", selectionArgs, null, null, null);

            while(cursor.moveToNext()){
                int index=cursor.getColumnIndex(DBhelper.OPTION_ID);
                id=cursor.getInt(index);
                integers.add(id);

            }
        }


        return integers;

    }

    public int deleteFromChoicesTableWhereOptionId(int optionId){



        int id = optionId;
        String[] selectionArgs={String.valueOf(id)};
        int id1= sqLiteDatabase.delete(DBhelper.TABLE_choices, DBhelper.OPTION_ID+ "=?",selectionArgs);
        return id1;


    }




    public int deleteImagePath(int responseId, int questionId){
        int id = 0;
        String[] selectionArgs={String.valueOf(responseId),String.valueOf(questionId)};
        int id1= sqLiteDatabase.delete(DBhelper.TABLE_answers, DBhelper.RESPONSE_ID + " =? AND " + DBhelper.QUESTION_ID + " =?", selectionArgs);
        return id1;
    }


    public int UpldateCompleteResponse(int responseId, int surveyId)
    {

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBhelper.STATUS,"complete");
        String[] args={String.valueOf(responseId),String.valueOf(surveyId)};
        int x=sqLiteDatabase.update(DBhelper.TABLE_responses, contentValues, DBhelper.ID + " =? AND " + DBhelper.SURVEY_ID + " =?", args);
        return x;

    }



    public int getIncompleteResponse(int surveyId){
        int value=0;
        String[] coloums={DBhelper.STATUS};
        String[] selectionArgs={String.valueOf(surveyId)};
        Cursor cursor=sqLiteDatabase.query(DBhelper.TABLE_responses, coloums, DBhelper.SURVEY_ID + " =?", selectionArgs, null, null, null);

        while(cursor.moveToNext()){
            int index=cursor.getColumnIndex(DBhelper.STATUS);
            if(cursor.getString(index).equals("incomplete")){
                value++;
            }

        }

        return value;
    }

    public int getCompleteResponse(int surveyId){
        int value=0;
        String[] coloums={DBhelper.STATUS};
        String[] selectionArgs={String.valueOf(surveyId)};
        Cursor cursor=sqLiteDatabase.query(DBhelper.TABLE_responses, coloums, DBhelper.SURVEY_ID + " =?", selectionArgs, null, null, null);

        while(cursor.moveToNext()){
            int index=cursor.getColumnIndex(DBhelper.STATUS);
            if(cursor.getString(index).equals("complete")){
                value++;
            }

        }

        return value;
    }

    public int getCompleteResponseFull(){
        int value=0;
        String[] coloums={DBhelper.STATUS};
        Cursor cursor=sqLiteDatabase.query(DBhelper.TABLE_responses, coloums, null, null, null, null, null);

        while(cursor.moveToNext()){
            int index=cursor.getColumnIndex(DBhelper.STATUS);
            if(cursor.getString(index).equals("complete")){
                value++;
            }

        }

        return value;
    }


    public List<Integer> getIncompleteResponsesIds(int surveyId){
        List<Integer> ids;
        ids= new ArrayList<Integer>();
        String[] coloums={DBhelper.STATUS,DBhelper.ID};
        String[] selectionArgs={String.valueOf(surveyId)};
        Cursor cursor=sqLiteDatabase.query(DBhelper.TABLE_responses, coloums, DBhelper.SURVEY_ID + " =?", selectionArgs, null, null, null);

        while(cursor.moveToNext()){
            int index=cursor.getColumnIndex(DBhelper.STATUS);
            if(cursor.getString(index).equals("incomplete")){

                int index2=cursor.getColumnIndex(DBhelper.ID);
                ids.add(cursor.getInt(index2));
            }

        }

        return ids;
    }

    public List<Integer> getCompleteResponsesIds(int surveyId){
        List<Integer> ids;
        ids= new ArrayList<Integer>();
        String[] coloums={DBhelper.STATUS,DBhelper.ID};
        String[] selectionArgs={String.valueOf(surveyId)};
        Cursor cursor=sqLiteDatabase.query(DBhelper.TABLE_responses, coloums, DBhelper.SURVEY_ID + " =?", selectionArgs, null, null, null);

        while(cursor.moveToNext()){
            int index=cursor.getColumnIndex(DBhelper.STATUS);
            if(cursor.getString(index).equals("complete")){

                int index2=cursor.getColumnIndex(DBhelper.ID);
                ids.add(cursor.getInt(index2));
            }

        }

        return ids;
    }

    public List<Answers> getAnswerByResponseId(int responseId){
        List<Answers> answersList;
        answersList= new ArrayList<Answers>();

        String[] coloums={DBhelper.ID,DBhelper.QUESTION_ID,DBhelper.CONTENT,DBhelper.IMAGE,DBhelper.UPDATED_AT,DBhelper.TYPE};
        String[] selectionArgs={String.valueOf(responseId)};
        Cursor cursor=sqLiteDatabase.query(DBhelper.TABLE_answers, coloums, DBhelper.RESPONSE_ID + " =?", selectionArgs, null, null, null);

        while(cursor.moveToNext()){
            int index1=cursor.getColumnIndex(DBhelper.QUESTION_ID);
            int index5=cursor.getColumnIndex(DBhelper.ID);
            int index2=cursor.getColumnIndex(DBhelper.CONTENT);
            int index3=cursor.getColumnIndex(DBhelper.IMAGE);
            int index6=cursor.getColumnIndex(DBhelper.TYPE);
            int index4=cursor.getColumnIndex(DBhelper.UPDATED_AT);
            Answers answers= new Answers();
            answers.setQuestion_id(cursor.getInt(index1));
            answers.setContent(cursor.getString(index2));
            answers.setImage(cursor.getString(index3));
            answers.setType(cursor.getString(index6));
            answers.setUpdated_at(cursor.getLong(index4));
            answers.setId(cursor.getInt(index5));
            answersList.add(answers);
        }


        return answersList;
    }


    public int deleteFromResponseTableOnUpload(int surveyId){

        int id = surveyId;
        String[] selectionArgs={String.valueOf(id),"complete"};
        int id1 = sqLiteDatabase.delete(DBhelper.TABLE_responses, DBhelper.SURVEY_ID + " =? AND " + DBhelper.STATUS + " =?", selectionArgs);
        return id1;
    }


    public int getChoicesCountWhereAnswerIdIs(int answerId) {


        int count = 0;
        String[] coloums = {DBhelper.OPTION_ID};
        String[] selectionArgs = {String.valueOf(answerId)};
        Cursor cursor = sqLiteDatabase.query(DBhelper.TABLE_choices, coloums, DBhelper.ANSWER_ID + " =?", selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            count++;
        }
        return count;
    }

    public String getQuestionTypeWhereAnswerIdIs(int answerId) {


        String type="";
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

    public int getAnswerId(int respnseId, int questionId) {


        int count=0;
        String[] coloums = {DBhelper.ID};
        String[] selectionArgs = {String.valueOf(respnseId),String.valueOf(questionId)};
        Cursor cursor = sqLiteDatabase.query(DBhelper.TABLE_answers, coloums, DBhelper.RESPONSE_ID + " =? AND " + DBhelper.QUESTION_ID + " =?", selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex(DBhelper.ID);
            count = cursor.getInt(index);


        }


        return count;


    }

    public List<Integer> getChoicesWhereAnswerCountIsMoreThanOne(int answerId) {


        List<Integer> optionList;
        optionList = new ArrayList<>();
        String content = null;
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

int count=0;

        String[] coloums = {DBhelper.OPTION_ID};
        String[] selectionArgs = {String.valueOf(answerId)};
        Cursor cursor = sqLiteDatabase.query(DBhelper.TABLE_choices, coloums, DBhelper.ANSWER_ID + " =?", selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            count++;

        }


        return count;


    }

    public boolean doesAnswerExist(int id,int responseid){
        int count=0;

        String[] coloums = {DBhelper.ID};
        String[] selectionArgs = {String.valueOf(id),String.valueOf(responseid)};
        Cursor cursor = sqLiteDatabase.query(DBhelper.TABLE_answers, coloums, DBhelper.QUESTION_ID + " =? AND " + DBhelper.RESPONSE_ID + " =?", selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            count++;
        }
        if(count>0)
            return true;
        else
            return false;
    }


    public int deleteRatingAnswer(int id,int responseId){


        String[] selectionArgs={String.valueOf(id),String.valueOf(responseId)};
        int id1 = sqLiteDatabase.delete(DBhelper.TABLE_answers, DBhelper.QUESTION_ID + " =? AND " + DBhelper.RESPONSE_ID + " =?", selectionArgs);
        return id1;
    }

    public String doesAnswerExistAsNonNull(int id,int responseid){



        String str="";
        String[] coloums = {DBhelper.CONTENT};
        String[] selectionArgs = {String.valueOf(id),String.valueOf(responseid)};
        Cursor cursor = sqLiteDatabase.query(DBhelper.TABLE_answers, coloums, DBhelper.QUESTION_ID + " =? AND " + DBhelper.RESPONSE_ID + " =?", selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex(DBhelper.CONTENT);
            str=cursor.getString(index);
        }

            return str;

    }


    public int deleteFromAnswerTable(int questionId, int responseId){

        String[] selectionArgs={String.valueOf(questionId),String.valueOf(responseId)};
        int id1 = sqLiteDatabase.delete(DBhelper.TABLE_answers, DBhelper.QUESTION_ID + " =? AND " + DBhelper.RESPONSE_ID + " =?", selectionArgs);
        return id1;
    }

    public long insertDataQuestionTable(Questions questions) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBhelper.IDENTIFIER, questions.getIdentifier());
        contentValues.put(DBhelper.PARENT_ID, questions.getParentId());
        contentValues.put(DBhelper.MIN_VALUE, questions.getMinValue());
        contentValues.put(DBhelper.MAX_VALUE, questions.getMaxVlue());
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

    public long insertDataChoicesTable(Choices choices) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBhelper.OPTION_ID, choices.getOptionId());
        contentValues.put(DBhelper.ANSWER_ID, choices.getAnswerId());
        contentValues.put(DBhelper.OPTION, choices.getOption());
        contentValues.put(DBhelper.TYPE, choices.getType());
        return sqLiteDatabase.insert(DBhelper.TABLE_choices, null, contentValues);
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
        //contentValues.put(DBhelper.ID, answers.getId());
        contentValues.put(DBhelper.RECORD_ID, answers.getRecordId());
        contentValues.put(DBhelper.IMAGE, answers.getImage());
        contentValues.put(DBhelper.WEB_ID, answers.getWebId());
        contentValues.put(DBhelper.UPDATED_AT, answers.getUpdated_at());
        contentValues.put(DBhelper.CONTENT, answers.getContent());
        contentValues.put(DBhelper.RESPONSE_ID, answers.getResponseId());

        contentValues.put(DBhelper.QUESTION_ID, answers.getQuestion_id());
        contentValues.put(DBhelper.TYPE, answers.getType());
        //  Toast.makeText(context,answers.getType(),Toast.LENGTH_SHORT).show();
        return sqLiteDatabase.insert(DBhelper.TABLE_answers, null, contentValues);
    }

    public long insertDataRecordsTable(Records records) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBhelper.ID, records.getId());
        contentValues.put(DBhelper.CATEGORY_ID, records.getCategoryId());
        contentValues.put(DBhelper.RESPONSE_ID, records.getResponseId());
        contentValues.put(DBhelper.WEB_ID, records.getWebId());
        return sqLiteDatabase.insert(DBhelper.TABLE_records, null, contentValues);
    }

    public long insertDataResponsesTable(Responses responses) {
        ContentValues contentValues = new ContentValues();
        //contentValues.put(DBhelper.ID, responses.getId());
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
        private Context mcontext;
        private static final String DATABASE_NAME = "SurveyAppDatabase";
        private static final int DATABASE_VERSION = 1;
        private static final String TABLE_sqlite_sequence = "sqlite_sequence";
        private static final String TABLE_choices = "choices";
        private static final String TABLE_questions = "questions";
        private static final String TABLE_options = "options";
        private static final String TABLE_surveys = "surveys";
        private static final String TABLE_categories = "categories";
        private static final String TABLE_answers = "answers";
        private static final String TABLE_records = "records";
        private static final String TABLE_responses = "responses";
        private static final String ID = "id";
        private static final String OPTION = "option";
        private static final String OPTION_ID = "option_id";
        private static final String ANSWER_ID = "answer_id";
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
        private static final String CATEGORY_ID = "category_id";
        private static final String QUESTION_ID = "question_id";
        private static final String PUBLISHED_ON = "published_on";
        private static final String NAME = "name";
        private static final String DESCRIPTION = "description";
        private static final String EXPIRY_DATE = "expiry_date";
        private static final String RECORD_ID = "record_id";
        private static final String IMAGE = "image";
        private static final String WEB_ID = "web_id";
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
                + OPTION_ID + " INTEGER," + OPTION + " VARCHAR(255)," + TYPE + " VARCHAR(255)," + ANSWER_ID + " INTEGER" + ")";
        private static final String CREATE_TABLE_answers = "CREATE TABLE "
                + TABLE_answers + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + RECORD_ID + " INTEGER," + TYPE + " VARCHAR(255) ," + WEB_ID + " INTEGER," + UPDATED_AT + " INTEGER," + CONTENT + " VARCHAR(255)," + IMAGE + " VARCHAR(255)," + RESPONSE_ID + " INTEGER," + QUESTION_ID + " INTEGER" + ")";

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
                + TABLE_records + "(" + ID + " INTEGER PRIMARY KEY,"
                + RESPONSE_ID + " INTEGER," + WEB_ID + " INTEGER," + CATEGORY_ID + " INTEGER" + ")";

        private static final String CREATE_TABLE_responses = "CREATE TABLE "
                + TABLE_responses + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MOBILE_ID + " VARCHAR(255)," + USER_ID + " INTEGER," + LONGITUDE + " VARCHAR(255)," + LATITUDE + " VARCHAR(255)," + UPDATED_AT + " INTEGER," + SURVEY_ID + " INTEGER," + WEB_ID + " INTEGER," + STATUS + " VARCHAR(255)," + ORGANISATION_ID + " INTEGER" + ")";
        public DBhelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            Log.e("contructor", "called");
            mcontext = context;
            // Toast.makeText(context, "constructor", Toast.LENGTH_LONG).show();
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
            Log.e("success", "success");

            // Toast.makeText(mcontext, "oncreate", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {


        }
    }

}
