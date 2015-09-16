package com.lumstic.ashoka.ui;

import android.app.ActionBar;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
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
import android.widget.CompoundButton;
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

import com.lumstic.ashoka.R;
import com.lumstic.ashoka.adapters.DBAdapter;
import com.lumstic.ashoka.adapters.DropDownAdapter;
import com.lumstic.ashoka.models.Answers;
import com.lumstic.ashoka.models.Categories;
import com.lumstic.ashoka.models.Choices;
import com.lumstic.ashoka.models.DropDown;
import com.lumstic.ashoka.models.MasterQuestion;
import com.lumstic.ashoka.models.MasterSurveyQuestionModel;
import com.lumstic.ashoka.models.Options;
import com.lumstic.ashoka.models.Questions;
import com.lumstic.ashoka.models.Records;
import com.lumstic.ashoka.models.Surveys;
import com.lumstic.ashoka.models.TagModel;
import com.lumstic.ashoka.utils.CommonUtil;
import com.lumstic.ashoka.utils.IntentConstants;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;


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
    private int subCategoryQuestionCounter;
    private int categoriesCounter = 0;
    private int categoryTypeCounter = 0;
    private String categoryTitleString = "";


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

    private void addNestedQuestionsToQuestionList(List<Questions> localQuestionsList, List<Categories> localCategoriesList) {
        if (surveys.getQuestions().size() > 0) {
            for (int j = 0; j < surveys.getQuestions().size(); j++) {
                for (int k = 0; k < localQuestionsList.size(); k++) {
                    for (int l = 0; l < localQuestionsList.get(k).getOptions().size(); l++) {
                        if (surveys.getQuestions().get(j).getParentId() == localQuestionsList.get(k).getOptions().get(l).getId()) {


                            if (!localQuestionsList.get(k).getOptions().get(l).getQuestions().contains(surveys.getQuestions().get(j)))
                                localQuestionsList.get(k).getOptions().get(l).getQuestions().add(surveys.getQuestions().get(j));

                            addNestedQuestionsToQuestionList(localQuestionsList.get(k).getOptions().get(l).getQuestions(), localCategoriesList);

                        } else {
                            if (localCategoriesList.size() > 0) {
                                for (int m = 0; m < localCategoriesList.size(); m++) {

                                    if (localCategoriesList.get(m).getParentId() == localQuestionsList.get(k).getOptions().get(l).getId()) {

                                        if (!localQuestionsList.get(k).getOptions().get(l).getCategories().contains(localCategoriesList.get(m)))
                                            localQuestionsList.get(k).getOptions().get(l).getCategories().add(localCategoriesList.get(m));

                                        addNestedQuestionsToQuestionList(localQuestionsList.get(k).getOptions().get(l).getQuestions(), localCategoriesList);


                                    }

                                }
                            }

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

    private void addCategoryQuestionsTOCategory(List<Questions> localNonRootedQuestionsList, List<Categories> localCategoriesList) {
        if (surveys.getCategories().size() > 0) {

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
    }

    private void addQuestionsToMasterList(List<Questions> localQuestionsList, List<Categories> localCategoriesList) {
        for (int i = 0; i < localQuestionsList.size(); i++) {
            surveyQuestionsList.add(new MasterSurveyQuestionModel(localQuestionsList.get(i), CommonUtil.TYPE_QUESTION));
        }
        for (int i = 0; i < localCategoriesList.size(); i++) {
            if (localCategoriesList.get(i).getCategoryId() == 0 && localCategoriesList.get(i).getParentId() == 0) {
                surveyQuestionsList.add(new MasterSurveyQuestionModel(localCategoriesList.get(i), CommonUtil.TYPE_CATEGORY));
            }
        }
    }

    private List<Categories> addCategoriesToCategoryList() {
        List<Categories> localCategoriesList = new ArrayList<>();
        if (surveys.getCategories().size() > 0) {
            localCategoriesList = surveys.getCategories();
        }
        return localCategoriesList;
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

        categoriesList = addCategoriesToCategoryList();


        //Root Level Questions
        addNestedQuestionsToQuestionList(questionsList, categoriesList);
        //Non Root Level Questions
        addNestedQuestionsToQuestionList(nonRootedQuestionsList, categoriesList);

        addCategoryQuestionsTOCategory(nonRootedQuestionsList, categoriesList);


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


    public void onMarkComplete() {

        if (storeDataToDB() == 0) {
            return;
        }
        addRecordCounter = 0;
        recordId = 0;
        categoriesCounter = 0;
        subCategoryQuestionCounter = 0;


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

            Questions cq = (Questions) sortedSurveyQuestionsList.get(questionCounter).getObject();
            buildLayout(cq, CommonUtil.isParentView, recordId, defaultParentIndex);
        } else {
            Categories currentCategory = (Categories) sortedSurveyQuestionsList.get(questionCounter).getObject();
            //for multi record questions
            if (currentCategory.getType().equals(CommonUtil.CATEGORY_TYPE_MULTI_RECORD)) {

                NextBackCommonCode(currentCategory);

            } else {
                TagModel tmpTagModel = new TagModel(0, recordId);
                setCategoryTitle(currentCategory, tmpTagModel, CommonUtil.isParentView);
                buildCategoryLayout(currentCategory, getCategoryTitleString(), recordId,
                        CommonUtil.isParentView, defaultParentIndex);
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
        String categoryTitle = getCategoryTitleString();
        if (categoryQuestionCounter > 0) {

            questionTextSingleLine.setTextSize(20);
            if (ques.getMandatory() == 1) {
                if (subCategoryQuestionCounter > 0 && categoriesCounter > 0) {
                    questionTextSingleLine.setText(categoryTitle + "." + categoriesCounter + "." + subCategoryQuestionCounter + "  " + ques.getContent() + " *");
                } else if (categoryQuestionCounter > 0) {
                    questionTextSingleLine.setText(categoryTitle + "." + categoryQuestionCounter + "  " + ques.getContent() + " *");
                } else if (categoryTypeCounter > 0) {
                    questionTextSingleLine.setText(categoryTitle + "." + categoryTypeCounter + ques.getContent() + " *");
                } else if (!categoryTitle.equals("")) {
                    questionTextSingleLine.setText(categoryTitle + " " + ques.getContent() + " *");
                } else {
                    questionTextSingleLine.setText(categoryTitle + "  " + ques.getContent() + " *");
                }
                if (ques.getParentId() > 0)
                    questionTextSingleLine.setText("Q." + "  " + ques.getContent() + " *");
            } else {
                if (subCategoryQuestionCounter > 0 && categoriesCounter > 0) {
                    questionTextSingleLine.setText(categoryTitle + "." + categoriesCounter + "." + subCategoryQuestionCounter + "  " + ques.getContent());
                } else if (categoryQuestionCounter > 0) {
                    questionTextSingleLine.setText(categoryTitle + "." + categoryQuestionCounter + "  " + ques.getContent());
                } else if (!categoryTitle.equals("")) {
                    questionTextSingleLine.setText(categoryTitle + " " + ques.getContent());
                } else {
                    questionTextSingleLine.setText(categoryTitle + "  " + ques.getContent());
                }
                if (ques.getParentId() > 0)
                    questionTextSingleLine.setText("Q." + "  " + ques.getContent());

            }
        } else {
            if (ques.getMandatory() == 1) {
                if (subCategoryQuestionCounter > 0 && categoriesCounter > 0) {
                    questionTextSingleLine.setText(categoryTitle + "." + categoriesCounter + "." + subCategoryQuestionCounter + "  " + ques.getContent() + " *");
                } else if (categoryQuestionCounter > 0) {
                    questionTextSingleLine.setText(categoryTitle + "." + categoryQuestionCounter + "  " + ques.getContent() + " *");
                } else if (categoryTypeCounter > 0) {
                    questionTextSingleLine.setText(categoryTitle + "." + categoryTypeCounter + ques.getContent() + " *");
                } else if (!categoryTitle.equals("")) {
                    questionTextSingleLine.setText(categoryTitle + " " + ques.getContent() + " *");
                } else {
                    questionTextSingleLine.setText(categoryTitle + "  " + ques.getContent() + " *");
                }
                if (ques.getParentId() > 0)
                    questionTextSingleLine.setText(categoryTitle + "." + order + "  " + ques.getContent() + " *");
            } else {

                if (subCategoryQuestionCounter > 0 && categoriesCounter > 0) {
                    questionTextSingleLine.setText(categoryTitle + "." + categoriesCounter + "." + subCategoryQuestionCounter + "  " + ques.getContent());
                } else if (categoryQuestionCounter > 0) {
                    questionTextSingleLine.setText(categoryTitle + "." + categoryQuestionCounter + "  " + ques.getContent());
                } else if (!categoryTitle.equals("")) {
                    questionTextSingleLine.setText(categoryTitle + " " + ques.getContent());
                } else {
                    questionTextSingleLine.setText(categoryTitle + "  " + ques.getContent());
                }
                if (ques.getParentId() > 0)
                    questionTextSingleLine.setText(categoryTitle + "." + order + "  " + ques.getContent());

            }
        }
        return questionTextSingleLine;

    }

    //hide keypad on next click and various events
    public void hideKeypad(EditText answer) {
        InputMethodManager imm = (InputMethodManager) getSystemService(
                INPUT_METHOD_SERVICE);
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
                        TagModel localTagModel = (TagModel) ((View) view.getParent()).getTag(R.string.multirecord_tag);
                        Answers localAnswer = new Answers(localTagModel.getRecordID(),
                                currentResponseId, localTagModel.getqID(), answer.getText()
                                .toString(), CommonUtil.getCurrentTimeStamp());
                        saveAnswerIntoDB(localTagModel, localAnswer);

                    }
                }
            });


        } else if (ques.getType().contains(CommonUtil.QUESTION_TYPE_MULTI_LINE_QUESTION)) {
            LinearLayout nestedContainer = createNestedContainer();
            TextView questionTextSingleLine = createQuestionTitle(ques, isChild, parentRecordId);
            nestedContainer.addView(questionTextSingleLine);

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
                        TagModel localTagModel = (TagModel) ((View) view.getParent()).getTag(R.string.multirecord_tag);
                        Answers localAnswer = new Answers(localTagModel.getRecordID(),
                                currentResponseId, localTagModel.getqID(), answer.getText()
                                .toString(), CommonUtil.getCurrentTimeStamp());
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

                    if (i == 0) {
                        removeOthersFromDataBaseForDefaultDropDown(ques, ((TagModel) nestedContainer.getTag(R.string.multirecord_tag)).getRecordID());
                        removeQuestionViewForDefaultDropDown(ques, ((TagModel) nestedContainer.getTag(R.string.multirecord_tag)).getRecordID());

                    }

                    if (i != 0) {


                        spinner.setTag(R.string.app_name, view.getTag());

                        String tempOrder = "";
                        int x = (i - 1) + 97;
                        String character = Character.toString((char) x);


                        if (ques.getParentId() == 0) {
                            order = "";
                            order = tempOrder + character + ".";
                        } else {
                            if (spinner.getTag(R.string.app_name) != null)
                                tempOrder = spinner.getTag(R.string.app_name).toString();
                            order = "";
                            order = tempOrder + ".";
                        }
                        order = tempOrder + character + ".";


                        //add option selected to database table choices table
                        Options options = ques.getOptions().get(i - 1);
                        addOptionToDataBase(options, ques, ((TagModel) nestedContainer.getTag(R.string.multirecord_tag)).getRecordID());
                        removeOthersFromDataBase(options, ques, ((TagModel) nestedContainer.getTag(R.string.multirecord_tag)).getRecordID());


                        //create nested questions
                        buildOptionLayout(options.getQuestions(), options.getCategories(), ques,
                                ((TagModel) nestedContainer.getTag(R.string.multirecord_tag))
                                        .getRecordID(), CommonUtil.isChildView, fieldContainer
                                        .indexOfChild(nestedContainer) + 1);


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
            if (isChild) {
                nestedContainer.setId(ques.getId() + IntentConstants.VIEW_CONSTANT_NESTED_CONTAINERS + parentRecordId);
            } else {
                nestedContainer.setId(ques.getId() + IntentConstants.VIEW_CONSTANT_NESTED_CONTAINERS + recordId);
            }

            if (isChild) {
                nestedContainer.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), parentRecordId, ques));
                fieldContainer.addView(nestedContainer, parentViewPosition);
                masterQuestionList.add(new MasterQuestion(ques, parentRecordId));
                addAnswerNextClick(ques, parentRecordId);
            } else {
                nestedContainer.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), recordId, ques));
                fieldContainer.addView(nestedContainer);
                masterQuestionList.add(new MasterQuestion(ques, recordId));
                addAnswerNextClick(ques, recordId);
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

                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                        if (isChecked) {

                            CheckBox checkBox1 = (CheckBox) compoundButton;
                            Options options = (Options) checkBox1.getTag();
                            String tempOrder = "";
                            int x = options.getOrderNumber() + 97;
                            String character = Character.toString((char) x);


                            ll.setTag(findViewById(compoundButton.getId()).getTag(R.string.app_name));

                            if (ques.getParentId() == 0) {
                                order = "";
                                order = tempOrder + character + ".";
                            } else {
                                if (checkBox.getTag(R.string.app_name) != null)
                                    tempOrder = findViewById(compoundButton.getId()).getTag(R.string.app_name).toString();
                                order = "";
                                order = tempOrder + ".";
                            }


                            order = tempOrder + character + ".";
                            addOptionToDataBase(options, ques, ((TagModel) nestedContainer.getTag(R.string.multirecord_tag)).getRecordID());

                            buildOptionLayout(options.getQuestions(), options.getCategories(),
                                    ques, ((TagModel) nestedContainer.getTag(R.string
                                            .multirecord_tag)).getRecordID(), CommonUtil
                                            .isChildView, fieldContainer.indexOfChild
                                            (nestedContainer) + 1);
                        } else {
                            //if check box is unchecked, we have to handle questions and categories of options not selected
                            CheckBox checkBox1 = (CheckBox) compoundButton;
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
                        TagModel localTagModel = (TagModel) ((View) view.getParent()).getTag(R.string.multirecord_tag);
                        Answers localAnswer = new Answers(localTagModel.getRecordID(),
                                currentResponseId, localTagModel.getqID(), answer.getText()
                                .toString(), CommonUtil.getCurrentTimeStamp());
                        saveAnswerIntoDB(localTagModel, localAnswer);
                    }
                }
            });


        } else if (ques.getType().contains(CommonUtil.QUESTION_TYPE_DATE_QUESTION)) {
            final LinearLayout nestedContainer = createNestedContainer();
            TextView questionTextSingleLine = createQuestionTitle(ques, isChild, parentRecordId);
            nestedContainer.addView(questionTextSingleLine);
            nestedContainer.addView(inflater.inflate(R.layout.answer_date_picker, null));
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
            if (isChild) {
                nestedContainer.setId(ques.getId() + IntentConstants.VIEW_CONSTANT_NESTED_CONTAINERS + parentRecordId);
            } else {
                nestedContainer.setId(ques.getId() + IntentConstants.VIEW_CONSTANT_NESTED_CONTAINERS + recordId);
            }
            nestedContainer.setTag(ques);


            if (isChild) {
                nestedContainer.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), parentRecordId, ques));
                masterQuestionList.add(new MasterQuestion(ques, parentRecordId));
                addAnswerNextClick(ques, parentRecordId);
            } else {
                nestedContainer.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), recordId, ques));
                masterQuestionList.add(new MasterQuestion(ques, recordId));
                addAnswerNextClick(ques, recordId);
            }
            //create new radio group
            RadioGroup radioGroup = new RadioGroup(this);
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
                        buildOptionLayout(options.getQuestions(), options.getCategories(), ques,
                                ((TagModel) nestedContainer.getTag(R.string.multirecord_tag))
                                        .getRecordID(), CommonUtil.isChildView, fieldContainer
                                        .indexOfChild(nestedContainer) + 1);


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


                    TagModel localTagModel = (TagModel) (nestedContainer).getTag(R.string.multirecord_tag);

                    if (dbAdapter.doesAnswerExist(ques.getId(), currentResponseId)) {
                        dbAdapter.deleteRatingAnswer(localTagModel.getqID(), currentResponseId, localTagModel.getRecordID());
                    }
                    Answers localAnswer = new Answers(localTagModel.getRecordID(),
                            currentResponseId, localTagModel.getqID(), String.valueOf(v),
                            CommonUtil.getCurrentTimeStamp());
                    saveAnswerIntoDB(localTagModel, localAnswer);

                }
            });


        } else if (ques.getType().equals(CommonUtil.QUESTION_TYPE_PHOTO_QUESTION)) {

            final LinearLayout nestedContainer = createNestedContainer();
            TextView questionTextSingleLine = createQuestionTitle(ques, isChild, parentRecordId);
            nestedContainer.addView(questionTextSingleLine);
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
        // try {
        if (isChild) {
            checkForAnswer(ques, currentResponseId, parentRecordId, isChild);
        } else {
            checkForAnswer(ques, currentResponseId, recordId, isChild);
        }
        //  } catch (Exception e) {

        //  }
        ///////////////////////////////////////

        if (markAsComplete.getVisibility() == View.VISIBLE)

        {

            fieldContainer.addView(markAsComplete);
        }
    }

    private void buildOptionLayout(List<Questions> questions, List<Categories> categories,
                                   Questions ques, int localRecordID, boolean isChild, int
                                           position) {

        //   int listAllSize = questionsSize + categoriesSize;
        List<MasterSurveyQuestionModel> listAll = new ArrayList<>();
        List<MasterSurveyQuestionModel> categoriesList = new ArrayList<>();

        for (int index1 = 0; index1 < questions.size(); index1++) {
            listAll.add(new MasterSurveyQuestionModel(questions.get(index1), CommonUtil.TYPE_QUESTION));
        }

        for (int index2 = 0; index2 < categories.size(); index2++) {
            categoriesList.add(new MasterSurveyQuestionModel(categories.get(index2), CommonUtil.TYPE_CATEGORY));
        }

        if (categoriesList.size() > 0 && listAll.size() > 0) {
            for (int index2 = 0; index2 < categoriesList.size(); index2++) {
                for (int index1 = 0; ; index1++) {

                    if (index1 == categories.size() || questions.get(index1).getOrderNumber() > categories.get(index2).getOrderNumber()) {
                        listAll.add(index1, new MasterSurveyQuestionModel(categories.get(index2), CommonUtil.TYPE_CATEGORY));
                        break;
                    }
                }
            }
        } else if (listAll.size() == 0 && categoriesList.size() > 0) {
            for (int index2 = 0; index2 < categories.size(); index2++) {
                listAll.add(new MasterSurveyQuestionModel(categories.get(index2), CommonUtil.TYPE_CATEGORY));
            }
        }

        if (listAll.size() > 0) {
            String tmp = order;
            for (int k = 0; k < listAll.size(); k++) {

                if (listAll.get(k).getType() == CommonUtil.TYPE_QUESTION) {
                    order = tmp + Integer.toString(k + 1);
                    //  int c = fieldContainer.getChildCount();
                    // View v = fieldContainer.getChildAt(c - 1);
                    buildLayout((Questions) listAll.get(k).getObject(), isChild,
                            localRecordID, position + k);

                } else {
                    TagModel tmpTagModel = new TagModel(ques.getId(), localRecordID);
                    order = tmp + Integer.toString(k + 1);
                    setCategoryTitle((Categories) listAll.get(k).getObject(), tmpTagModel,
                            isChild);
                    buildCategoryLayout((Categories) listAll.get(k).getObject(),
                            getCategoryTitleString(), localRecordID, isChild, position + k);
                }
            }
        }

    }


    private void buildCategoryLayout(Categories currentCategory, String categoryTitleString, int
            localRecordId, boolean isChild, int localPosition) {
        int i;
        if (currentCategory.getQuestionsList().size() > 0) {
            for (i = 0; i < currentCategory.getQuestionsList().size(); i++) {
                setCategoryTitleString(categoryTitleString + "." + (i + 1));

                if (currentCategory.getCategories() == null) {
                    buildLayout(currentCategory.getQuestionsList().get(i), isChild,
                            localRecordId,
                            localPosition + i);
                    setCategoryTitleString(categoryTitleString);
                } else {
                    if (currentCategory.getCategories().getOrderNumber() > currentCategory.getQuestionsList().get(i).getOrderNumber()) {
                        buildLayout(currentCategory.getQuestionsList().get(i), isChild,
                                localRecordId, localPosition + i);
                        setCategoryTitleString(categoryTitleString + "." + (i + 2));

                        if (i == (currentCategory.getQuestionsList().size() - 1)) {

                            if (currentCategory.getCategories().getType().equals(CommonUtil.CATEGORY_TYPE_MULTI_RECORD)) {
                                NextBackCommonCode(currentCategory.getCategories());
                            } else {
                                order = "";
                                TagModel tmpTagModel = new TagModel(0, localRecordId);
                                setCategoryTitle(currentCategory.getCategories(), tmpTagModel, isChild);
                                buildCategoryLayout(currentCategory.getCategories(),
                                        getCategoryTitleString(), localRecordId, isChild,
                                        localPosition + i);
                                setCategoryTitleString(categoryTitleString);
                            }
                        }
                    } else {
                        if (i == 0) {
                            if (currentCategory.getCategories().getType().equals(CommonUtil.CATEGORY_TYPE_MULTI_RECORD)) {
                                NextBackCommonCode(currentCategory.getCategories());
                            } else {
                                order = "";
                                TagModel tmpTagModel = new TagModel(0, localRecordId);
                                setCategoryTitle(currentCategory.getCategories(), tmpTagModel, isChild);
                                buildCategoryLayout(currentCategory.getCategories(),
                                        getCategoryTitleString(), localRecordId, isChild,
                                        localPosition + i);
                            }
                        }

                        setCategoryTitleString(categoryTitleString + "." + (i + 2));
                        buildLayout(currentCategory.getQuestionsList().get(i), isChild,
                                localRecordId, localPosition + i);
                        setCategoryTitleString(categoryTitleString);
                    }
                }
            }
        } else {
            if (currentCategory.getCategories() != null) {
                i = 0;
                if (currentCategory.getCategories().getType().equals(CommonUtil.CATEGORY_TYPE_MULTI_RECORD)) {
                    NextBackCommonCode(currentCategory.getCategories());
                } else {
                    TagModel tmpTagModel = new TagModel(0, localRecordId);
                    setCategoryTitleString(categoryTitleString + "." + (i + 1));
                    setCategoryTitle(currentCategory.getCategories(), tmpTagModel, isChild);
                    buildCategoryLayout(currentCategory.getCategories(), getCategoryTitleString
                            (), localRecordId, isChild, localPosition + i);
                    setCategoryTitleString(categoryTitleString);
                }
            }
        }

    }

    private String getCategoryTitleString() {
        return categoryTitleString;
    }

    private void setCategoryTitleString(String s) {
        categoryTitleString = s;
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
                    INPUT_METHOD_SERVICE);
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
                    showMandatoryDialog((EditText) findViewById(masterQuestionList.get(i).getAnsAndroidID()));
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

        //  defaultParentIndex = 0;

        //set next and previous buttons
        if (questionCounter < totalQuestionCount - 1) {
            previousQuestion.setBackgroundColor(getResources().getColor(R.color.login_button_color));
            previousQuestion.setTextColor(getResources().getColor(R.color.white));
            previousQuestion.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_arrow_back, 0, 0, 0);

            fieldContainer.removeAllViews();
            questionCounter++;

            counterButton.setText(questionCounter + 1 + " out of " + totalQuestionCount);
            setCategoryTitleString("Q." + (questionCounter + 1));

            //////////////////////////////////////////////////////////////////////////////


            if (sortedSurveyQuestionsList.get(questionCounter).getType() == CommonUtil.TYPE_QUESTION) {

                Questions cq = ((Questions) sortedSurveyQuestionsList.get(questionCounter).getObject());
                buildLayout(cq, CommonUtil.isParentView, recordId, defaultParentIndex);
            } else {
                Categories currentCategory = ((Categories) sortedSurveyQuestionsList.get(questionCounter).getObject());
                //for multi record questions
                if (currentCategory.getType().equals(CommonUtil.CATEGORY_TYPE_MULTI_RECORD)) {

                    NextBackCommonCode(currentCategory);

                } else {
                    TagModel tmpTagModel = new TagModel(0, recordId);
                    setCategoryTitle(currentCategory, tmpTagModel, CommonUtil.isParentView);
                    buildCategoryLayout(currentCategory, getCategoryTitleString(), recordId,
                            CommonUtil.isParentView, defaultParentIndex);
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
        categoriesCounter = 0;
        subCategoryQuestionCounter = 0;
        setCategoryTitleString("Q." + (questionCounter + 1));
        //Added qID as 0
        TagModel tmpTagModel = new TagModel(0, localRecordID);
        createDeleteRecord(tmpTagModel, currentCategory);
        buildCategoryLayout(currentCategory, getCategoryTitleString(), localRecordID, CommonUtil
                .isParentView, defaultParentIndex);
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
                        dbAdapter.deleteFromRecordsTableWhereRecordId(localMasterQuestions.get(j)
                                .getRecordID(), currentResponseId);
                    }

                    ArrayList<View> localViews = getViewsByTag(fieldContainer, tag);

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
                    recordId = (int) dbAdapter.insertDataRecordsTable(new Records
                            (currentCategory
                                    .getId(), currentResponseId));
                } else {
                    recordId = (int) dbAdapter.insertDataRecordsTable(new Records(currentCategory.getId(), currentResponseId));
                }


                onAddRecordClick(recordId, currentCategory);


            }
        });
    }


    private void NextBackCommonCode(final Categories currentCategory) {
        int entries = 0;
        Cursor multiRecordCountCursor = dbAdapter.findNoOfEntriesFromRecordTable(currentCategory
                .getId(), currentResponseId);
        if (multiRecordCountCursor != null) {
            addRecordCounter = entries = multiRecordCountCursor.getCount();
        }

        buildMultiRecordTitle(currentCategory);

        createAddRecordButton(currentCategory);


        if (entries != 0) {
            multiRecordCountCursor.moveToFirst();
            for (int i = 0; i < entries; i++) {
                int localRecordID = multiRecordCountCursor.getInt(multiRecordCountCursor.getColumnIndex(DBAdapter.DBhelper.ID));
                onAddRecordClick(localRecordID, currentCategory);
                multiRecordCountCursor.moveToNext();
            }
        }
    }

    private void backSaveCommonCode() {
        for (int i = 0; i < masterQuestionList.size(); i++) {
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
//        defaultParentIndex = 0;

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
            setCategoryTitleString("Q." + (questionCounter + 1));
            //////////////////////////////////////////////////////////////////////////////

            if (sortedSurveyQuestionsList.get(questionCounter).getType() == CommonUtil.TYPE_QUESTION) {

                Questions cq = ((Questions) sortedSurveyQuestionsList.get(questionCounter).getObject());
                buildLayout(cq, CommonUtil.isParentView, recordId, defaultParentIndex);
            } else {
                Categories currentCategory = ((Categories) sortedSurveyQuestionsList.get(questionCounter).getObject());
                //for multi record questions
                if (currentCategory.getType().equals(CommonUtil.CATEGORY_TYPE_MULTI_RECORD)) {

                    NextBackCommonCode(currentCategory);

                } else {
                    TagModel tmpTagModel = new TagModel(0, recordId);
                    setCategoryTitle(currentCategory, tmpTagModel, CommonUtil.isParentView);
                    buildCategoryLayout(currentCategory, getCategoryTitleString(), recordId,
                            CommonUtil.isParentView, defaultParentIndex);
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
    public void setCategoryTitle(Categories categories, TagModel tmpTagModel, boolean isChild) {
        LinearLayout nestedContainer = new LinearLayout(this);
        nestedContainer.setOrientation(LinearLayout.VERTICAL);
        TextView questionTextSingleLine = new TextView(this);
        questionTextSingleLine.setTextSize(20);
        questionTextSingleLine.setTextColor(Color.BLACK);
        questionTextSingleLine.setPadding(8, 12, 8, 20);

        String setText = "";
        if (getCategoryTitleString().equals("")) {

            setText = "Q." + (questionCounter + 1) + " ";
            questionTextSingleLine.setText(setText + categories.getContent());
        } else if (categoriesCounter == 0 && !order.equals("")) {
            setText = "Q." + (questionCounter + 1) + "." + order + "  ";
            questionTextSingleLine.setText(setText + categories.getContent());
        } else if (categoriesCounter > 0) {
            setText = "Q." + (questionCounter + 1) + "." + categoriesCounter + " ";
            questionTextSingleLine.setText(setText + categories.getContent());
        } else if (getCategoryTitleString().contains((questionCounter + 1) + "")) {
            setText = getCategoryTitleString();
            questionTextSingleLine.setText(setText + " " + categories.getContent());

        } else {
            setText = getCategoryTitleString();
            questionTextSingleLine.setText(setText + categories.getContent());
        }

        setCategoryTitleString(setText.trim());

        nestedContainer.addView(questionTextSingleLine);
        if (isChild) {
            nestedContainer.setId(categories.getId() + IntentConstants.VIEW_CONSTANT_NESTED_CONTAINERS + tmpTagModel.getRecordID());
        } else {
            nestedContainer.setId(categories.getId() + IntentConstants.VIEW_CONSTANT_NESTED_CONTAINERS + recordId);
        }
        nestedContainer.setTag(categories);
        nestedContainer.setTag(R.string.multirecord_tag, tmpTagModel);
        nestedContainer.setBackgroundColor(Color.WHITE);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 10, 0, 10);
        fieldContainer.addView(nestedContainer, layoutParams);
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
            if (question.getOptions().size() > 0) {
                for (int w = 0; w < question.getOptions().size(); w++) {

                    removeQuestionViewFromUI(question.getOptions().get(w), localRecordID);
                    removeCategoryViewFromUI(question.getOptions().get(w), localRecordID);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void removeCategoryTitle(Categories localCategories, int localRecordID) {
        try {
            View myView = findViewById(localCategories.getId() + IntentConstants.VIEW_CONSTANT_NESTED_CONTAINERS + localRecordID);
            ViewGroup parent = (ViewGroup) myView.getParent();
            parent.removeView(myView);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (localCategories.getCategories() != null) {
            removeCategoryTitle(localCategories.getCategories(), localRecordID);
        }

    }

    //remove category views from main container
    public void removeCategoryViewFromUI(Options options, int localRecordID) {

        try {
            for (int k = 0; k < options.getCategories().size(); k++) {


                if (options.getCategories().get(k) != null) {
                    removeCategoryTitle(options.getCategories().get(k), localRecordID);
                }

                if (options.getCategories().get(k).getCategories() != null) {


                    if (options.getCategories().get(k).getCategories().getQuestionsList().size() > 0) {

                        for (int m = 0; m < options.getCategories().get(k).getCategories().getQuestionsList().size(); m++) {

                            View myView = findViewById(options.getCategories().get(k).getCategories().getQuestionsList().get(m).getId() + IntentConstants.VIEW_CONSTANT_NESTED_CONTAINERS + localRecordID);
                            ViewGroup parent = (ViewGroup) myView.getParent();
                            parent.removeView(myView);


                            if (options.getCategories().get(k).getCategories().getQuestionsList().get(m).getOptions().size() > 0) {
                                for (int n = 0; n < options.getCategories().get(k).getCategories().getQuestionsList().get(m).getOptions().size(); n++) {
                                    removeQuestionViewFromUI(options.getCategories().get(k).getCategories().getQuestionsList().get(m).getOptions().get(n), localRecordID);
                                    removeCategoryViewFromUI(options.getCategories().get(k).getCategories().getQuestionsList().get(m).getOptions().get(n), localRecordID);
                                }
                            }

                        }

                    }
                }


                if (options.getCategories().get(k).getQuestionsList().size() > 0) {

                    for (int i = 0; i < options.getCategories().get(k).getQuestionsList().size(); i++) {
                        View myView = findViewById(options.getCategories().get(k).getQuestionsList().get(i).getId() + IntentConstants.VIEW_CONSTANT_NESTED_CONTAINERS + localRecordID);
                        ViewGroup parent = (ViewGroup) myView.getParent();
                        parent.removeView(myView);

                        if (options.getCategories().get(k).getQuestionsList().get(i).getOptions().size() > 0) {
                            for (int j = 0; j < options.getCategories().get(k).getQuestionsList().get(i).getOptions().size(); j++) {
                                removeQuestionViewFromUI(options.getCategories().get(k).getQuestionsList().get(i).getOptions().get(j), localRecordID);
                                removeCategoryViewFromUI(options.getCategories().get(k).getQuestionsList().get(i).getOptions().get(j), localRecordID);
                            }
                        }
                    }


                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void addAnswerNextClick(Questions questions, int localRecordID) {

        Long tsLong = CommonUtil.getCurrentTimeStamp();
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
    public void checkForAnswer(Questions qu, int responseId, int localRecordId, boolean isChild) {

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
//            List<Integer> list2 =
//                    dbAdapter.getOptionIds(integers);


            List<Integer> choiceTableIds = dbAdapter.getIdFromChoicesTable(integers);


            for (int i = 0; i < choiceTableIds.size(); i++) {
                int optionId = dbAdapter.getOptionIdFromPrimaryId(choiceTableIds.get(i));
                optionId = optionId + localRecordId;
                CheckBox checkBox = (CheckBox) findViewById(optionId);
                checkBox.setChecked(true);
            }

        } else if (qu.getType().equals(CommonUtil.QUESTION_TYPE_RADIO_QUESTION)) {

            List<Options> options = qu.getOptions();
            for (int i = 0; i < options.size(); i++) {
                options.get(i).getId();
            }
            List<Integer> integers = dbAdapter.getIdFromAnswerTable(responseId, qu.getId(), localRecordId);
//            List<Integer> list2 =
//                    dbAdapter.getOptionIds(integers);


            List<Integer> choiceTableIds = dbAdapter.getIdFromChoicesTable(integers);


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
                    dbAdapter.getOptionIds(integers);

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
//            int answerId = dbAdapter.getIdFromAnswerTable(currentResponseId, qu.getId(), localRecordID).get(0);
            int answerId = dbAdapter.getAnswerId(currentResponseId, qu.getId(), localRecordID);
            choices1.setAnswerId(answerId);
            choices1.setOption("");
            choices1.setType(qu.getType());
            dbAdapter.insertDataChoicesTable(choices1);
        } else {
            Choices choices = new Choices();
//            int answerId = dbAdapter.getIdFromAnswerTable(currentResponseId, qu.getId(), localRecordID).get(0);
            int answerId = dbAdapter.getAnswerId(currentResponseId, qu.getId(), localRecordID);
            choices.setAnswerId(answerId);
            choices.setOptionId(options.getId());
            choices.setOption(options.getContent());
//            choices.setRecordId(localRecordID);
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


        dbAdapter.deleteOption(options);

    }

    public void removeOthersFromDataBaseForDefaultDropDown(Questions qu, int localRecordID) {

        //this removes the answers entry from answers table as well as choices in choices table of rest of the options
        for (int i = 0; i < qu.getOptions().size(); i++) {

//            dbAdapter.deleteFromChoicesTableWhereOptionId(qu.getOptions().get(i).getId());
            dbAdapter.deleteFromChoicesTableWhereAnswerId(dbAdapter.getAnswerId
                    (currentResponseId, qu.getId(), localRecordID), qu.getOptions().get(i).getId());
            if (qu.getOptions().get(i).getQuestions().size() > 0) {

                ArrayList<MasterQuestion> localMasterQuestion = null;
                try {
                    localMasterQuestion = getQuestionByRecordIDAndQuestionID(new MasterQuestion(qu.getOptions().get(i).getQuestions().get(i), localRecordID));
                } catch (Exception e) {

                }
                if (localMasterQuestion != null) {
                    for (int z = 0; z < localMasterQuestion.size(); z++) {
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

    private void removeCategoryQuestionsFromDB(Categories localCategories, int localRecordID) {
        for (int l = 0; l < localCategories.getQuestionsList().size(); l++) {
            ArrayList<MasterQuestion> localMasterQuestion = getQuestionByRecordIDAndQuestionID(new MasterQuestion(localCategories.getQuestionsList().get(l), localRecordID));
            for (int z = 0; z < localMasterQuestion.size(); z++) {
                masterQuestionList.remove(localMasterQuestion.get(z));
                dbAdapter.deleteFromAnswerTable(localMasterQuestion.get(z).getQuestions().getId(), currentResponseId, localRecordID);
            }
        }
        if (localCategories.getCategories() != null) {
            removeCategoryQuestionsFromDB(localCategories.getCategories(), localRecordID);
        }

    }

    //this is for not selected elements of radio
    public void removeOthersFromDataBase(Options options, Questions qu, int localRecordID) {

        //this removes the answers entry from answers table as well as choices in choices table of rest of the options
        for (int i = 0; i < qu.getOptions().size(); i++) {
            if (options.getId() != qu.getOptions().get(i).getId()) {
                dbAdapter.deleteFromChoicesTableWhereAnswerId(dbAdapter.getAnswerId
                        (currentResponseId, qu.getId(), localRecordID), qu.getOptions().get(i).getId());
                if (qu.getOptions().get(i).getQuestions().size() > 0) {


                    for (int n = 0; n < qu.getOptions().get(i).getQuestions().size(); n++) {
                        ArrayList<MasterQuestion> localMasterQuestion = null;
                        try {
                            localMasterQuestion = getQuestionByRecordIDAndQuestionID(new MasterQuestion(qu.getOptions().get(i).getQuestions().get(n), localRecordID));
                        } catch (Exception e) {
                        }

                        if (localMasterQuestion != null) {
                            for (int z = 0; z < localMasterQuestion.size(); z++) {
                                masterQuestionList.remove(localMasterQuestion.get(z));
                                dbAdapter.deleteFromAnswerTable(localMasterQuestion.get(z).getQuestions().getId(), currentResponseId, localRecordID);
                            }
                        }
                    }


                }


                //this removes all the category questions stored in answers table
                if (qu.getOptions().get(i).getCategories().size() > 0) {
                    for (int k = 0; k < qu.getOptions().get(i).getCategories().size(); k++) {
                        removeCategoryQuestionsFromDB(qu.getOptions().get(i).getCategories().get(k), localRecordID);
                    }
                }
            }
        }
    }


    /**
     * function to check if the question is mandatory or not
     * this function will also check all the questions on the page if any question is mandatory or not
     *
     * @param type
     * @param recordId
     * @param qId
     * @return
     */
    public boolean checkMandatoryQuestion(String type, int recordId, int qId) {

        //this will check for answers for questions having answers in answers table
        if (type.equals(CommonUtil.QUESTION_TYPE_SINGLE_LINE_QUESTION) || type.equals(CommonUtil.QUESTION_TYPE_MULTI_LINE_QUESTION) || type.equals(CommonUtil.QUESTION_TYPE_NUMERIC_QUESTION)) {


            if (((EditText) findViewById(qId + IntentConstants.VIEW_CONSTANT + recordId)).getText().toString().trim().equals("")) {
                return false;
            } else
                return true;
        }
        if (type.equals(CommonUtil.QUESTION_TYPE_DATE_QUESTION)) {

            if (dbAdapter.doesAnswerExistAsNonNull(qId, currentResponseId, recordId).equals("")) {
                return false;
            } else
                return true;
        }
        if (type.equals(CommonUtil.QUESTION_TYPE_RATING_QUESTION)) {
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
                if (dbAdapter.getChoicesCountWhereAnswerIdIs(ansID) == 0) {
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
            dateText = (TextView) findViewById(localQuestions.getId() + IntentConstants.VIEW_CONSTANT + localRecordID);
            dateText.setText(year + "/" + (monthOfYear + 1) + "/" + dayOfMonth);

            Answers localAnswer = new Answers(localRecordID, currentResponseId, localQuestions
                    .getId(), dateText.getText().toString(), CommonUtil.getCurrentTimeStamp());
            saveAnswerIntoDB(new TagModel(localQuestions.getId(), localRecordID), localAnswer);

        }

    }

}