package lumstic.ashoka.com.lumstic.UI;

import android.app.ActionBar;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import lumstic.ashoka.com.lumstic.Adapters.DBAdapter;
import lumstic.ashoka.com.lumstic.Adapters.DropDownAdapter;
import lumstic.ashoka.com.lumstic.Models.Answers;
import lumstic.ashoka.com.lumstic.Models.Categories;
import lumstic.ashoka.com.lumstic.Models.Choices;
import lumstic.ashoka.com.lumstic.Models.DropDown;
import lumstic.ashoka.com.lumstic.Models.MasterQuestion;
import lumstic.ashoka.com.lumstic.Models.MasterSurveyQuestionModel;
import lumstic.ashoka.com.lumstic.Models.Options;
import lumstic.ashoka.com.lumstic.Models.Questions;
import lumstic.ashoka.com.lumstic.Models.Surveys;
import lumstic.ashoka.com.lumstic.Models.TagModel;
import lumstic.ashoka.com.lumstic.R;
import lumstic.ashoka.com.lumstic.Utils.CommonUtil;
import lumstic.ashoka.com.lumstic.Utils.IntentConstants;

public class NewResponseActivity extends BaseActivity {
    DBAdapter dbAdapter;
    ActionBar actionBar;
    private List<MasterQuestion> masterQuestionList = new ArrayList<>();
    private String fname = "";
    private int currentResponseId = 0;
    private int totalQuestionCount = 0;
    private int CAMERA_REQUEST = 1;
    private int questionCounter = 0;
    private int categoryQuestionCounter = 0;
    private Surveys surveys;
    private EditText answer;
    private TextView dateText;
    private Spinner spinner;
    private RadioGroup radioGroup;
    private FrameLayout imageContainer;
    private ImageView imageViewPhotoQuestion;
    private RelativeLayout deleteImageRelativeLayout;
    private Button counterButton, markAsComplete;
    private Button nextQuestion, previousQuestion;
    private LinearLayout fieldContainer;
    private LayoutInflater inflater;
    private RatingBar ratingBar;
    private int recordId = 0;
    private String order = "";
    private MasterQuestion tagMasterQuestion;
    private int addRecordCounter = 0;
    private int defaultParentIndex = 0;
    private LinearLayout takePictureContainer;

    private ArrayList<MasterSurveyQuestionModel> sortedSurveyQuestionsList = new ArrayList();
    private ArrayList<MasterSurveyQuestionModel> surveyQuestionsList = new ArrayList();

    public static ArrayList<View> getViewsByTag(ViewGroup root, TagModel tag) {
        ArrayList<View> views = new ArrayList<>();
        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                views.addAll(getViewsByTag((ViewGroup) child, tag));
            }

            final TagModel tagObj = (TagModel) child.getTag(R.string.multirecord_tag);
            if (tagObj != null && tagObj.getRecordID() == tag.getRecordID()) {
                views.add(child);
            }

        }
        return views;
    }

    private void addNestedQuestionsToQuestionList(List<Questions> localQuestionsList) {
        if (surveys.getQuestions().size() > 0) {
            for (int j = 0; j < surveys.getQuestions().size(); j++) {
                for (int k = 0; k < localQuestionsList.size(); k++) {
                    for (int l = 0; l < localQuestionsList.get(k).getOptions().size(); l++) {
                        if (surveys.getQuestions().get(j).getParentId() == localQuestionsList.get(k).getOptions().get(l).getId()) {


                            if (!localQuestionsList.get(k).getOptions().get(l).getQuestions().contains(surveys.getQuestions().get(j)))
                                localQuestionsList.get(k).getOptions().get(l).getQuestions().add(surveys.getQuestions().get(j));

                            addNestedQuestionsToQuestionList(localQuestionsList.get(k).getOptions().get(l).getQuestions());

                        }
                    }
                }
            }
        }
    }

    private void addRootLevelQuestionsTOList(List<Questions> localQuestionsList, List<Questions> localnonRootedQuestionsList) {
        if (surveys.getQuestions().size() > 0) {
            for (int i = 0; i < surveys.getQuestions().size(); i++) {
                if (surveys.getQuestions().get(i).getCategoryId() == 0 && surveys.getQuestions().get(i).getParentId() == 0) {
                    localQuestionsList.add(surveys.getQuestions().get(i));
                } else {
                    localnonRootedQuestionsList.add(surveys.getQuestions().get(i));
                }
            }

        }
    }

    private List<Categories> addCategoryQuestionsTOCategory(List<Questions> localNonRootedQuestionsList) {
        List<Categories> localCategoriesList = new ArrayList<>();
        if (surveys.getCategories().size() > 0) {
            localCategoriesList = surveys.getCategories();

            for (int i = 0; i < localCategoriesList.size(); i++) {
                for (int j = 0; j < localNonRootedQuestionsList.size(); j++) {
                    if ((localCategoriesList.get(i).getId() == localNonRootedQuestionsList.get(j).getCategoryId()) && (!localCategoriesList.get(i).getQuestionsList().contains(localNonRootedQuestionsList.get(j)))) {
                        localCategoriesList.get(i).getQuestionsList().add(localNonRootedQuestionsList.get(j));
                    }
                }
            }


            for (int i = 0; i < localCategoriesList.size(); i++) {
                for (int j = i + 1; j < localCategoriesList.size(); j++) {
                    if (localCategoriesList.get(i).getId() == localCategoriesList.get(j).getCategoryId()) {
                        //Add to master list
                        localCategoriesList.get(i).setCategories(localCategoriesList.get(j));
                    } else if (localCategoriesList.get(j).getId() == localCategoriesList.get(i).getCategoryId()) {
                        localCategoriesList.get(j).setCategories(localCategoriesList.get(i));
                    }
                }
            }

        }
        return localCategoriesList;
    }

    private void addQuestionsToMasterList(List<Questions> localQuestionsList, List<Categories> localCategoriesList) {
        for (int i = 0; i < localQuestionsList.size(); i++) {
            surveyQuestionsList.add(new MasterSurveyQuestionModel(localQuestionsList.get(i), CommonUtil.TYPE_QUESTION));
        }
        for (int i = 0; i < localCategoriesList.size(); i++) {
            if (localCategoriesList.get(i).getCategoryId() == 0) {
                surveyQuestionsList.add(new MasterSurveyQuestionModel(localCategoriesList.get(i), CommonUtil.TYPE_CATEGORY));
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_response);
        //action bar attributes
        setActionbar();
        //views declaration
        setViews();
        //layout inflater initialization
        inflater = getLayoutInflater();
        dbAdapter = new DBAdapter(NewResponseActivity.this);
        //declaration of list items
        List<Questions> questionsList = new ArrayList<>();
        List<Categories> categoriesList;
        List<Questions> nonRootedQuestionsList = new ArrayList<>();

        //create mark as complete button and mandatory text
        createMarkAsComplete();
        makeMandatoryText();
        previousQuestion.setText("BACK");
        //surveys from previous activity
        surveys = (Surveys) getIntent().getExtras().getSerializable(IntentConstants.SURVEY);
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        addRootLevelQuestionsTOList(questionsList, nonRootedQuestionsList);
        //Root Level Questions
        addNestedQuestionsToQuestionList(questionsList);
        //Non Root Level Questions
        addNestedQuestionsToQuestionList(nonRootedQuestionsList);

        categoriesList = addCategoryQuestionsTOCategory(nonRootedQuestionsList);


        addQuestionsToMasterList(questionsList, categoriesList);

        getSortedQuestionList(surveyQuestionsList);

        totalQuestionCount = sortedSurveyQuestionsList.size();
        counterButton.setText("1 out of " + totalQuestionCount);

        //get app response id
        getResponseId();

        buildFirstQuestion();

        checkIfLastQuestion();

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        //save various answers on mark as complete
        markAsComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onMarkComplete();
            }
        });

        //on next pressed
        nextQuestion.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if ((questionCounter + 1) != totalQuestionCount) {
                    onNextClick();
                }
            }
        });

        //on previous pressed
        previousQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (questionCounter != 0) {
                    onBackClicked();
                }
            }
        });
    }

    ArrayList<MasterSurveyQuestionModel> getSortedQuestionList(ArrayList<MasterSurveyQuestionModel> localMasterSurveyQuestionModelsList) {

        sortedSurveyQuestionsList = localMasterSurveyQuestionModelsList;
        for (int out = localMasterSurveyQuestionModelsList.size() - 1; out >= 0; out--) {
            for (int i = 0; i < out; i++) {
                int n = i + 1;
                int x, y;
                if (sortedSurveyQuestionsList.get(i).getType() == CommonUtil.TYPE_QUESTION) {
                    x = ((Questions) sortedSurveyQuestionsList.get(i).getObject()).getOrderNumber();
                } else {
                    x = ((Categories) sortedSurveyQuestionsList.get(i).getObject()).getOrderNumber();
                }

                if (sortedSurveyQuestionsList.get(n).getType() == CommonUtil.TYPE_QUESTION) {
                    y = ((Questions) sortedSurveyQuestionsList.get(n).getObject()).getOrderNumber();
                } else {
                    y = ((Categories) sortedSurveyQuestionsList.get(n).getObject()).getOrderNumber();
                }


                if (x > y) {
                    MasterSurveyQuestionModel temp = sortedSurveyQuestionsList.get(i);
                    sortedSurveyQuestionsList.set(i, sortedSurveyQuestionsList.get(n));
                    sortedSurveyQuestionsList.set(n, temp);
                }
            }
        }

        return sortedSurveyQuestionsList;
    }

    public void setActionbar() {
        actionBar = getActionBar();
        actionBar.setTitle("New Response Activity");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
    }

    public void setViews() {
        counterButton = (Button) findViewById(R.id.counter_button);
        fieldContainer = (LinearLayout) findViewById(R.id.field_container);
        nextQuestion = (Button) findViewById(R.id.next_queation);
        previousQuestion = (Button) findViewById(R.id.previous_question);
        markAsComplete = new Button(this);
    }

    public void createMarkAsComplete() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 5;
        params.topMargin = 30;
        params.bottomMargin = 30;
        params.gravity = Gravity.CENTER;
        markAsComplete.setBackgroundResource(R.drawable.selector_custom_button);
        markAsComplete.setText("mark as complete");
        markAsComplete.setGravity(Gravity.CENTER_HORIZONTAL);
        markAsComplete.setTextColor(getResources().getColor(R.color.white));
        markAsComplete.setLayoutParams(params);
        markAsComplete.setVisibility(View.GONE);
    }

    public TextView makeMandatoryText() {
        TextView mandatoryText = new TextView(this);
        mandatoryText.setTextSize(16);
        mandatoryText.setTextColor(getResources().getColor(R.color.login_button_color));
        mandatoryText.setPadding(0, 0, 0, 8);
        mandatoryText.setText(" The Question Is Mandatory");
        return mandatoryText;
    }


    public void getResponseId() {
        if (getIntent().hasExtra(IntentConstants.RESPONSE_ID)) {
            currentResponseId = getIntent().getIntExtra(IntentConstants.RESPONSE_ID, 0);
        }
        if (!getIntent().hasExtra(IntentConstants.RESPONSE_ID)) {
            currentResponseId = (int) dbAdapter.getMaxID();
        }
    }

//    public void getCategoryAndQuestionsCount(Surveys surveys) {
//        int questionCount = 0, categoryCount = 0;
//        if (surveys.getQuestions().size() > 0) {
//            questionsList = surveys.getQuestions();
//            questionCount = questionsList.size();
//        }
//
//
//        if (surveys.getCategories().size() > 0) {
//            categoriesList = surveys.getCategories();
//            categoryCount = categoriesList.size();
//        }
//
//
//        totalQuestionCount = categoryCount + questionCount;
//    }


    public void onMarkComplete() {

        if (storeDataToDB() == 0) {
            return;
        }
        addRecordCounter = 0;
        recordId = 0;


        if (sortedSurveyQuestionsList.get(questionCounter).getType() == CommonUtil.TYPE_QUESTION) {
            int surveyID = ((Questions) sortedSurveyQuestionsList.get(questionCounter).getObject()).getSurveyId();
            dbAdapter.UpdateCompleteResponse(currentResponseId, surveyID);
        } else {
            int surveyID = ((Categories) sortedSurveyQuestionsList.get(questionCounter).getObject()).getSurveyId();
            dbAdapter.UpdateCompleteResponse(currentResponseId, surveyID);
        }


        Intent intent = new Intent(NewResponseActivity.this, SurveyDetailsActivity.class);
        intent.putExtra(IntentConstants.SURVEY, (java.io.Serializable) surveys);
        startActivity(intent);
        finish();

    }

    public void buildFirstQuestion() {

        if (sortedSurveyQuestionsList.get(questionCounter).getType() == CommonUtil.TYPE_QUESTION) {

            Questions cq = ((Questions) sortedSurveyQuestionsList.get(questionCounter).getObject());
            buildLayout(cq, false, recordId, defaultParentIndex);
        } else {
            Categories currentCategory = ((Categories) sortedSurveyQuestionsList.get(questionCounter).getObject());
            //for multi record questions
            if (currentCategory.getType().equals(CommonUtil.CATEGORY_TYPE_MULTI_RECORD)) {

                NextBackCommonCode(currentCategory);

            } else {
                TagModel tmpTagModel = new TagModel(0, recordId);
                setCategoryTitle(currentCategory, tmpTagModel);
                buildCategoryLayout(currentCategory);
            }
        }

    }

    public void checkIfLastQuestion() {

        if (questionCounter + 1 == totalQuestionCount) {
            createMarkAsComplete();
            markAsComplete.setVisibility(View.VISIBLE);
            fieldContainer.addView(markAsComplete);
            nextQuestion.setTextColor(getResources().getColor(R.color.back_button_text));
            nextQuestion.setText("NEXT");
            nextQuestion.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_next_disable, 0);
            nextQuestion.setBackgroundColor(getResources().getColor(R.color.back_button_background));
        }
    }

    // build category layout
    public void buildCategoryLayout(Categories categories) {
        //  setCategoryTitle(categories);

        for (int j = 0; j < categories.getQuestionsList().size(); j++) {
            categoryQuestionCounter++;
            buildLayout(categories.getQuestionsList().get(j), false, recordId, defaultParentIndex);
        }

        if (categories.getCategories() != null) {

            if (categories.getCategories().getType().equals(CommonUtil.CATEGORY_TYPE_MULTI_RECORD)) {
                NextBackCommonCode(categories.getCategories());
            } else {
                TagModel tmpTagModel = new TagModel(0, recordId);
                setCategoryTitle(categories.getCategories(), tmpTagModel);
                buildCategoryLayout(categories.getCategories());
            }


        }
        categoryQuestionCounter = 0;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_response, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.save) {

            backSaveCommonCode();

            finish();
            return true;
        } else if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public LinearLayout createNestedContainer() {
        LinearLayout nestedContainer = new LinearLayout(this);
        nestedContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 10, 0, 0);
        nestedContainer.setPadding(10, 20, 10, 20);
        nestedContainer.setLayoutParams(layoutParams);
        nestedContainer.setBackgroundColor(Color.WHITE);

        return nestedContainer;
    }

    public TextView createQuestionTitle(Questions ques, boolean isChild, int parentRecordId) {
        TextView questionTextSingleLine = new TextView(this);
        questionTextSingleLine.setTextSize(20);
        questionTextSingleLine.setTextColor(getResources().getColor(R.color.text_color));
        questionTextSingleLine.setPadding(0, 0, 0, 16);
        if (isChild) {
            questionTextSingleLine.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), parentRecordId, ques));
        } else {
            questionTextSingleLine.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), recordId, ques));
        }

        if (categoryQuestionCounter > 0) {

            questionTextSingleLine.setTextSize(20);
            if (ques.getMandatory() == 1) {
                questionTextSingleLine.setText("Q." + (questionCounter + 1) + "." + categoryQuestionCounter + "  " + ques.getContent() + " *");
                if (ques.getParentId() > 0)
                    questionTextSingleLine.setText("Q." + "  " + ques.getContent() + " *");
            } else {
                questionTextSingleLine.setText("Q." + (questionCounter + 1) + "." + categoryQuestionCounter + "  " + ques.getContent());
                if (ques.getParentId() > 0)
                    questionTextSingleLine.setText("Q." + "  " + ques.getContent());

            }
        } else {
            if (ques.getMandatory() == 1) {
                questionTextSingleLine.setText("Q." + (questionCounter + 1) + "  " + ques.getContent() + " *");
                if (ques.getParentId() > 0)
                    questionTextSingleLine.setText("Q." + (questionCounter + 1) + "." + order + "  " + ques.getContent() + " *");
            } else {
                questionTextSingleLine.setText("Q." + (questionCounter + 1) + "  " + ques.getContent());
                if (ques.getParentId() > 0)
                    questionTextSingleLine.setText("Q." + (questionCounter + 1) + "." + order + "  " + ques.getContent());

            }
        }
        return questionTextSingleLine;

    }

    //hide keypad on next click and various events
    public void hideKeypad(EditText answer) {
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(answer.getWindowToken(), 0);
    }

    public void buildLayout(final Questions ques, final boolean isChild, final int parentRecordId, int parentViewPosition) {
        //to adjust mark as complete position at last of the layout
        if (markAsComplete.getVisibility() == View.VISIBLE)
            fieldContainer.removeView(markAsComplete);


        //if question is single line question
        if (ques.getType().equals(CommonUtil.QUESTION_TYPE_SINGLE_LINE_QUESTION)) {
            LinearLayout nestedContainer = createNestedContainer();
            TextView questionTextSingleLine = createQuestionTitle(ques, isChild, parentRecordId);
            nestedContainer.addView(questionTextSingleLine);
            if (isChild) {
                nestedContainer.setId(ques.getId() + IntentConstants.VIEW_CONSTANT_NESTED_CONTAINERS + parentRecordId);
            } else {
                nestedContainer.setId(ques.getId() + IntentConstants.VIEW_CONSTANT_NESTED_CONTAINERS + recordId);
            }
//            nestedContainer.setId(ques.getId());
            nestedContainer.addView(inflater.inflate(R.layout.answer_single_line, null));


            if (isChild) {
                nestedContainer.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), parentRecordId, ques));
                fieldContainer.addView(nestedContainer, parentViewPosition);

            } else {
                nestedContainer.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), recordId, ques));
                fieldContainer.addView(nestedContainer);

            }

            answer = (EditText) findViewById(R.id.answer_text);
            answer.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            if (ques.getMaxLength() > 0)
                answer.setHint(ques.getMaxLength() + " Characters");
            if (isChild) {
                answer.setId(ques.getId() + IntentConstants.VIEW_CONSTANT + parentRecordId);
                masterQuestionList.add(new MasterQuestion(ques, parentRecordId, ques.getId() + IntentConstants.VIEW_CONSTANT + parentRecordId));
            } else {
                answer.setId(ques.getId() + IntentConstants.VIEW_CONSTANT + recordId);
                masterQuestionList.add(new MasterQuestion(ques, recordId, ques.getId() + IntentConstants.VIEW_CONSTANT + recordId));
            }
            hideKeypad(answer);

            answer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (!hasFocus) {
                        answer = (EditText) view;
                        Long tsLong = System.currentTimeMillis() / 1000;
                        TagModel localTagModel = (TagModel) ((View) view.getParent()).getTag(R.string.multirecord_tag);
                        Answers localAnswer = new Answers(localTagModel.getRecordID(), currentResponseId, localTagModel.getqID(), answer.getText().toString(), tsLong);
                        saveAnswerIntoDB(localTagModel, localAnswer);

                    }
                }
            });


        } else if (ques.getType().contains(CommonUtil.QUESTION_TYPE_MULTI_LINE_QUESTION)) {
            LinearLayout nestedContainer = createNestedContainer();
            TextView questionTextSingleLine = createQuestionTitle(ques, isChild, parentRecordId);
            nestedContainer.addView(questionTextSingleLine);
//            nestedContainer.setId(ques.getId());
            if (isChild) {
                nestedContainer.setId(ques.getId() + IntentConstants.VIEW_CONSTANT_NESTED_CONTAINERS + parentRecordId);
            } else {
                nestedContainer.setId(ques.getId() + IntentConstants.VIEW_CONSTANT_NESTED_CONTAINERS + recordId);
            }
            nestedContainer.addView(inflater.inflate(R.layout.answer_multi_line, null));
            if (isChild) {
                nestedContainer.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), parentRecordId, ques));
                fieldContainer.addView(nestedContainer, parentViewPosition);

            } else {
                nestedContainer.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), recordId, ques));
                fieldContainer.addView(nestedContainer);

            }
            answer = (EditText) findViewById(R.id.answer_text);
            answer.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            if (ques.getMaxLength() > 0)
                answer.setHint(ques.getMaxLength() + " Characters");
            if (isChild) {
                answer.setId(ques.getId() + IntentConstants.VIEW_CONSTANT + parentRecordId);
                masterQuestionList.add(new MasterQuestion(ques, parentRecordId, ques.getId() + IntentConstants.VIEW_CONSTANT + parentRecordId));
            } else {
                answer.setId(ques.getId() + IntentConstants.VIEW_CONSTANT + recordId);
                masterQuestionList.add(new MasterQuestion(ques, recordId, ques.getId() + IntentConstants.VIEW_CONSTANT + recordId));
            }
            answer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (hasFocus) {
                        answer = (EditText) view;
                    } else {
                        Long tsLong = System.currentTimeMillis() / 1000;
                        TagModel localTagModel = (TagModel) ((View) view.getParent()).getTag(R.string.multirecord_tag);
                        Answers localAnswer = new Answers(localTagModel.getRecordID(), currentResponseId, localTagModel.getqID(), answer.getText().toString(), tsLong);
                        saveAnswerIntoDB(localTagModel, localAnswer);
                    }
                }
            });


        } else if (ques.getType().contains(CommonUtil.QUESTION_TYPE_DROPDOWN_QUESTION)) {


            final LinearLayout nestedContainer = createNestedContainer();
            TextView questionTextSingleLine = createQuestionTitle(ques, isChild, parentRecordId);
            nestedContainer.addView(questionTextSingleLine);
            if (isChild) {
                nestedContainer.setId(ques.getId() + IntentConstants.VIEW_CONSTANT_NESTED_CONTAINERS + parentRecordId);
            } else {
                nestedContainer.setId(ques.getId() + IntentConstants.VIEW_CONSTANT_NESTED_CONTAINERS + recordId);
            }
            spinner = new Spinner(NewResponseActivity.this);
            spinner = (Spinner) getLayoutInflater().inflate(R.layout.answer_dropdown, null);
            nestedContainer.addView(spinner);
            spinner.setTag(order);
            if (isChild) {
                nestedContainer.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), parentRecordId, ques));
                spinner.setId(ques.getId() + IntentConstants.VIEW_CONSTANT + parentRecordId);
                fieldContainer.addView(nestedContainer, parentViewPosition);
                masterQuestionList.add(new MasterQuestion(ques, parentRecordId, ques.getId() + IntentConstants.VIEW_CONSTANT + parentRecordId));
                addAnswerNextClick(ques, parentRecordId);
            } else {
                nestedContainer.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), recordId, ques));
                spinner.setId(ques.getId() + IntentConstants.VIEW_CONSTANT + recordId);
                fieldContainer.addView(nestedContainer);
                masterQuestionList.add(new MasterQuestion(ques, recordId, ques.getId() + IntentConstants.VIEW_CONSTANT + recordId));
                addAnswerNextClick(ques, recordId);

            }


            List<DropDown> dropDowns = new ArrayList<>();
            dropDowns.add(new DropDown(order, "Select one"));

            for (int i = 0; i < ques.getOptions().size(); i++) {
                dropDowns.add(new DropDown(order, ques.getOptions().get(i).getContent()));

            }

            DropDownAdapter dropDownAdapter = new DropDownAdapter(NewResponseActivity.this, dropDowns);
            spinner.setAdapter(dropDownAdapter);
            if (isChild) {
                spinner.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), parentRecordId, ques));
            } else {
                spinner.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), recordId, ques));
            }
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                    order = view.findViewById(R.id.spinner_item).getTag().toString();
                    if (i == 0) {
                        removeOthersFromDataBaseForDefaultDropDown(ques, ((TagModel) nestedContainer.getTag(R.string.multirecord_tag)).getRecordID());
                        removeCategoryViewForDefaultDropDown(ques, ((TagModel) nestedContainer.getTag(R.string.multirecord_tag)).getRecordID());
                        removeQuestionViewForDefaultDropDown(ques, ((TagModel) nestedContainer.getTag(R.string.multirecord_tag)).getRecordID());

                    }

                    if (i != 0) {
                        //add option selected to database table choices table
                        Options options = ques.getOptions().get(i - 1);
                        addOptionToDataBase(options, ques, ((TagModel) nestedContainer.getTag(R.string.multirecord_tag)).getRecordID());
                        removeOthersFromDataBase(options, ques, ((TagModel) nestedContainer.getTag(R.string.multirecord_tag)).getRecordID());


                        String temp = order;
                        //create nested questions
                        if (options.getQuestions().size() > 0) {
                            for (int j = 0; j < options.getQuestions().size(); j++) {
                                order = temp + Integer.toString(0 + 1);
                                buildLayout(options.getQuestions().get(j), true, ((TagModel) nestedContainer.getTag(R.string.multirecord_tag)).getRecordID(), fieldContainer.indexOfChild(nestedContainer) + 1);
                            }
                        }

                        //create nested categories
                        if (options.getCategories().size() > 0) {
                            for (int j = 0; j < options.getCategories().size(); j++) {
                                buildCategoryLayout(options.getCategories().get(j));
                            }
                        }

                        //remove views from non selected nested categories and questions
                        for (int j = 0; j < ques.getOptions().size(); j++) {
                            if (!ques.getOptions().get(j).getContent().equals(options.getContent())) {
                                removeQuestionViewFromUI(ques.getOptions().get(j), ((TagModel) nestedContainer.getTag(R.string.multirecord_tag)).getRecordID());
                                removeCategoryViewFromUI(ques.getOptions().get(j), ((TagModel) nestedContainer.getTag(R.string.multirecord_tag)).getRecordID());
                            }
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }

            });

        } else if (ques.getType().contains(CommonUtil.QUESTION_TYPE_MULTI_CHOICE_QUESTION)) {


            final LinearLayout nestedContainer = createNestedContainer();
            TextView questionTextSingleLine = createQuestionTitle(ques, isChild, parentRecordId);
            nestedContainer.addView(questionTextSingleLine);
//            nestedContainer.setId(ques.getId());
            if (isChild) {
                nestedContainer.setId(ques.getId() + IntentConstants.VIEW_CONSTANT_NESTED_CONTAINERS + parentRecordId);
            } else {
                nestedContainer.setId(ques.getId() + IntentConstants.VIEW_CONSTANT_NESTED_CONTAINERS + recordId);
            }

            if (isChild) {
                nestedContainer.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), parentRecordId, ques));
                fieldContainer.addView(nestedContainer, parentViewPosition);
                masterQuestionList.add(new MasterQuestion(ques, parentRecordId));
//                if (!dbAdapter.doesAnswerExist(ques.getId(), currentResponseId, parentRecordId)) {
                addAnswerNextClick(ques, parentRecordId);

//                }
            } else {
                nestedContainer.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), recordId, ques));
                fieldContainer.addView(nestedContainer);
                masterQuestionList.add(new MasterQuestion(ques, recordId));
//                if (!dbAdapter.doesAnswerExist(ques.getId(), currentResponseId, recordId)) {
                addAnswerNextClick(ques, recordId);

//                }
            }
            final LinearLayout ll = new LinearLayout(this);
            ll.setTag(order);
            ll.setOrientation(LinearLayout.VERTICAL);
            nestedContainer.addView(ll);
            for (int i = 0; i < ques.getOptions().size(); i++) {
                final CheckBox checkBox = new CheckBox(this);
                ll.addView(checkBox);
                if (isChild) {
                    checkBox.setId(ques.getOptions().get(i).getId() + parentRecordId);
                } else {
                    checkBox.setId(ques.getOptions().get(i).getId() + recordId);
                }
//                checkBox.setId(ques.getOptions().get(i).getId() + recordId);
                checkBox.setText(ques.getOptions().get(i).getContent());
                checkBox.setTextSize(16);
                checkBox.setTextColor(getResources().getColor(R.color.text_color));
                checkBox.setText(ques.getOptions().get(i).getContent());
                checkBox.setTag(ques.getOptions().get(i));


                if (isChild) {
                    nestedContainer.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), parentRecordId, ques));

                } else {
                    nestedContainer.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), recordId, ques));

                }


                checkBox.setTag(R.string.app_name, ll.getTag());

                checkBox.setButtonDrawable(R.drawable.custom_checkbox);
                checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (ques.getParentId() == 0) {

                        }

                        if (((CheckBox) view).isChecked()) {

                            CheckBox checkBox1 = (CheckBox) view;
                            Options options = (Options) checkBox1.getTag();
                            String tempOrder = "";
                            int x = options.getOrderNumber() + 97;
                            String character = Character.toString((char) x);


                            ll.setTag(findViewById(view.getId()).getTag(R.string.app_name));

                            if (ques.getParentId() == 0) {
                                order = "";
                                order = tempOrder + character + ".";
                            } else {
                                if (checkBox.getTag(R.string.app_name) != null)
                                    tempOrder = findViewById(view.getId()).getTag(R.string.app_name).toString();
                                order = "";
                                order = tempOrder + ".";
                            }


                            order = tempOrder + character + ".";
                            addOptionToDataBase(options, ques, ((TagModel) nestedContainer.getTag(R.string.multirecord_tag)).getRecordID());


                            //create layout for nested question of check box questions
                            try {
                                if (options.getQuestions().size() > 0) {
                                    for (int i = 0; i < options.getQuestions().size(); i++) {
                                        order = order + Integer.toString(0 + 1);
                                        buildLayout(options.getQuestions().get(i), true, ((TagModel) nestedContainer.getTag(R.string.multirecord_tag)).getRecordID(), fieldContainer.indexOfChild(nestedContainer) + 1);
                                    }
                                }
                                //create layout for nested categories of check box questions
                                if (options.getCategories().size() > 0) {
                                    for (int i = 0; i < options.getCategories().size(); i++) {
                                        buildCategoryLayout(options.getCategories().get(i));
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        //if check box is unchecked, we have to handle questions and categories of options not selected
                        if (!((CheckBox) view).isChecked()) {
                            CheckBox checkBox1 = (CheckBox) view;
                            Options options = (Options) checkBox1.getTag();
                            removeOptionFromDataBase(options, ques, ((TagModel) nestedContainer.getTag(R.string.multirecord_tag)).getRecordID());
                            if (options.getQuestions().size() > 0) {
                                removeQuestionViewFromUI(options, ((TagModel) nestedContainer.getTag(R.string.multirecord_tag)).getRecordID());
                            }
                            if (options.getCategories().size() > 0) {
                                removeCategoryViewFromUI(options, ((TagModel) nestedContainer.getTag(R.string.multirecord_tag)).getRecordID());
                            }
                        }
                    }
                });
            }


        } else if (ques.getType().contains(CommonUtil.QUESTION_TYPE_NUMERIC_QUESTION)) {
            LinearLayout nestedContainer = createNestedContainer();
            TextView questionTextSingleLine = createQuestionTitle(ques, isChild, parentRecordId);
            nestedContainer.addView(questionTextSingleLine);
            nestedContainer.addView(inflater.inflate(R.layout.answer_numeric, null));
//            nestedContainer.setId(ques.getId());
            if (isChild) {
                nestedContainer.setId(ques.getId() + IntentConstants.VIEW_CONSTANT_NESTED_CONTAINERS + parentRecordId);
            } else {
                nestedContainer.setId(ques.getId() + IntentConstants.VIEW_CONSTANT_NESTED_CONTAINERS + recordId);
            }
            if (isChild) {
                nestedContainer.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), parentRecordId, ques));
                fieldContainer.addView(nestedContainer, parentViewPosition);

            } else {
                nestedContainer.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), recordId, ques));

                fieldContainer.addView(nestedContainer);
            }

            answer = (EditText) findViewById(R.id.answer_text);
            if (isChild) {
                answer.setId(ques.getId() + IntentConstants.VIEW_CONSTANT + parentRecordId);
                masterQuestionList.add(new MasterQuestion(ques, parentRecordId, ques.getId() + IntentConstants.VIEW_CONSTANT + parentRecordId));
            } else {
                answer.setId(ques.getId() + IntentConstants.VIEW_CONSTANT + recordId);
                masterQuestionList.add(new MasterQuestion(ques, recordId, ques.getId() + IntentConstants.VIEW_CONSTANT + recordId));
            }
            if (ques.getMaxValue() != ques.getMinValue())
                answer.setHint("Between  " + ques.getMinValue() + " to " + ques.getMaxValue());
            answer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                public void onFocusChange(View view, boolean hasFocus) {
                    if (hasFocus) {
                        answer = (EditText) view;
                    } else {
                        Long tsLong = System.currentTimeMillis() / 1000;
                        TagModel localTagModel = (TagModel) ((View) view.getParent()).getTag(R.string.multirecord_tag);
                        Answers localAnswer = new Answers(localTagModel.getRecordID(), currentResponseId, localTagModel.getqID(), answer.getText().toString(), tsLong);
                        saveAnswerIntoDB(localTagModel, localAnswer);
                    }
                }
            });


        } else if (ques.getType().contains(CommonUtil.QUESTION_TYPE_DATE_QUESTION)) {
            final LinearLayout nestedContainer = createNestedContainer();
            TextView questionTextSingleLine = createQuestionTitle(ques, isChild, parentRecordId);
            nestedContainer.addView(questionTextSingleLine);
            nestedContainer.addView(inflater.inflate(R.layout.answer_date_picker, null));
//            nestedContainer.setId(ques.getId());
            if (isChild) {
                nestedContainer.setId(ques.getId() + IntentConstants.VIEW_CONSTANT_NESTED_CONTAINERS + parentRecordId);
            } else {
                nestedContainer.setId(ques.getId() + IntentConstants.VIEW_CONSTANT_NESTED_CONTAINERS + recordId);
            }
            nestedContainer.setTag(ques);

            if (isChild) {
                nestedContainer.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), parentRecordId, ques));
                fieldContainer.addView(nestedContainer, parentViewPosition);
                masterQuestionList.add(new MasterQuestion(ques, parentRecordId));
            } else {
                nestedContainer.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), recordId, ques));
                fieldContainer.addView(nestedContainer);
                masterQuestionList.add(new MasterQuestion(ques, recordId));
            }


            dateText = (TextView) findViewById(R.id.answer_text_date);
            dateText.setText("dd.yy.mm");
            dateText.setId(ques.getId() + IntentConstants.VIEW_CONSTANT + recordId);

            dateText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Calendar c = Calendar.getInstance();
                    int mYear = c.get(Calendar.YEAR);
                    int mMonth = c.get(Calendar.MONTH);
                    int mDay = c.get(Calendar.DAY_OF_MONTH);
                    DatePickerDialog dialog;
                    TagModel localTagModel = (TagModel) nestedContainer.getTag(R.string.multirecord_tag);
                    dialog = new DatePickerDialog(NewResponseActivity.this,
                            new mDateSetListener(localTagModel.getRecordID(), localTagModel.getQues()), mYear, mMonth, mDay);
                    dialog.show();
                }
            });

        } else if (ques.getType().contains(CommonUtil.QUESTION_TYPE_RADIO_QUESTION)) {


            final LinearLayout nestedContainer = createNestedContainer();
            TextView questionTextSingleLine = createQuestionTitle(ques, isChild, parentRecordId);
            nestedContainer.addView(questionTextSingleLine);
//            nestedContainer.setId(ques.getId());
            if (isChild) {
                nestedContainer.setId(ques.getId() + IntentConstants.VIEW_CONSTANT_NESTED_CONTAINERS + parentRecordId);
            } else {
                nestedContainer.setId(ques.getId() + IntentConstants.VIEW_CONSTANT_NESTED_CONTAINERS + recordId);
            }
            nestedContainer.setTag(ques);


            if (isChild) {
                nestedContainer.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), parentRecordId, ques));
                masterQuestionList.add(new MasterQuestion(ques, parentRecordId));
                if (!dbAdapter.doesAnswerExist(ques.getId(), currentResponseId, parentRecordId)) {
                    addAnswerNextClick(ques, parentRecordId);
                }
            } else {
                nestedContainer.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), recordId, ques));
                masterQuestionList.add(new MasterQuestion(ques, recordId));
                if (!dbAdapter.doesAnswerExist(ques.getId(), currentResponseId, recordId)) {
                    addAnswerNextClick(ques, recordId);
                }
            }
            //create new radio group
            radioGroup = new RadioGroup(this);
            if (isChild) {
                radioGroup.setId(ques.getId() + IntentConstants.VIEW_CONSTANT + parentRecordId);
            } else {
                radioGroup.setId(ques.getId() + IntentConstants.VIEW_CONSTANT + recordId);
            }
            radioGroup.setOrientation(RadioGroup.VERTICAL);
            nestedContainer.addView(radioGroup);
            radioGroup.setTag(order);

            if (isChild) {
                radioGroup.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), parentRecordId, ques));
                fieldContainer.addView(nestedContainer, parentViewPosition);
            } else {
                radioGroup.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), recordId, ques));
                fieldContainer.addView(nestedContainer);
            }
            for (int i = 0; i < ques.getOptions().size(); i++) {

                final RadioButton radioButton = new RadioButton(this);
                radioGroup.addView(radioButton);
                if (isChild) {
                    radioButton.setId(ques.getOptions().get(i).getId() + parentRecordId);
                } else {
                    radioButton.setId(ques.getOptions().get(i).getId() + recordId);
                }
//                radioButton.setId(ques.getOptions().get(i).getId() + recordId);
                radioButton.setTextSize(16);
                radioButton.setTextColor(getResources().getColor(R.color.text_color));
                radioButton.setText(ques.getOptions().get(i).getContent());
                radioButton.setTag(ques.getOptions().get(i));


                radioButton.setTag(R.string.app_name, radioGroup.getTag());


                radioButton.setButtonDrawable(R.drawable.custom_radio_button);

                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {

                        try {
                            answer.clearFocus();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        String tempOrder = "";

                        View myView = findViewById(checkedId);
                        RadioButton radioButton1 = (RadioButton) myView;
                        Options options = (Options) radioButton1.getTag();
                        int x = options.getOrderNumber() + 97;
                        String character = Character.toString((char) x);


                        group.setTag(findViewById(group.getCheckedRadioButtonId()).getTag(R.string.app_name));

                        if (ques.getParentId() == 0) {
                            order = "";
                            order = tempOrder + character + ".";
                        } else {
                            RadioButton localRadioButton = (RadioButton) findViewById(group.getCheckedRadioButtonId());
                            if (radioButton.getTag(R.string.app_name) != null)
                                tempOrder = localRadioButton.getTag(R.string.app_name).toString();
                            order = "";
                            order = tempOrder + ".";
                        }


                        order = tempOrder + character + ".";
                        addOptionToDataBase(options, ques, ((TagModel) nestedContainer.getTag(R.string.multirecord_tag)).getRecordID());

                        //remove others from database


                        removeOthersFromDataBase(options, ques, ((TagModel) nestedContainer.getTag(R.string.multirecord_tag)).getRecordID());
                        String tmp = order;
                        //decide where to make questions for nested question
                        if (options.getQuestions().size() > 0) {
                            for (int i = 0; i < options.getQuestions().size(); i++) {
                                order = tmp + Integer.toString(i + 1);
                                buildLayout(options.getQuestions().get(i), true, ((TagModel) nestedContainer.getTag(R.string.multirecord_tag)).getRecordID(), fieldContainer.indexOfChild(nestedContainer) + 1);
                            }
                        }

                        //decide where to make categories for nested question
                        if (options.getCategories().size() > 0) {
                            for (int i = 0; i < options.getCategories().size(); i++) {
                                buildCategoryLayout(options.getCategories().get(i));
                            }
                        }

                        //remove unnecessary questions and categories on other item selected
                        for (int i = 0; i < ques.getOptions().size(); i++) {
                            if (!ques.getOptions().get(i).getContent().equals(options.getContent())) {
                                removeQuestionViewFromUI(ques.getOptions().get(i), ((TagModel) nestedContainer.getTag(R.string.multirecord_tag)).getRecordID());
                                removeCategoryViewFromUI(ques.getOptions().get(i), ((TagModel) nestedContainer.getTag(R.string.multirecord_tag)).getRecordID());
                            }
                        }


                    }
                });
            }


        } else if (ques.getType().equals(CommonUtil.QUESTION_TYPE_RATING_QUESTION)) {

            final LinearLayout nestedContainer = createNestedContainer();
            TextView questionTextSingleLine = createQuestionTitle(ques, isChild, parentRecordId);

            nestedContainer.addView(questionTextSingleLine);
            nestedContainer.addView(inflater.inflate(R.layout.answer_rating, null));
//            nestedContainer.setId(ques.getId());
            if (isChild) {
                nestedContainer.setId(ques.getId() + IntentConstants.VIEW_CONSTANT_NESTED_CONTAINERS + parentRecordId);
            } else {
                nestedContainer.setId(ques.getId() + IntentConstants.VIEW_CONSTANT_NESTED_CONTAINERS + recordId);
            }
            if (isChild) {
                nestedContainer.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), parentRecordId, ques));
                fieldContainer.addView(nestedContainer, parentViewPosition);
                masterQuestionList.add(new MasterQuestion(ques, parentRecordId));
            } else {
                nestedContainer.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), recordId, ques));
                masterQuestionList.add(new MasterQuestion(ques, recordId));
                fieldContainer.addView(nestedContainer);
            }

            nestedContainer.setTag(ques);

            ratingBar = (RatingBar) findViewById(R.id.ratingBar);
            if (isChild) {
                ratingBar.setId(ques.getId() + IntentConstants.VIEW_CONSTANT + parentRecordId);
                addAnswerNextClick(ques, parentRecordId);
            } else {
                ratingBar.setId(ques.getId() + IntentConstants.VIEW_CONSTANT + recordId);
                addAnswerNextClick(ques, recordId);
            }

            ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                //answer saved on rating changed
                @Override
                public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {


                    Long tsLong = System.currentTimeMillis() / 1000;
                    TagModel localTagModel = (TagModel) (nestedContainer).getTag(R.string.multirecord_tag);

                    if (dbAdapter.doesAnswerExist(ques.getId(), currentResponseId)) {
                        dbAdapter.deleteRatingAnswer(localTagModel.getqID(), currentResponseId, localTagModel.getRecordID());
                    }
                    Answers localAnswer = new Answers(localTagModel.getRecordID(), currentResponseId, localTagModel.getqID(), String.valueOf(v), tsLong);
                    saveAnswerIntoDB(localTagModel, localAnswer);

                }
            });


        } else if (ques.getType().equals(CommonUtil.QUESTION_TYPE_PHOTO_QUESTION)) {

            final LinearLayout nestedContainer = createNestedContainer();
            TextView questionTextSingleLine = createQuestionTitle(ques, isChild, parentRecordId);
            nestedContainer.addView(questionTextSingleLine);
//            nestedContainer.setId(ques.getId());
            if (isChild) {
                nestedContainer.setId(ques.getId() + IntentConstants.VIEW_CONSTANT_NESTED_CONTAINERS + parentRecordId);
            } else {
                nestedContainer.setId(ques.getId() + IntentConstants.VIEW_CONSTANT_NESTED_CONTAINERS + recordId);
            }
            nestedContainer.addView(inflater.inflate(R.layout.answer_image_picker, null));


            if (isChild) {
                nestedContainer.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), parentRecordId, ques));
                fieldContainer.addView(nestedContainer, parentViewPosition);

            } else {
                nestedContainer.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), recordId, ques));
                fieldContainer.addView(nestedContainer);

            }
            Button lButton = (Button) findViewById(R.id.answer_text_image);
            lButton.setId(ques.getId() + IntentConstants.VIEW_CONSTANT + recordId);
            takePictureContainer = (LinearLayout) findViewById(R.id.takePictureContainer);
            deleteImageRelativeLayout = (RelativeLayout) findViewById(R.id.delete_picture);

            imageViewPhotoQuestion = (ImageView) findViewById(R.id.image);

            //  imageViewPhotoQuestion.setId(ques.getId());
            imageContainer = (FrameLayout) findViewById(R.id.image_container);


//Don't include this code in above if else
            if (isChild) {
                masterQuestionList.add(new MasterQuestion(ques, parentRecordId));
                deleteImageRelativeLayout.setId(ques.getId() + IntentConstants.VIEW_CONSTANT_FOR_PHOTO1 + parentRecordId);
                imageViewPhotoQuestion.setId(ques.getId() + IntentConstants.VIEW_CONSTANT_FOR_PHOTO2 + parentRecordId);
                imageContainer.setId(ques.getId() + IntentConstants.VIEW_CONSTANT_FOR_PHOTO3 + parentRecordId);
                takePictureContainer.setId(ques.getId() + IntentConstants.VIEW_CONSTANT_FOR_PHOTO4 + parentRecordId);
            } else {
                masterQuestionList.add(new MasterQuestion(ques, recordId));
                deleteImageRelativeLayout.setId(ques.getId() + IntentConstants.VIEW_CONSTANT_FOR_PHOTO1 + recordId);
                imageViewPhotoQuestion.setId(ques.getId() + IntentConstants.VIEW_CONSTANT_FOR_PHOTO2 + recordId);
                imageContainer.setId(ques.getId() + IntentConstants.VIEW_CONSTANT_FOR_PHOTO3 + recordId);
                takePictureContainer.setId(ques.getId() + IntentConstants.VIEW_CONSTANT_FOR_PHOTO4 + recordId);
            }


            lButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TagModel localTagModel = (TagModel) nestedContainer.getTag(R.string.multirecord_tag);
                    takePicture(localTagModel.getQues(), localTagModel.getRecordID());
                    Log.e("TAG", "RECORD ID -->>" + localTagModel.getRecordID());


                }
            });
            takePictureContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    TagModel localTagModel = (TagModel) nestedContainer.getTag(R.string.multirecord_tag);
                    takePicture(localTagModel.getQues(), localTagModel.getRecordID());

                }
            });

            deleteImageRelativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    TagModel localTagModel = (TagModel) nestedContainer.getTag(R.string.multirecord_tag);
                    findViewById(localTagModel.getqID() + IntentConstants.VIEW_CONSTANT_FOR_PHOTO4 + localTagModel.getRecordID()).setVisibility(View.VISIBLE);
                    findViewById(localTagModel.getqID() + IntentConstants.VIEW_CONSTANT_FOR_PHOTO3 + localTagModel.getRecordID()).setVisibility(View.GONE);
                    dbAdapter.deleteImagePath(currentResponseId, localTagModel.getqID(), localTagModel.getRecordID());


                }
            });

        }
        ////////////////////////////////////////
        try {
            if (isChild) {
                checkForAnswer(ques, currentResponseId, parentRecordId);
            } else {
                checkForAnswer(ques, currentResponseId, recordId);
            }
        } catch (Exception e) {

        }
        ///////////////////////////////////////

        if (markAsComplete.getVisibility() == View.VISIBLE)

        {

            fieldContainer.addView(markAsComplete);
        }
    }

    public void checkHint() {
//
//        if (hint) {
//
//            fieldContainer.addView(inflater.inflate(R.layout.hint_helper, null));
//            final RelativeLayout hintContainer = (RelativeLayout) findViewById(R.id.hint_container);
//            LinearLayout hintButtonContainer = (LinearLayout) findViewById(R.id.hint_buttons_container);
//            LinearLayout hintButtonContainer = (LinearLayout) findViewById(R.id.hint_buttons_container);
//            final Button textHintButton = (Button) findViewById(R.id.text_hint_button);
//            final Button imageHintButton = (Button) findViewById(R.id.image_hint_button);
//            hintButtonContainer.setVisibility(View.VISIBLE);
//            textHintButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    hintContainer.setVisibility(View.VISIBLE);
//                    TextView textHint = (TextView) findViewById(R.id.text_hint);
//                    ImageView imageHint = (ImageView) findViewById(R.id.image_hint);
//                    textHint.setVisibility(View.VISIBLE);
//                    textHintButton.setBackgroundResource(R.drawable.hint_button_pressed);
//                    imageHintButton.setBackgroundResource(R.drawable.hint_button);
//                    imageHint.setVisibility(View.GONE);
//                }
//            });
//            imageHintButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    hintContainer.setVisibility(View.VISIBLE);
//                    imageHintButton.setBackgroundResource(R.drawable.hint_button_pressed);
//                    textHintButton.setBackgroundResource(R.drawable.hint_button);
//                    TextView textHint = (TextView) findViewById(R.id.text_hint);
//                    ImageView imageHint = (ImageView) findViewById(R.id.image_hint);
//                    textHint.setVisibility(View.GONE);
//                    imageHint.setVisibility(View.VISIBLE);
//                }
//            });
//        }
    }

    private void hideKeyBoard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(answer.getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int storeDataToDB() {
        int success = 0;

        try {
            answer.clearFocus();
        } catch (Exception e) {
            e.printStackTrace();
        }
        hideKeyBoard();

        int masterListSize = masterQuestionList.size();
        for (int i = 0; i < masterListSize; i++) {
            Log.e("Master", "Master Details QID->>" + masterQuestionList.get(i).getQuestions().getId() + "RecordID->>" + masterQuestionList.get(i).getRecordID());

//Check For validation for these types of Questions
            if (masterQuestionList.get(i).getQuestions().getType().equals(CommonUtil.QUESTION_TYPE_SINGLE_LINE_QUESTION) || masterQuestionList.get(i).getQuestions().getType().equals(CommonUtil.QUESTION_TYPE_MULTI_LINE_QUESTION)) {
                int localMaxLength = masterQuestionList.get(i).getQuestions().getMaxLength();
                if (localMaxLength != 0)
                    if (((EditText) findViewById(masterQuestionList.get(i).getAnsAndroidID())).getText().toString().trim().length() > localMaxLength) {
                        showValidationDialog(((EditText) findViewById(masterQuestionList.get(i).getAnsAndroidID())), ((EditText) findViewById(masterQuestionList.get(i).getAnsAndroidID())).getText().toString().length());
                        return success;
                    }
            }
            if (masterQuestionList.get(i).getQuestions().getType().equals(CommonUtil.QUESTION_TYPE_NUMERIC_QUESTION)) {
                int localMaxValue = masterQuestionList.get(i).getQuestions().getMaxValue();
                int localMinValue = masterQuestionList.get(i).getQuestions().getMinValue();
                if (localMaxValue != localMinValue) {
                    String localData = ((EditText) findViewById(masterQuestionList.get(i).getAnsAndroidID())).getText().toString();
                    int localValue;
                    if (localData != null) {
                        try {
                            localValue = Integer.parseInt(localData);
                            if (localValue > localMaxValue || localValue < localMinValue) {
                                showValidationDialog(((EditText) findViewById(masterQuestionList.get(i).getAnsAndroidID())), ((EditText) findViewById(masterQuestionList.get(i).getAnsAndroidID())).getText().toString().length());
                                return success;
                            }
                        } catch (NumberFormatException e) {

                        }

                    }
                }
            }

            if (masterQuestionList.get(i).getQuestions().getMandatory() == 1) {

                if (checkMandatoryQuestion(masterQuestionList.get(i).getQuestions().getType(), masterQuestionList.get(i).getRecordID(), masterQuestionList.get(i).getQuestions().getId())) {
                    if (!masterQuestionList.get(i).getQuestions().getType().equals(CommonUtil.QUESTION_TYPE_MULTI_CHOICE_QUESTION) && !masterQuestionList.get(i).getQuestions().getType().equals(CommonUtil.QUESTION_TYPE_DROPDOWN_QUESTION) && !masterQuestionList.get(i).getQuestions().getType().equals(CommonUtil.QUESTION_TYPE_RADIO_QUESTION)) {
                        addAnswerNextClick(masterQuestionList.get(i).getQuestions(), masterQuestionList.get(i).getRecordID());
                    }
                } else {
                    showMandatoryDialog(((EditText) findViewById(masterQuestionList.get(i).getAnsAndroidID())));
                    return success;
                }

            } else {

                if (!masterQuestionList.get(i).getQuestions().getType().equals(CommonUtil.QUESTION_TYPE_MULTI_CHOICE_QUESTION) && !masterQuestionList.get(i).getQuestions().getType().equals(CommonUtil.QUESTION_TYPE_DROPDOWN_QUESTION) && !masterQuestionList.get(i).getQuestions().getType().equals(CommonUtil.QUESTION_TYPE_RADIO_QUESTION)) {
                    addAnswerNextClick(masterQuestionList.get(i).getQuestions(), masterQuestionList.get(i).getRecordID());
                }
            }


        }


        //////////////////////////////
        success = 1;
        masterQuestionList.clear();
        //////////////////////////////
        return success;
    }

    private void showValidationDialog(final EditText viewById, final int length) {
        final Dialog dialog = new Dialog(NewResponseActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //before
        dialog.setContentView(R.layout.number_limit_dialog);
        dialog.show();
        Button button = (Button) dialog.findViewById(R.id.okay);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                viewById.setFocusable(true);
                viewById.setSelection(length);
                viewById.requestFocus();

                dialog.dismiss();


            }
        });
    }


    //on next ic clicked
    public void onNextClick() {


        if (storeDataToDB() == 0) {
            return;
        }
        addRecordCounter = 0;
        recordId = 0;
        order = "";
        fname = "";

        //set next and previous buttons
        if (questionCounter < totalQuestionCount - 1) {
            previousQuestion.setBackgroundColor(getResources().getColor(R.color.login_button_color));
            previousQuestion.setTextColor(getResources().getColor(R.color.white));
            previousQuestion.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_arrow_back, 0, 0, 0);

            fieldContainer.removeAllViews();
            questionCounter++;

            counterButton.setText(questionCounter + 1 + " out of " + totalQuestionCount);


            //////////////////////////////////////////////////////////////////////////////

//            if (sortedSurveyQuestionsList.get(questionCounter).getType() == CommonUtil.TYPE_QUESTION) {
//
//                Questions cq = ((Questions) sortedSurveyQuestionsList.get(questionCounter).getObject());
//                buildLayout(cq, false, recordId, defaultParentIndex);
//            } else {
//                Categories currentCategory = ((Categories) sortedSurveyQuestionsList.get(questionCounter).getObject());
//                //for multi record questions
//                if (currentCategory.getType().equals(CommonUtil.CATEGORY_TYPE_MULTI_RECORD)) {
//
//                    NextBackCommonCode(currentCategory);
//
//                } else {
//                    buildCategoryLayout(currentCategory);
//                }
//            }

            if (sortedSurveyQuestionsList.get(questionCounter).getType() == CommonUtil.TYPE_QUESTION) {

                Questions cq = ((Questions) sortedSurveyQuestionsList.get(questionCounter).getObject());
                buildLayout(cq, false, recordId, defaultParentIndex);
            } else {
                Categories currentCategory = ((Categories) sortedSurveyQuestionsList.get(questionCounter).getObject());
                //for multi record questions
                if (currentCategory.getType().equals(CommonUtil.CATEGORY_TYPE_MULTI_RECORD)) {

                    NextBackCommonCode(currentCategory);

                } else {
                    TagModel tmpTagModel = new TagModel(0, recordId);
                    setCategoryTitle(currentCategory, tmpTagModel);
                    buildCategoryLayout(currentCategory);
                }
            }


            //////////////////////////////////////////////////////////////////////////////

            //check if questions is the last question
            checkIfLastQuestion();
        }

        //check not the first question
        if (questionCounter != 0) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayUseLogoEnabled(false);
        }

    }

    public void onAddRecordClick(int localRecordID, Categories currentCategory) {
        recordId = localRecordID;
        addRecordCounter++;
        //Added qID as 0
        TagModel tmpTagModel = new TagModel(0, localRecordID);
        createDeleteRecord(tmpTagModel, currentCategory);
        setCategoryQuestion(currentCategory, localRecordID);
        if (currentCategory.getCategories() != null) {
            setCategoryTitle(currentCategory.getCategories(), tmpTagModel);
            buildCategoryLayout(currentCategory.getCategories());
        }
        try {
            answer.clearFocus();
        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    public void buildMultiRecordTitle(Categories categories) {
        LinearLayout nestedContainer = new LinearLayout(this);
        nestedContainer.setOrientation(LinearLayout.VERTICAL);
        TextView questionTextSingleLine = new TextView(this);
        questionTextSingleLine.setTextSize(20);
        questionTextSingleLine.setTextColor(Color.BLACK);
        questionTextSingleLine.setPadding(8, 12, 8, 20);
        questionTextSingleLine.setText("Q." + (questionCounter + 1) + " " + categories.getContent());
        nestedContainer.addView(questionTextSingleLine);
        nestedContainer.setId(categories.getId());
        nestedContainer.setTag(categories);
        nestedContainer.setBackgroundColor(Color.WHITE);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        layoutParams.setMargins(0, 10, 0, 10);
        fieldContainer.addView(nestedContainer, layoutParams);


    }

    public void createDeleteRecord(TagModel tmpTagModel, Categories currentCategory) {

        if (currentCategory.getType().equals(CommonUtil.CATEGORY_TYPE_MULTI_RECORD)) {

            Button deleteRecord = new Button(this);
            deleteRecord.setText("Delete Record");
            deleteRecord.setTextColor(Color.WHITE);
            deleteRecord.setBackgroundResource(R.drawable.selector_custom_button);
            Drawable drawable = getResources().getDrawable(R.drawable.ic_delete_img);
            drawable.setBounds(0, 0, (int) (drawable.getIntrinsicWidth() * 0.2),
                    (int) (drawable.getIntrinsicHeight() * 0.2));
            deleteRecord.setCompoundDrawables(drawable, null, null, null);
            deleteRecord.setTag(R.string.multirecord_tag, tmpTagModel);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.RIGHT;
            layoutParams.setMargins(0, 10, 0, 10);
            fieldContainer.addView(deleteRecord, layoutParams);


            deleteRecord.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addRecordCounter--;

                    TagModel tag = (TagModel) view.getTag(R.string.multirecord_tag);

                    ArrayList<MasterQuestion> localMasterQuestions = getQuestionByRecordID(new MasterQuestion(null, tag.getRecordID()));

                    for (int j = 0; j < localMasterQuestions.size(); j++) {
                        masterQuestionList.remove(localMasterQuestions.get(j));
                        int localAnswerID = dbAdapter.getAnswerId(currentResponseId, localMasterQuestions.get(j).getQuestions().getId(), localMasterQuestions.get(j).getRecordID());
                        dbAdapter.deleteFromAnswerTableWithRecordId(localMasterQuestions.get(j).getQuestions().getId(), currentResponseId, localMasterQuestions.get(j).getRecordID());
                        dbAdapter.deleteFromChoicesTableWhereAnswerId(localAnswerID);
                    }

                    ArrayList<View> localViews = getViewsByTag(fieldContainer, tag);
                    Log.e("TAG", "Number Of Views ->>" + localViews.size());

                    for (int i = 0; i < localViews.size(); i++) {
                        fieldContainer.removeView(localViews.get(i));
                    }

                }
            });


        }
    }

    public ArrayList<MasterQuestion> getQuestionByRecordIDAndQuestionID(MasterQuestion tag) {
        ArrayList<MasterQuestion> views = new ArrayList<>();
        for (int i = 0; i < masterQuestionList.size(); i++) {
            MasterQuestion tagObj = masterQuestionList.get(i);
            if (tagObj != null && tagObj.getRecordID() == tag.getRecordID() && tagObj.getQuestions().getId() == tag.getQuestions().getId()) {
                views.add(tagObj);

            }

        }
        return views;
    }

    public ArrayList<MasterQuestion> getQuestionByRecordID(MasterQuestion tag) {
        ArrayList<MasterQuestion> views = new ArrayList<>();
        for (int i = 0; i < masterQuestionList.size(); i++) {
            MasterQuestion tagObj = masterQuestionList.get(i);
            if (tagObj != null && tagObj.getRecordID() == tag.getRecordID()) {
                views.add(tagObj);

            }

        }
        return views;
    }

    private void createAddRecordButton(final Categories currentCategory) {
        Button addRecord = new Button(this);
        addRecord.setBackgroundResource(R.drawable.selector_custom_button);
        addRecord.setText("+  Add Record");
        addRecord.setTextColor(getResources().getColor(R.color.white));

        fieldContainer.addView(addRecord);
        addRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (addRecordCounter == 0) {
                    recordId = currentCategory.getCategoryId();
                } else {
                    recordId = recordId + 20;
                }
                onAddRecordClick(recordId, currentCategory);


            }
        });
    }

    private void NextBackCommonCode(final Categories currentCategory) {


        int localCategorySize = currentCategory.getQuestionsList().size();
        Cursor multiRecordCursor = null;
        if (localCategorySize > 0) {
            multiRecordCursor = dbAdapter.findNoOfEntries(currentCategory.getQuestionsList().get(0).getId(), currentResponseId);
        }
        int entries = 0;
        if (multiRecordCursor != null) {
            addRecordCounter = entries = multiRecordCursor.getCount();
            Log.e("TAG", "LOCAL->> C" + entries);
        }

        buildMultiRecordTitle(currentCategory);

        createAddRecordButton(currentCategory);


        if (entries == 0) {
            recordId = currentCategory.getCategoryId();
        } else {
            multiRecordCursor.moveToFirst();
            for (int i = 0; i < entries; i++) {
                int localRecordID = multiRecordCursor.getInt(multiRecordCursor.getColumnIndex(DBAdapter.DBhelper.RECORD_ID));
                onAddRecordClick(localRecordID, currentCategory);
                multiRecordCursor.moveToNext();
            }
        }
    }

    private void backSaveCommonCode() {
        for (int i = 0; i < masterQuestionList.size(); i++) {
            Log.e("Master", "Master Details QID->>" + masterQuestionList.get(i).getQuestions().getId() + "RecordID->>" + masterQuestionList.get(i).getRecordID());

            if (!masterQuestionList.get(i).getQuestions().getType().equals(CommonUtil.QUESTION_TYPE_MULTI_CHOICE_QUESTION) && !masterQuestionList.get(i).getQuestions().getType().equals(CommonUtil.QUESTION_TYPE_DROPDOWN_QUESTION) && !masterQuestionList.get(i).getQuestions().getType().equals(CommonUtil.QUESTION_TYPE_RADIO_QUESTION))
                addAnswerNextClick(masterQuestionList.get(i).getQuestions(), masterQuestionList.get(i).getRecordID());
        }
    }

    //on back is clicked
    public void onBackClicked() {


        markAsComplete.setVisibility(View.GONE);
        hideKeyBoard();
        addRecordCounter = 0;
        recordId = 0;
        order = "";

        backSaveCommonCode();
        fname = "";

        //////////////////////////////
        masterQuestionList.clear();
        //////////////////////////////

        if (questionCounter != 0) {
            try {
                answer.clearFocus();
            } catch (Exception e) {
                e.printStackTrace();
            }

            fieldContainer.removeAllViews();
            //change count
            questionCounter--;


            counterButton.setText(questionCounter + 1 + " out of " + totalQuestionCount);

            //////////////////////////////////////////////////////////////////////////////

//            if (sortedSurveyQuestionsList.get(questionCounter).getType() == CommonUtil.TYPE_QUESTION) {
//
//                Questions cq = ((Questions) sortedSurveyQuestionsList.get(questionCounter).getObject());
//                buildLayout(cq, false, recordId, defaultParentIndex);
//            } else {
//                Categories currentCategory = ((Categories) sortedSurveyQuestionsList.get(questionCounter).getObject());
//                //for multi record questions
//                if (currentCategory.getType().equals(CommonUtil.CATEGORY_TYPE_MULTI_RECORD)) {
//
//                    NextBackCommonCode(currentCategory);
//
//                } else {
//                    buildCategoryLayout(currentCategory);
//                }
//            }
            if (sortedSurveyQuestionsList.get(questionCounter).getType() == CommonUtil.TYPE_QUESTION) {

                Questions cq = ((Questions) sortedSurveyQuestionsList.get(questionCounter).getObject());
                buildLayout(cq, false, recordId, defaultParentIndex);
            } else {
                Categories currentCategory = ((Categories) sortedSurveyQuestionsList.get(questionCounter).getObject());
                //for multi record questions
                if (currentCategory.getType().equals(CommonUtil.CATEGORY_TYPE_MULTI_RECORD)) {

                    NextBackCommonCode(currentCategory);

                } else {
                    TagModel tmpTagModel = new TagModel(0, recordId);
                    setCategoryTitle(currentCategory, tmpTagModel);
                    buildCategoryLayout(currentCategory);
                }
            }
            //////////////////////////////////////////////////////////////////////////////


            //if its the first question
            if (questionCounter == 0) {
                previousQuestion.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_arrow_back_enable, 0, 0, 0);
                previousQuestion.setTextColor(getResources().getColor(R.color.back_button_text));
                previousQuestion.setBackgroundColor(getResources().getColor(R.color.back_button_background));
            }
            //check if is not the last question
            if (questionCounter + 1 != totalQuestionCount) {
                nextQuestion.setTextColor(getResources().getColor(R.color.white));
                nextQuestion.setText("NEXT");
                nextQuestion.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_next, 0);
                nextQuestion.setBackgroundColor(getResources().getColor(R.color.login_button_color));
            }
        }
        //check if its the first question
        if (questionCounter == 0) {
            setActionBarTrue();
        }
    }

    //get data from camera activity
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            findViewById(tagMasterQuestion.getQuestions().getId() + IntentConstants.VIEW_CONSTANT_FOR_PHOTO3 + tagMasterQuestion.getRecordID()).setVisibility(View.VISIBLE);
            ((ImageView) findViewById(tagMasterQuestion.getQuestions().getId() + IntentConstants.VIEW_CONSTANT_FOR_PHOTO2 + tagMasterQuestion.getRecordID())).setImageBitmap(photo);
            findViewById(tagMasterQuestion.getQuestions().getId() + IntentConstants.VIEW_CONSTANT_FOR_PHOTO4 + tagMasterQuestion.getRecordID()).setVisibility(View.GONE);
            SaveImage(photo);
            dbAdapter.deleteFromAnswerTable(tagMasterQuestion.getQuestions().getId(), currentResponseId, tagMasterQuestion.getRecordID());
            addAnswerNextClick(tagMasterQuestion.getQuestions(), tagMasterQuestion.getRecordID());
        }
    }

    private void setActionBarTrue() {
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);
    }

    //save image to sd card
    private void SaveImage(Bitmap finalBitmap) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/saved_images");
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        fname = "Image-" + n + ".jpg";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //load image from memory card
    private Bitmap loadImageFromStorage(String path, String fileName) {
        Bitmap b = null;
        try {
            File f = new File(path, fileName);
            b = BitmapFactory.decodeStream(new FileInputStream(f));
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return b;
    }


    //set category titles
    public void setCategoryTitle(Categories categories, TagModel tmpTagModel) {
        LinearLayout nestedContainer = new LinearLayout(this);
        nestedContainer.setOrientation(LinearLayout.VERTICAL);
        TextView questionTextSingleLine = new TextView(this);
        questionTextSingleLine.setTextSize(20);
        questionTextSingleLine.setTextColor(Color.BLACK);
        questionTextSingleLine.setPadding(8, 12, 8, 20);
        questionTextSingleLine.setText("Q." + (questionCounter + 1) + "." + order + "  " + categories.getContent());
        nestedContainer.addView(questionTextSingleLine);
        nestedContainer.setId(categories.getId());
        nestedContainer.setTag(categories);
        nestedContainer.setTag(R.string.multirecord_tag, tmpTagModel);
        nestedContainer.setBackgroundColor(Color.WHITE);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 10, 0, 10);
        fieldContainer.addView(nestedContainer, layoutParams);
    }

    public void setCategoryQuestion(Categories categories, int localRecordID) {

//        for (int j = categories.getQuestionsList().size() - 1; j >= 0; j--) {
//            categoryQuestionCounter++;
//            buildLayout(categories.getQuestionsList().get(j), false, localRecordID, defaultParentIndex);
//        }
        for (int j = 0; j < categories.getQuestionsList().size(); j++) {
            categoryQuestionCounter++;
            buildLayout(categories.getQuestionsList().get(j), false, localRecordID, defaultParentIndex);
        }
        categoryQuestionCounter = 0;

    }


    //remove questions view from main container
    public void removeQuestionViewFromUI(Options options, int localRecordID) {
        try {
            for (int i = 0; i < options.getQuestions().size(); i++) {
                View myView = findViewById(options.getQuestions().get(i).getId() + IntentConstants.VIEW_CONSTANT_NESTED_CONTAINERS + localRecordID);
                ViewGroup parent = (ViewGroup) myView.getParent();
                parent.removeView(myView);

                if (options.getQuestions().get(i).getOptions().size() > 0) {
                    for (int j = 0; j < options.getQuestions().get(i).getOptions().size(); j++) {
                        removeQuestionViewFromUI(options.getQuestions().get(i).getOptions().get(j), localRecordID);
                        removeCategoryViewFromUI(options.getQuestions().get(i).getOptions().get(j), localRecordID);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //remove questions view from main container
    public void removeQuestionViewForDefaultDropDown(Questions question, int localRecordID) {
        try {
            for (int w = 0; w < question.getOptions().size(); w++) {
                for (int i = 0; i < question.getOptions().get(w).getQuestions().size(); i++) {
                    View myView = findViewById(question.getOptions().get(w).getQuestions().get(i).getId() + IntentConstants.VIEW_CONSTANT_NESTED_CONTAINERS + localRecordID);
                    ViewGroup parent = (ViewGroup) myView.getParent();
                    parent.removeView(myView);

                    if (question.getOptions().get(w).getQuestions().get(i).getOptions().size() > 0) {
                        for (int j = 0; j < question.getOptions().get(w).getQuestions().get(i).getOptions().size(); j++) {
                            for (int k = 0; k < question.getOptions().get(w).getQuestions().get(i).getOptions().get(j).getQuestions().size(); k++) {
                                removeQuestionViewForDefaultDropDown(question.getOptions().get(w).getQuestions().get(i).getOptions().get(j).getQuestions().get(k), localRecordID);
                                removeCategoryViewForDefaultDropDown(question.getOptions().get(w).getQuestions().get(i).getOptions().get(j).getQuestions().get(k), localRecordID);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeCategoryViewForDefaultDropDown(Questions question, int localRecordID) {

        try {

            for (int w = 0; w < question.getOptions().size(); w++) {
                for (int k = 0; k < question.getOptions().get(w).getCategories().size(); k++) {

                    View myView = findViewById(question.getOptions().get(w).getQuestions().get(k).getId() + IntentConstants.VIEW_CONSTANT_NESTED_CONTAINERS + localRecordID);
                    ViewGroup parent = (ViewGroup) myView.getParent();
                    parent.removeView(myView);
                    for (int h = 0; h < question.getOptions().get(w).getCategories().get(k).getQuestionsList().size(); h++) {
                        View myView2 = findViewById(question.getOptions().get(w).getCategories().get(k).getQuestionsList().get(h).getId());
                        ViewGroup parent2 = (ViewGroup) myView2.getParent();
                        parent2.removeView(myView2);
                        if (question.getOptions().get(w).getCategories().get(k).getQuestionsList().get(h).getOptions().size() > 0) {
                            for (int j = 0; j < question.getOptions().get(w).getCategories().get(k).getQuestionsList().get(h).getOptions().size(); j++) {
                                for (int l = 0; l < question.getOptions().get(w).getCategories().get(k).getQuestionsList().get(h).getOptions().get(j).getQuestions().size(); l++) {
                                    removeQuestionViewForDefaultDropDown(question.getOptions().get(w).getCategories().get(k).getQuestionsList().get(h).getOptions().get(j).getQuestions().get(l), localRecordID);
                                    removeCategoryViewForDefaultDropDown(question.getOptions().get(w).getCategories().get(k).getQuestionsList().get(h).getOptions().get(j).getQuestions().get(l), localRecordID);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //remove category views from main container
    public void removeCategoryViewFromUI(Options options, int localRecordID) {

        try {
            for (int k = 0; k < options.getCategories().size(); k++) {

                View myView = findViewById(options.getQuestions().get(k).getId() + IntentConstants.VIEW_CONSTANT_NESTED_CONTAINERS + localRecordID);
                ViewGroup parent = (ViewGroup) myView.getParent();
                parent.removeView(myView);
                for (int h = 0; h < options.getCategories().get(k).getQuestionsList().size(); h++) {
                    View myView2 = findViewById(options.getCategories().get(k).getQuestionsList().get(h).getId());
                    ViewGroup parent2 = (ViewGroup) myView2.getParent();
                    parent2.removeView(myView2);
                    if (options.getCategories().get(k).getQuestionsList().get(h).getOptions().size() > 0) {
                        for (int j = 0; j < options.getCategories().get(k).getQuestionsList().get(h).getOptions().size(); j++) {
                            removeQuestionViewFromUI(options.getCategories().get(k).getQuestionsList().get(h).getOptions().get(j), localRecordID);
                            removeCategoryViewFromUI(options.getCategories().get(k).getQuestionsList().get(h).getOptions().get(j), localRecordID);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void addAnswerNextClick(Questions questions, int localRecordID) {

        Long tsLong = System.currentTimeMillis() / 1000;
        Answers localAnswer;

        try {
            if (questions.getType().equals(CommonUtil.QUESTION_TYPE_SINGLE_LINE_QUESTION)) {
                String localAns = ((EditText) findViewById(questions.getId() + IntentConstants.VIEW_CONSTANT + localRecordID)).getText().toString().trim();
                if (localRecordID != 0) {
                    localAnswer = new Answers(localRecordID, currentResponseId, questions.getId(), localAns, tsLong, "multirecord");
                } else {
                    localAnswer = new Answers(localRecordID, currentResponseId, questions.getId(), localAns, tsLong);
                }
                if (!dbAdapter.doesAnswerExist(questions.getId(), currentResponseId, localRecordID)) {
                    dbAdapter.insertDataAnswersTable(localAnswer);
                }
            } else if (questions.getType().equals(CommonUtil.QUESTION_TYPE_MULTI_LINE_QUESTION)) {
                String localAns = ((EditText) findViewById(questions.getId() + IntentConstants.VIEW_CONSTANT + localRecordID)).getText().toString().trim();
                localAnswer = new Answers(localRecordID, currentResponseId, questions.getId(), localAns, tsLong);

                if (!dbAdapter.doesAnswerExist(questions.getId(), currentResponseId, localRecordID)) {
                    dbAdapter.insertDataAnswersTable(localAnswer);
                }
            } else if (questions.getType().equals(CommonUtil.QUESTION_TYPE_NUMERIC_QUESTION)) {
                String localAns = ((EditText) findViewById(questions.getId() + IntentConstants.VIEW_CONSTANT + localRecordID)).getText().toString().trim();
                localAnswer = new Answers(localRecordID, currentResponseId, questions.getId(), localAns, tsLong);

                if (!dbAdapter.doesAnswerExist(questions.getId(), currentResponseId, localRecordID)) {
                    dbAdapter.insertDataAnswersTable(localAnswer);
                }
            } else if (questions.getType().equals(CommonUtil.QUESTION_TYPE_DATE_QUESTION)) {

                localAnswer = new Answers(localRecordID, currentResponseId, questions.getId(), dateText.getText().toString(), tsLong);

                if (!dbAdapter.doesAnswerExist(questions.getId(), currentResponseId, localRecordID))
                    dbAdapter.insertDataAnswersTable(localAnswer);

            } else if (questions.getType().equals(CommonUtil.QUESTION_TYPE_RATING_QUESTION)) {
                RatingBar localRatingBar = (RatingBar) findViewById(questions.getId() + IntentConstants.VIEW_CONSTANT + localRecordID);
                localAnswer = new Answers(localRecordID, currentResponseId, questions.getId(), String.valueOf(localRatingBar.getRating()), tsLong);
                if (!dbAdapter.doesAnswerExist(questions.getId(), currentResponseId, localRecordID))
                    dbAdapter.insertDataAnswersTable(localAnswer);

            } else if (questions.getType().equals(CommonUtil.QUESTION_TYPE_PHOTO_QUESTION)) {

                localAnswer = new Answers(localRecordID, currentResponseId, questions.getId(), "", tsLong, CommonUtil.QUESTION_TYPE_PHOTO_QUESTION, fname);


                if (!dbAdapter.doesAnswerExist(questions.getId(), currentResponseId, localRecordID)) {
                    dbAdapter.insertDataAnswersTable(localAnswer);
                }
                fname = "";

            } else if (questions.getType().equals(CommonUtil.QUESTION_TYPE_RADIO_QUESTION)) {
                localAnswer = new Answers(localRecordID, currentResponseId, questions.getId(), "", tsLong, CommonUtil.QUESTION_TYPE_RADIO_QUESTION);
                if (!dbAdapter.doesAnswerExist(questions.getId(), currentResponseId, localRecordID)) {
                    dbAdapter.insertDataAnswersTable(localAnswer);
                }
            } else if (questions.getType().equals(CommonUtil.QUESTION_TYPE_DROPDOWN_QUESTION)) {
                localAnswer = new Answers(localRecordID, currentResponseId, questions.getId(), "", tsLong, CommonUtil.QUESTION_TYPE_DROPDOWN_QUESTION);
                if (!dbAdapter.doesAnswerExist(questions.getId(), currentResponseId, localRecordID)) {
                    dbAdapter.insertDataAnswersTable(localAnswer);
                }
            } else if (questions.getType().equals(CommonUtil.QUESTION_TYPE_MULTI_CHOICE_QUESTION)) {
                localAnswer = new Answers(localRecordID, currentResponseId, questions.getId(), "", tsLong, CommonUtil.QUESTION_TYPE_MULTI_CHOICE_QUESTION);
                if (!dbAdapter.doesAnswerExist(questions.getId(), currentResponseId, localRecordID)) {
                    dbAdapter.insertDataAnswersTable(localAnswer);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //this will check if the answer is saved and can be retrieved from the tables
    public void checkForAnswer(Questions qu, int responseId, int localRecordId) {

        if ((qu.getType().equals(CommonUtil.QUESTION_TYPE_SINGLE_LINE_QUESTION)) || (qu.getType().equals(CommonUtil.QUESTION_TYPE_MULTI_LINE_QUESTION)) || (qu.getType().equals(CommonUtil.QUESTION_TYPE_NUMERIC_QUESTION))) {
            answer = (EditText) findViewById(qu.getId() + IntentConstants.VIEW_CONSTANT + localRecordId);
            answer.setText(dbAdapter.getAnswer(responseId, qu.getId(), localRecordId));
        } else if (qu.getType().equals(CommonUtil.QUESTION_TYPE_DATE_QUESTION)) {
            dateText = (TextView) findViewById(qu.getId() + IntentConstants.VIEW_CONSTANT + localRecordId);
            dateText.setText(dbAdapter.getAnswer(responseId, qu.getId(), localRecordId));
        } else if (qu.getType().equals(CommonUtil.QUESTION_TYPE_RATING_QUESTION)) {

            ratingBar = (RatingBar) findViewById(qu.getId() + IntentConstants.VIEW_CONSTANT + localRecordId);
            dbAdapter.getAnswer(responseId, qu.getId(), localRecordId);
            try {
                float f = Float.parseFloat((dbAdapter.getAnswer(responseId, qu.getId(), localRecordId)));
                ratingBar.setRating(f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (qu.getType().equals(CommonUtil.QUESTION_TYPE_PHOTO_QUESTION)) {
            String localfname = dbAdapter.getImage(responseId, qu.getId(), localRecordId);
            if (!localfname.equals("")) {
                Bitmap b = loadImageFromStorage(Environment.getExternalStorageDirectory().toString() + "/saved_images", localfname);
                findViewById(qu.getId() + IntentConstants.VIEW_CONSTANT_FOR_PHOTO3 + localRecordId).setVisibility(View.VISIBLE);
                findViewById(qu.getId() + IntentConstants.VIEW_CONSTANT_FOR_PHOTO4 + localRecordId).setVisibility(View.GONE);
                ((ImageView) findViewById(qu.getId() + IntentConstants.VIEW_CONSTANT_FOR_PHOTO2 + localRecordId)).setImageBitmap(b);
            }
        } else if (qu.getType().equals(CommonUtil.QUESTION_TYPE_MULTI_CHOICE_QUESTION)) {


            List<Options> options = qu.getOptions();
            for (int i = 0; i < options.size(); i++) {
                options.get(i).getId();
            }
            List<Integer> integers = dbAdapter.getIdFromAnswerTable(responseId, qu.getId(), localRecordId);
            List<Integer> list2 =
                    dbAdapter.getOptionIds(integers, localRecordId);


            List<Integer> choiceTableIds = dbAdapter.getIdFromChoicesTable(integers, localRecordId);


            for (int i = 0; i < choiceTableIds.size(); i++) {
                int optionId = dbAdapter.getOptionIdFromPrimaryId(choiceTableIds.get(i));
                optionId = optionId + localRecordId;

                CheckBox checkBox = (CheckBox) findViewById(optionId);

                checkBox.setChecked(true);
                for (int k = 0; k < options.size(); k++) {
                    if (options.get(k).getId() == list2.get(i)) {
                        Options options1 = options.get(k);
                        if (options1.getQuestions().size() > 0) {
                            for (int l = 0; l < options1.getQuestions().size(); l++) {
                                buildLayout(options1.getQuestions().get(l), false, localRecordId, defaultParentIndex);
                            }
                        }
                    }
                }
            }

        } else if (qu.getType().equals(CommonUtil.QUESTION_TYPE_RADIO_QUESTION)) {

            List<Options> options = qu.getOptions();
            for (int i = 0; i < options.size(); i++) {
                options.get(i).getId();
            }
            List<Integer> integers = dbAdapter.getIdFromAnswerTable(responseId, qu.getId(), localRecordId);
            List<Integer> list2 =
                    dbAdapter.getOptionIds(integers, localRecordId);


            List<Integer> choiceTableIds = dbAdapter.getIdFromChoicesTable(integers, localRecordId);


            for (int i = 0; i < choiceTableIds.size(); i++) {

                try {
                    int optionId = dbAdapter.getOptionIdFromPrimaryId(choiceTableIds.get(i));
                    optionId = optionId + localRecordId;
                    RadioButton radioButton = (RadioButton) findViewById(optionId);
                    radioButton.setChecked(true);


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } else if (qu.getType().equals(CommonUtil.QUESTION_TYPE_DROPDOWN_QUESTION)) {
            List<Options> options = qu.getOptions();
            for (int i = 0; i < options.size(); i++) {
                options.get(i).getId();
            }

            List<Integer> integers = dbAdapter.getIdFromAnswerTable(responseId, qu.getId(), localRecordId);


            List<Integer> list2 =
                    dbAdapter.getOptionIds(integers, localRecordId);

            try {
                for (int i = 0; i < list2.size(); i++) {

                    for (int j = 0; j < qu.getOptions().size(); j++) {
                        if (qu.getOptions().get(j).getId() == list2.get(i)) {
                            spinner = (Spinner) findViewById(qu.getId() + IntentConstants.VIEW_CONSTANT + localRecordId);
                            spinner.setSelection(j + 1);

                        }

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    //add record to database in case of selected element
    public void addOptionToDataBase(Options options, Questions qu, int localRecordID) {

        if (options == null) {

            Choices choices1 = new Choices();
            int answerId = dbAdapter.getIdFromAnswerTable(currentResponseId, qu.getId(), localRecordID).get(0);
            choices1.setAnswerId(answerId);
//            choices1.setOptionId(0);
            choices1.setOption("");
            choices1.setType(qu.getType());
            dbAdapter.insertDataChoicesTable(choices1);
        } else {
            Choices choices = new Choices();
            int answerId = dbAdapter.getIdFromAnswerTable(currentResponseId, qu.getId(), localRecordID).get(0);
            choices.setAnswerId(answerId);
            choices.setOptionId(options.getId());
            choices.setOption(options.getContent());
            choices.setRecordId(localRecordID);
            choices.setType(qu.getType());
            dbAdapter.insertDataChoicesTable(choices);
        }
    }

    //this is remove option from table in case of multi choice
    public void removeOptionFromDataBase(Options options, Questions qu, int localRecordID) {

        for (int k = 0; k < options.getQuestions().size(); k++) {
            ArrayList<MasterQuestion> localMasterQuestion = getQuestionByRecordIDAndQuestionID(new MasterQuestion(options.getQuestions().get(k), localRecordID));
            for (int j = 0; j < localMasterQuestion.size(); j++) {
                dbAdapter.deleteFromAnswerTable(localMasterQuestion.get(j).getQuestions().getId(), currentResponseId, localRecordID);
                masterQuestionList.remove(localMasterQuestion.get(j));
            }
        }


        dbAdapter.deleteOption(options, localRecordID);

    }

    public void removeOthersFromDataBaseForDefaultDropDown(Questions qu, int localRecordID) {

        //this removes the answers entry from answers table as well as choices in choices table of rest of the options
        for (int i = 0; i < qu.getOptions().size(); i++) {

            dbAdapter.deleteFromChoicesTableWhereOptionId(qu.getOptions().get(i).getId(), localRecordID);
            if (qu.getOptions().get(i).getQuestions().size() > 0) {

                ArrayList<MasterQuestion> localMasterQuestion = null;
                try {
                    localMasterQuestion = getQuestionByRecordIDAndQuestionID(new MasterQuestion(qu.getOptions().get(i).getQuestions().get(i), localRecordID));
                } catch (Exception e) {

                }
                if (localMasterQuestion != null) {
                    Log.e("TAG", "DELETE->> IF MASTER" + localMasterQuestion.size());
                    for (int z = 0; z < localMasterQuestion.size(); z++) {
                        Log.e("TAG", "DELETE->> IF MASTER QID" + localMasterQuestion.get(z).getQuestions().getId());
                        masterQuestionList.remove(localMasterQuestion.get(z));
                        dbAdapter.deleteFromAnswerTable(localMasterQuestion.get(z).getQuestions().getId(), currentResponseId, localRecordID);
                    }
                }
            }


            //this removes all the category questions stored in answers table
            if (qu.getOptions().get(i).getCategories().size() > 0) {
                for (int k = 0; k < qu.getOptions().get(i).getCategories().size(); k++) {

                    for (int l = 0; l < qu.getOptions().get(i).getCategories().get(k).getQuestionsList().size(); l++) {
                        ArrayList<MasterQuestion> localMasterQuestion = getQuestionByRecordIDAndQuestionID(new MasterQuestion(qu.getOptions().get(i).getCategories().get(k).getQuestionsList().get(l), localRecordID));
                        for (int z = 0; z < localMasterQuestion.size(); z++) {
                            masterQuestionList.remove(localMasterQuestion.get(z));
                            dbAdapter.deleteFromAnswerTable(localMasterQuestion.get(z).getQuestions().getId(), currentResponseId, localRecordID);
                        }
                    }
                }
            }

        }
    }

    //this is for not selected elements of radio
    public void removeOthersFromDataBase(Options options, Questions qu, int localRecordID) {

        //this removes the answers entry from answers table as well as choices in choices table of rest of the options
        for (int i = 0; i < qu.getOptions().size(); i++) {
            if (options.getId() != qu.getOptions().get(i).getId()) {

                dbAdapter.deleteFromChoicesTableWhereOptionId(qu.getOptions().get(i).getId(), localRecordID);
                if (qu.getOptions().get(i).getQuestions().size() > 0) {

                    ArrayList<MasterQuestion> localMasterQuestion = null;
                    try {
                        localMasterQuestion = getQuestionByRecordIDAndQuestionID(new MasterQuestion(qu.getOptions().get(i).getQuestions().get(i), localRecordID));
                    } catch (Exception e) {

                    }
                    if (localMasterQuestion != null) {
                        Log.e("TAG", "DELETE->> IF MASTER" + localMasterQuestion.size());
                        for (int z = 0; z < localMasterQuestion.size(); z++) {
                            Log.e("TAG", "DELETE->> IF MASTER QID" + localMasterQuestion.get(z).getQuestions().getId());
                            masterQuestionList.remove(localMasterQuestion.get(z));
                            dbAdapter.deleteFromAnswerTable(localMasterQuestion.get(z).getQuestions().getId(), currentResponseId, localRecordID);
                        }
                    }
//                    for (int j = 0; j < qu.getOptions().get(i).getQuestions().size(); j++) {
//                        dbAdapter.deleteFromAnswerTable(qu.getOptions().get(i).getQuestions().get(j).getId(), currentResponseId, localRecordID);
//
//                    }
                }


                //this removes all the category questions stored in answers table
                if (qu.getOptions().get(i).getCategories().size() > 0) {
                    for (int k = 0; k < qu.getOptions().get(i).getCategories().size(); k++) {

                        for (int l = 0; l < qu.getOptions().get(i).getCategories().get(k).getQuestionsList().size(); l++) {
                            ArrayList<MasterQuestion> localMasterQuestion = getQuestionByRecordIDAndQuestionID(new MasterQuestion(qu.getOptions().get(i).getCategories().get(k).getQuestionsList().get(l), localRecordID));
                            for (int z = 0; z < localMasterQuestion.size(); z++) {
                                masterQuestionList.remove(localMasterQuestion.get(z));
                                dbAdapter.deleteFromAnswerTable(localMasterQuestion.get(z).getQuestions().getId(), currentResponseId, localRecordID);
                            }
//                            dbAdapter.deleteFromAnswerTable(qu.getOptions().get(i).getCategories().get(k).getQuestionsList().get(l).getId(), currentResponseId, localRecordID);
                        }
                    }
                }
            }
        }
    }


    //function to check if the question is mandatory or not
    //this function will also check all the questions on the page if any question is mandatory or not


    public boolean checkMandatoryQuestion(String type, int recordId, int qId) {

        //this will check for answers for questions having answers in answers table
        if ((type.equals(CommonUtil.QUESTION_TYPE_SINGLE_LINE_QUESTION)) || (type.equals(CommonUtil.QUESTION_TYPE_MULTI_LINE_QUESTION) || (type.equals(CommonUtil.QUESTION_TYPE_NUMERIC_QUESTION)))) {


            if (((EditText) findViewById(qId + IntentConstants.VIEW_CONSTANT + recordId)).getText().toString().trim().equals("")) {
                return false;
            } else
                return true;
        }
        if ((type.equals(CommonUtil.QUESTION_TYPE_DATE_QUESTION))) {

            if (dbAdapter.doesAnswerExistAsNonNull(qId, currentResponseId, recordId).equals("")) {
                return false;
            } else
                return true;
        }
        if ((type.equals(CommonUtil.QUESTION_TYPE_RATING_QUESTION))) {
            if (dbAdapter.doesAnswerExistAsNonNull(qId, currentResponseId, recordId).equals("0.0")) {
                return false;

            } else return true;
        }


        //this wil check questions having answers in choices tables have answers or not
        if (type.equals(CommonUtil.QUESTION_TYPE_RADIO_QUESTION) || type.equals(CommonUtil.QUESTION_TYPE_MULTI_CHOICE_QUESTION)) {
            int ansID = dbAdapter.getAnswerId(currentResponseId, qId, recordId);
            if (ansID == 0) {
                return false;
            } else {
                if (dbAdapter.getChoicesCountWhereAnswerIdIs(ansID, recordId) == 0) {
                    return false;
                } else {
                    return true;
                }
            }

        }

        if (type.equals(CommonUtil.QUESTION_TYPE_DROPDOWN_QUESTION)) {
            int ansID = dbAdapter.getAnswerId(currentResponseId, qId, recordId);
            if (ansID == 0) {
                return false;
            } else {
                if (dbAdapter.getDropDownChoicesCountWhereAnswerIdIs(ansID) == 0) {
                    return false;
                } else {
                    return true;
                }
            }

        }

        // this wil check if photo mandatory questions have answers in te answers table or not
        if (type.equals(CommonUtil.QUESTION_TYPE_PHOTO_QUESTION)) {

            if (dbAdapter.doesImageExistAsNonNull(qId, currentResponseId, recordId).equals("")) {
                return false;
            } else return true;

        }

        return true;
    }


    private void takePicture(Questions ques, int parentRecordId) {

        tagMasterQuestion = new MasterQuestion(ques, parentRecordId);
        Intent cameraIntent = new Intent(
                android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    //mandatory question dialog
    public void showMandatoryDialog(final EditText viewById) {
        final Dialog dialog = new Dialog(NewResponseActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //before
        dialog.setContentView(R.layout.mandatory_question_dialog);
        dialog.show();
        Button button = (Button) dialog.findViewById(R.id.okay);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewById.setFocusable(true);
                viewById.requestFocus();
                dialog.dismiss();
            }
        });

    }

    private void saveAnswerIntoDB(TagModel localTagModel, Answers localAnswer) {
        if (!dbAdapter.doesAnswerExist(localTagModel.getqID(), currentResponseId, localTagModel.getRecordID())) {
            dbAdapter.insertDataAnswersTable(localAnswer);
        } else {
            dbAdapter.deleteFromAnswerTableWithRecordId(localTagModel.getqID(), currentResponseId, localTagModel.getRecordID());
            dbAdapter.insertDataAnswersTable(localAnswer);
        }
    }

    //this is a class which implements date picker dialog and shows up on item click of date questuions
    class mDateSetListener implements DatePickerDialog.OnDateSetListener {
        int localRecordID;
        Questions localQuestions;

        mDateSetListener(int localRecordID, Questions localQuestions) {
            this.localRecordID = localRecordID;
            this.localQuestions = localQuestions;
        }

        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            int mYear = year;
            int mMonth = monthOfYear;
            int mDay = dayOfMonth;
            dateText = (TextView) findViewById(localQuestions.getId() + IntentConstants.VIEW_CONSTANT + localRecordID);
            dateText.setText(new StringBuilder().append(mYear).append("/").append(mMonth + 1).append("/").append(mDay).toString());

            Long tsLong = System.currentTimeMillis() / 1000;
            Answers localAnswer = new Answers(localRecordID, currentResponseId, localQuestions.getId(), dateText.getText().toString(), tsLong);
            saveAnswerIntoDB(new TagModel(localQuestions.getId(), localRecordID), localAnswer);

        }

    }

}