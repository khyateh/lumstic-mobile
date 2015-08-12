package lumstic.ashoka.com.lumstic.UI;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
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
import lumstic.ashoka.com.lumstic.Models.Options;
import lumstic.ashoka.com.lumstic.Models.Questions;
import lumstic.ashoka.com.lumstic.Models.Surveys;
import lumstic.ashoka.com.lumstic.Models.TagModel;
import lumstic.ashoka.com.lumstic.R;
import lumstic.ashoka.com.lumstic.Utils.IntentConstants;
import lumstic.ashoka.com.lumstic.Utils.LumsticApp;

public class NewResponseActivity extends Activity {
    DBAdapter dbAdapter;
    ActionBar actionBar;
    int countofcheck = 0;
    private ArrayList<Integer> types = null;
    private ArrayList<String> stringTypes = null;
    private List<Questions> questionsList;
    private List<Categories> categoriesList;
    private List<MasterQuestion> masterQuestionList = new ArrayList<>();
    private Long tsLong = null;
    private String fname = "";
    private int questionCount = 0;
    private int categoryCount = 0;
    private int currentResponseId = 0;
    private int categoryAndQuestionCount = 0;
    private int totalQuestionCount = 0;
    private int CAMERA_REQUEST = 1;
    private int questionCounter = 0;
    private int categoryQuestionCounter = 0;
    private Surveys surveys;
    private Answers answers;
    private Categories currentCategory;

    private EditText answer;
    private TextView dateText;
    private Spinner spinner;
    private RadioGroup radioGroup;
    private RelativeLayout imageContainer;
    private ImageView imageViewPhotoQuestion;
    private RelativeLayout deleteImageRelativeLayout;
    private Button counterButton, markAsComplete;
    private Button nextQuestion, previousQuestion;
    private LinearLayout fieldContainer;
    private LayoutInflater inflater;
    private RatingBar ratingBar;
    private int recordId = 0;
    private LumsticApp lumsticApp;
    private String order = "";
    private boolean numberlimitOk = true;
    private boolean nextPressed = false;
    private boolean addRecordPressed = false;
    private TagModel tagValue;
    private MasterQuestion tagMasterQuestion;
    private int addRecordCounter = 0;
    private int defaultParentIndex = 0;
    private LinearLayout takePictureContainer;

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
        lumsticApp = (LumsticApp) getApplication();
        //declaration of list items
        stringTypes = new ArrayList<>();
        types = new ArrayList<>();

        questionsList = new ArrayList<>();
        categoriesList = new ArrayList<>();
        //create mark as complete button and mandatory text
        createMarkAsComplete();
        makeMandatoryText();
        previousQuestion.setText("BACK");
        //surveys from previous activity
        surveys = (Surveys) getIntent().getExtras().getSerializable(IntentConstants.SURVEY);
        //remove category questions from questions array
        removeQuestionBelongingTOCategory(surveys);
        //remove categories which are not at root level
        removeNonRootLevelCategories(surveys);
        //get app response id
        getResponseId();
        //getTotalCountOfQuestionAndCategory
        getCategoryAndQuestionsCount(surveys);
        //sort order of questions and category,type has order number stored and string types has question or category
        sortOrder();
        //build first question
        buildFirstQuestion();
        //check if first question is the last question
        checkIfLastQuestion();
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
                onNextClick();
            }
        });

        //on previous pressed
        previousQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackClicked();
            }
        });
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
        markAsComplete.setBackgroundResource(R.drawable.custom_button);
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

    public void removeQuestionBelongingTOCategory(Surveys surveys) {
        for (int j = surveys.getQuestions().size() - 1; j >= 0; j--) {
            if (surveys.getQuestions().get(j).getCategoryId() > 0)
                surveys.getQuestions().remove(j);
        }
    }

    public void removeNonRootLevelCategories(Surveys surveys) {
        for (int j = surveys.getCategories().size() - 1; j >= 0; j--) {
            if (surveys.getCategories().get(j).getParentId() > 0)
                surveys.getCategories().remove(j);
        }
    }

    public void getResponseId() {
        if (getIntent().hasExtra(IntentConstants.RESPONSE_ID)) {
            currentResponseId = getIntent().getIntExtra(IntentConstants.RESPONSE_ID, 0);
        }
        if (!getIntent().hasExtra(IntentConstants.RESPONSE_ID)) {
            currentResponseId = (int) dbAdapter.getMaxID();
        }
    }

    public void getCategoryAndQuestionsCount(Surveys surveys) {

        if (surveys.getQuestions().size() > 0) {
            questionsList = surveys.getQuestions();
            questionCount = questionsList.size();
        }


        if (surveys.getCategories().size() > 0) {
            categoriesList = surveys.getCategories();
            categoryCount = categoriesList.size();
        }


        totalQuestionCount = categoryCount + questionCount;
    }

    public void sortOrder() {
        int i = 0;
        for (int count = 0; count < 100; count++) {
            for (i = 0; i < questionsList.size(); i++) {
                if (questionsList.get(i).getOrderNumber() == count) {
                    types.add(count);
                    stringTypes.add("question");
                }


            }
            for (i = 0; i < categoriesList.size(); i++) {
                if (categoriesList.get(i).getOrderNumber() == count) {
                    types.add(count);
                    stringTypes.add("category");
                }


            }
        }
    }

    public void onMarkComplete() {

        if (storeDataToDB() == 0) {
            return;
        }
        addRecordCounter = 0;
        recordId = 0;


        dbAdapter.UpldateCompleteResponse(currentResponseId, questionsList.get(0).getSurveyId());
        Intent intent = new Intent(NewResponseActivity.this, SurveyDetailsActivity.class);
        intent.putExtra(IntentConstants.SURVEY, (java.io.Serializable) surveys);
        startActivity(intent);
        finish();

    }

    public void buildFirstQuestion() {
        //build from questions
        for (int j = 0; j < questionsList.size(); j++) {
            if (questionsList.get(j).getOrderNumber() == types.get(0)) {
                Questions cq = questionsList.get(j);
                counterButton.setText("1 out of " + totalQuestionCount);
                buildLayout(cq, false, recordId, defaultParentIndex);
                checkForAnswer(cq, currentResponseId, recordId);
                break;
            }
        }


        //build first question as category from category array
        for (int j = 0; j < categoriesList.size(); j++) {
            if (categoriesList.get(j).getOrderNumber() == types.get(0)) {
                counterButton.setText("1 out of " + totalQuestionCount);
                Categories currentCategory = categoriesList.get(j);
                buildCategoryLayout(currentCategory);
                for (int k = 0; k < currentCategory.getQuestionsList().size(); k++) {
                    checkForAnswer(currentCategory.getQuestionsList().get(k), currentResponseId, recordId);
                }
                break;
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

    public void buildCategoryLayout(Categories categories) {
        setCategoryTitle(categories);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_response, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.save) {

            finish();

            return true;

        }
        if (id == android.R.id.home) {

            finish();

            return true;

        }
        if (id == R.id.action_logout) {

            final Dialog dialog = new Dialog(NewResponseActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //before
            dialog.setContentView(R.layout.logout_dialog);
            dialog.show();
            Button button = (Button) dialog.findViewById(R.id.okay);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    lumsticApp.getPreferences().setAccessToken("");
                    Intent i = new Intent(NewResponseActivity.this, LoginActivity.class);
                    startActivity(i);
                    dialog.dismiss();
                }
            });

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public LinearLayout createNestedContainer() {
        LinearLayout nestedContainer = new LinearLayout(this);
        nestedContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(10, 10, 10, 30);
        nestedContainer.setLayoutParams(layoutParams);


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
        if (ques.getType().equals("SingleLineQuestion")) {
            LinearLayout nestedContainer = createNestedContainer();
            TextView questionTextSingleLine = createQuestionTitle(ques, isChild, parentRecordId);
            nestedContainer.addView(questionTextSingleLine);
            nestedContainer.setId(ques.getId());
            nestedContainer.addView(inflater.inflate(R.layout.answer_single_line, null));
            tagValue = new TagModel(ques.getId(), recordId, ques);


            if (isChild) {
                nestedContainer.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), parentRecordId, ques));
                fieldContainer.addView(nestedContainer, parentViewPosition);

            } else {
                nestedContainer.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), recordId, ques));
                fieldContainer.addView(nestedContainer);

            }

            answer = (EditText) findViewById(R.id.answer_text);
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
                        tsLong = System.currentTimeMillis() / 1000;
                        TagModel localTagModel = (TagModel) ((View) view.getParent()).getTag(R.string.multirecord_tag);
                        Answers localAnswer = new Answers(localTagModel.getRecordID(), currentResponseId, localTagModel.getqID(), answer.getText().toString(), tsLong);
                        saveAnswerIntoDB(localTagModel, localAnswer);

                    }
                }
            });

            try {
                if (isChild)
                    checkForAnswer(ques, currentResponseId, parentRecordId);
                else
                    checkForAnswer(ques, currentResponseId, recordId);
            } catch (Exception e) {
                e.printStackTrace();
                checkHint();
            }
        } else if (ques.getType().contains("MultilineQuestion")) {
            LinearLayout nestedContainer = createNestedContainer();
            TextView questionTextSingleLine = createQuestionTitle(ques, isChild, parentRecordId);
            nestedContainer.addView(questionTextSingleLine);
            nestedContainer.setId(ques.getId());
            nestedContainer.addView(inflater.inflate(R.layout.answer_multi_line, null));
            nestedContainer.setId(ques.getId());
            tagValue = new TagModel(ques.getId(), recordId, ques);
            if (isChild) {
                nestedContainer.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), parentRecordId, ques));
                fieldContainer.addView(nestedContainer, parentViewPosition);

            } else {
                nestedContainer.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), recordId, ques));
                fieldContainer.addView(nestedContainer);

            }
            answer = (EditText) findViewById(R.id.answer_text);
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
                        tsLong = System.currentTimeMillis() / 1000;
                        TagModel localTagModel = (TagModel) ((View) view.getParent()).getTag(R.string.multirecord_tag);
                        Answers localAnswer = new Answers(localTagModel.getRecordID(), currentResponseId, localTagModel.getqID(), answer.getText().toString(), tsLong);
                        saveAnswerIntoDB(localTagModel, localAnswer);
                    }
                }
            });


            try {
                if (isChild)
                    checkForAnswer(ques, currentResponseId, parentRecordId);
                else
                    checkForAnswer(ques, currentResponseId, recordId);
            } catch (Exception e) {
                e.printStackTrace();
                checkHint();
            }
            checkHint();

        } else if (ques.getType().contains("DropDownQuestion")) {


            final LinearLayout nestedContainer = createNestedContainer();
            TextView questionTextSingleLine = createQuestionTitle(ques, isChild, parentRecordId);
            nestedContainer.addView(questionTextSingleLine);
            tagValue = new TagModel(ques.getId(), recordId, ques);
            spinner = new Spinner(NewResponseActivity.this);
            spinner = (Spinner) getLayoutInflater().inflate(R.layout.answer_dropdown, null);

            spinner.setTag(order);
            if (isChild) {
                nestedContainer.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), parentRecordId, ques));
                fieldContainer.addView(nestedContainer, parentViewPosition);
                nestedContainer.addView(spinner, parentViewPosition);
                spinner.setId(ques.getId() + IntentConstants.VIEW_CONSTANT + parentRecordId);
                masterQuestionList.add(new MasterQuestion(ques, parentRecordId, ques.getId() + IntentConstants.VIEW_CONSTANT + parentRecordId));
//                if (!dbAdapter.doesAnswerExist(ques.getId(), currentResponseId, parentRecordId)) {
                addAnswerNextClick(ques, parentRecordId);
//                }
            } else {
                nestedContainer.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), recordId, ques));
                fieldContainer.addView(nestedContainer);
                spinner.setId(ques.getId() + IntentConstants.VIEW_CONSTANT + recordId);
                masterQuestionList.add(new MasterQuestion(ques, recordId, ques.getId() + IntentConstants.VIEW_CONSTANT + recordId));
//                if (!dbAdapter.doesAnswerExist(ques.getId(), currentResponseId, recordId)) {
                addAnswerNextClick(ques, recordId);
//                }
                nestedContainer.addView(spinner);
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
//                    try {
                    if (i == 0) {
                        // addOptionToDataBase(null, ques, ((TagModel) nestedContainer.getTag(R.string.multirecord_tag)).getRecordID());
                        removeOthersFromDataBaseForDefaultDropDown(ques, ((TagModel) nestedContainer.getTag(R.string.multirecord_tag)).getRecordID());
                    }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }

                    if (i != 0) {
                        //add option selected to database table choices table
                        Options options = ques.getOptions().get(i - 1);
                        addOptionToDataBase(options, ques, ((TagModel) nestedContainer.getTag(R.string.multirecord_tag)).getRecordID());
                        removeOthersFromDataBase(options, ques, ((TagModel) nestedContainer.getTag(R.string.multirecord_tag)).getRecordID());

//                        //For Deleting Default Selection From DB
//                        dbAdapter.deleteFromChoicesTableWhereOptionId(0, ((TagModel) nestedContainer.getTag(R.string.multirecord_tag)).getRecordID());


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
                                removeQuestionView(ques.getOptions().get(j));
                                removeCategoryView(ques.getOptions().get(j));
                            }
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }

            });
            try {
                if (isChild) {
                    checkForAnswer(ques, currentResponseId, parentRecordId);
                } else {
                    checkForAnswer(ques, currentResponseId, recordId);
                }
            } catch (Exception e) {

            }
        } else if (ques.getType().contains("MultiChoiceQuestion")) {


            final LinearLayout nestedContainer = createNestedContainer();
            TextView questionTextSingleLine = createQuestionTitle(ques, isChild, parentRecordId);
            nestedContainer.addView(questionTextSingleLine);
            nestedContainer.setId(ques.getId());


            if (isChild) {
                nestedContainer.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), parentRecordId, ques));
                fieldContainer.addView(nestedContainer, parentViewPosition);
                masterQuestionList.add(new MasterQuestion(ques, parentRecordId));
                if (!dbAdapter.doesAnswerExist(ques.getId(), currentResponseId, parentRecordId)) {
                    addAnswerNextClick(ques, parentRecordId);

                }
            } else {
                nestedContainer.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), recordId, ques));
                fieldContainer.addView(nestedContainer);
                masterQuestionList.add(new MasterQuestion(ques, recordId));
                if (!dbAdapter.doesAnswerExist(ques.getId(), currentResponseId, recordId)) {
                    addAnswerNextClick(ques, recordId);

                }
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


                tagValue = new TagModel(ques.getId(), recordId, ques);
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
                                removeQuestionView(options);
                            }
                            if (options.getCategories().size() > 0) {
                                removeCategoryView(options);
                            }
                        }
                    }
                });
            }

            try {
                if (isChild) {
                    checkForAnswer(ques, currentResponseId, parentRecordId);
                } else {
                    checkForAnswer(ques, currentResponseId, recordId);
                }
            } catch (Exception e) {

            }

            checkHint();
        } else if (ques.getType().contains("NumericQuestion")) {
            LinearLayout nestedContainer = createNestedContainer();
            TextView questionTextSingleLine = createQuestionTitle(ques, isChild, parentRecordId);
            nestedContainer.addView(questionTextSingleLine);
            nestedContainer.addView(inflater.inflate(R.layout.answer_numeric, null));
            nestedContainer.setId(ques.getId());
            if (isChild) {
                nestedContainer.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), parentRecordId, ques));
                fieldContainer.addView(nestedContainer, parentViewPosition);

            } else {
                nestedContainer.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), recordId, ques));

                fieldContainer.addView(nestedContainer);
            }
            tagValue = new TagModel(ques.getId(), recordId, ques);

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
                        tsLong = System.currentTimeMillis() / 1000;
                        TagModel localTagModel = (TagModel) ((View) view.getParent()).getTag(R.string.multirecord_tag);
                        Answers localAnswer = new Answers(localTagModel.getRecordID(), currentResponseId, localTagModel.getqID(), answer.getText().toString(), tsLong);
                        saveAnswerIntoDB(localTagModel, localAnswer);
                    }
                }
            });

            try {
                if (isChild)
                    checkForAnswer(ques, currentResponseId, parentRecordId);
                else
                    checkForAnswer(ques, currentResponseId, recordId);
            } catch (Exception e) {
                e.printStackTrace();
                checkHint();
            }
            checkHint();
        } else if (ques.getType().contains("DateQuestion")) {
            final LinearLayout nestedContainer = createNestedContainer();
            TextView questionTextSingleLine = createQuestionTitle(ques, isChild, parentRecordId);
            nestedContainer.addView(questionTextSingleLine);
            nestedContainer.addView(inflater.inflate(R.layout.answer_date_picker, null));
            nestedContainer.setId(ques.getId());
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
            tagValue = new TagModel(ques.getId(), recordId, ques);


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
            try {
                if (isChild)
                    checkForAnswer(ques, currentResponseId, parentRecordId);
                else
                    checkForAnswer(ques, currentResponseId, recordId);
            } catch (Exception e) {
                e.printStackTrace();
                checkHint();
            }
        } else if (ques.getType().contains("RadioQuestion")) {


            final LinearLayout nestedContainer = createNestedContainer();
            TextView questionTextSingleLine = createQuestionTitle(ques, isChild, parentRecordId);
            nestedContainer.addView(questionTextSingleLine);
            nestedContainer.setId(ques.getId());
            nestedContainer.setTag(ques);

            tagValue = new TagModel(ques.getId(), recordId, ques);

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
                                //  checkForAnswer(options.getQuestions().get(i), currentResponseId, ((TagModel) nestedContainer.getTag(R.string.multirecord_tag)).getRecordID());
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
                                removeQuestionView(ques.getOptions().get(i));
                                removeCategoryView(ques.getOptions().get(i));
                            }
                        }
                    }
                });
            }
            try {
                if (isChild) {
                    checkForAnswer(ques, currentResponseId, parentRecordId);
                } else {
                    checkForAnswer(ques, currentResponseId, recordId);
                }
            } catch (Exception e) {

            }
            checkHint();


        } else if (ques.getType().equals("RatingQuestion")) {

            final LinearLayout nestedContainer = createNestedContainer();
            TextView questionTextSingleLine = createQuestionTitle(ques, isChild, parentRecordId);

            nestedContainer.addView(questionTextSingleLine);
            nestedContainer.addView(inflater.inflate(R.layout.answer_rating, null));
            nestedContainer.setId(ques.getId());

            if (isChild) {
                nestedContainer.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), parentRecordId, ques));
                fieldContainer.addView(nestedContainer, parentViewPosition);
                masterQuestionList.add(new MasterQuestion(ques, parentRecordId));
            } else {
                nestedContainer.setTag(R.string.multirecord_tag, new TagModel(ques.getId(), recordId, ques));
                masterQuestionList.add(new MasterQuestion(ques, recordId));
                fieldContainer.addView(nestedContainer);
            }
            tagValue = new TagModel(ques.getId(), recordId, ques);

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


                    tsLong = System.currentTimeMillis() / 1000;
                    TagModel localTagModel = (TagModel) (nestedContainer).getTag(R.string.multirecord_tag);

                    if (dbAdapter.doesAnswerExist(ques.getId(), currentResponseId)) {
                        dbAdapter.deleteRatingAnswer(localTagModel.getqID(), currentResponseId, localTagModel.getRecordID());
                    }
                    Answers localAnswer = new Answers(localTagModel.getRecordID(), currentResponseId, localTagModel.getqID(), String.valueOf(v), tsLong);
                    saveAnswerIntoDB(localTagModel, localAnswer);

                }
            });

            try {
                if (isChild) {
                    checkForAnswer(ques, currentResponseId, parentRecordId);
                } else {
                    checkForAnswer(ques, currentResponseId, recordId);
                }
            } catch (Exception e) {

            }
            checkHint();
        } else if (ques.getType().equals("PhotoQuestion")) {

            final LinearLayout nestedContainer = createNestedContainer();
            TextView questionTextSingleLine = createQuestionTitle(ques, isChild, parentRecordId);
            nestedContainer.addView(questionTextSingleLine);
            //   nestedContainer.setId(ques.getId());
            nestedContainer.addView(inflater.inflate(R.layout.answer_image_picker, null));

            tagValue = new TagModel(ques.getId(), recordId, ques);


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
            imageContainer = (RelativeLayout) findViewById(R.id.image_container);


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

            try {
                if (isChild) {
                    checkForAnswer(ques, currentResponseId, parentRecordId);

//                    PhotoBitmapModel localPhotoBitmapModel = checkForAnswer(ques, currentResponseId, parentRecordId);
//                    if (localPhotoBitmapModel.getBitmap() != null)
//                        imageViewPhotoQuestion.setImageBitmap(localPhotoBitmapModel.getBitmap());
//                    if (localPhotoBitmapModel.getIsExist()) {
//                        imageContainer.setVisibility(View.VISIBLE);
//                    } else {
//                        imageContainer.setVisibility(View.GONE);
//                }

                } else {
                    checkForAnswer(ques, currentResponseId, recordId);
//                    PhotoBitmapModel localPhotoBitmapModel = checkForAnswer(ques, currentResponseId, recordId);
//                    if (localPhotoBitmapModel.getBitmap() != null)
//                        imageViewPhotoQuestion.setImageBitmap(localPhotoBitmapModel.getBitmap());
//                    if (localPhotoBitmapModel.getIsExist()) {
//                        imageContainer.setVisibility(View.VISIBLE);
//                    } else {
//                        imageContainer.setVisibility(View.GONE);
//                    }

                }
            } catch (Exception e) {

            }
        }


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
            if (masterQuestionList.get(i).getQuestions().getType().equals("SingleLineQuestion") || masterQuestionList.get(i).getQuestions().getType().equals("MultilineQuestion")) {
                int localMaxLength = masterQuestionList.get(i).getQuestions().getMaxLength();
                if (localMaxLength != 0)
                    if (((EditText) findViewById(masterQuestionList.get(i).getAnsAndroidID())).getText().toString().trim().length() > localMaxLength) {
                        showNumberValidationDialog();

                        return success;
                    }
            }
            if (masterQuestionList.get(i).getQuestions().getType().equals("NumericQuestion")) {
                int localMaxValue = masterQuestionList.get(i).getQuestions().getMaxValue();
                int localMinValue = masterQuestionList.get(i).getQuestions().getMinValue();
                if (localMaxValue != localMinValue) {
                    String localData = ((EditText) findViewById(masterQuestionList.get(i).getAnsAndroidID())).getText().toString();
                    int localValue;
                    if (localData != null) {
                        try {
                            localValue = Integer.parseInt(localData);
                            if (localValue > localMaxValue || localValue < localMinValue) {
                                showNumberValidationDialog();

                                return success;
                            }
                        } catch (NumberFormatException e) {

                        }

                    }
                }
            }

            if (masterQuestionList.get(i).getQuestions().getMandatory() == 1) {

                if (checkMandatoryQuestion(masterQuestionList.get(i).getQuestions().getType(), masterQuestionList.get(i).getRecordID(), masterQuestionList.get(i).getQuestions().getId())) {
                    if (!masterQuestionList.get(i).getQuestions().getType().equals("MultiChoiceQuestion") && !masterQuestionList.get(i).getQuestions().getType().equals("DropDownQuestion") && !masterQuestionList.get(i).getQuestions().getType().equals("RadioQuestion")) {
                        addAnswerNextClick(masterQuestionList.get(i).getQuestions(), masterQuestionList.get(i).getRecordID());
                    }
                } else {
                    showDialog();
                    return success;
                }

            } else {

                if (!masterQuestionList.get(i).getQuestions().getType().equals("MultiChoiceQuestion") && !masterQuestionList.get(i).getQuestions().getType().equals("DropDownQuestion") && !masterQuestionList.get(i).getQuestions().getType().equals("RadioQuestion")) {
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

    private void showNumberValidationDialog() {
        final Dialog dialog = new Dialog(NewResponseActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //before
        dialog.setContentView(R.layout.number_limit_dialog);
        dialog.show();
        Button button = (Button) dialog.findViewById(R.id.okay);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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

        //set next and previous buttons
        if (questionCounter < totalQuestionCount - 1) {
            previousQuestion.setBackgroundColor(getResources().getColor(R.color.login_button_color));
            previousQuestion.setTextColor(getResources().getColor(R.color.white));
            previousQuestion.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_arrow_back, 0, 0, 0);

            fieldContainer.removeAllViews();
            questionCounter++;

            counterButton.setText(questionCounter + 1 + " out of " + totalQuestionCount);
//for category type questions


            for (int j = 0; j < categoriesList.size(); j++) {
                if (categoriesList.get(j).getOrderNumber() == types.get(questionCounter)) {

                    currentCategory = categoriesList.get(j);
                    //for multi record questions
                    if (currentCategory.getType().equals("MultiRecordCategory")) {

                        NextBackCommonCode();

                    } else {
                        buildCategoryLayout(currentCategory);
                    }

                }
            }


            //for general questions
            for (int j = 0; j < questionsList.size(); j++) {
                if (questionsList.get(j).getOrderNumber() == types.get(questionCounter)) {
                    Questions cq = questionsList.get(j);
                    buildLayout(cq, false, recordId, defaultParentIndex);
                    //  checkForAnswer(cq, currentResponseId, recordId);
                    break;
                }
            }

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

    public void onAddRecordClick(int localRecordID) {
        recordId = localRecordID;
        addRecordCounter++;
        setCategoryQuestion(currentCategory, localRecordID);
        createDeleteRecord(tagValue);
        try {
            answer.clearFocus();
        } catch (Exception e) {
            e.printStackTrace();

        }

        addRecordPressed = true;
    }

    public void buildMultiRecordTitle(Categories categories) {
        LinearLayout nestedContainer = new LinearLayout(this);
        nestedContainer.setOrientation(LinearLayout.VERTICAL);
        TextView questionTextSingleLine = new TextView(this);
        questionTextSingleLine.setTextSize(20);
        questionTextSingleLine.setTextColor(Color.BLACK);
        questionTextSingleLine.setPadding(8, 12, 8, 20);
        questionTextSingleLine.setText("" + categories.getContent());
        nestedContainer.addView(questionTextSingleLine);
        nestedContainer.setId(categories.getId());
        nestedContainer.setTag(categories);
        fieldContainer.addView(nestedContainer);


    }

    public void createDeleteRecord(TagModel tmpTagModel) {

        if (currentCategory.getType().equals("MultiRecordCategory")) {
            final Button deleteRecord = new Button(this);
            deleteRecord.setBackgroundResource(R.drawable.custom_button);
            deleteRecord.setText("Delete Record");
            deleteRecord.setTextColor(getResources().getColor(R.color.white));
            deleteRecord.setTag(R.string.multirecord_tag, tmpTagModel);
            fieldContainer.addView(deleteRecord);


            deleteRecord.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addRecordCounter--;

                    TagModel tag = (TagModel) view.getTag(R.string.multirecord_tag);

                    Log.e("TAG", "TAgValue BUTTON VALUE->> QID->>" + tagValue.getqID() + "RecordID->>" + tagValue.getRecordID());
                    Log.e("TAG", "DELETE BUTTON VALUE->> QID->>" + tag.getqID() + "RecordID->>" + tag.getRecordID());

                    ArrayList<MasterQuestion> localMasterQuestions = getQuestionByRecordID(new MasterQuestion(tag.getQues(), tag.getRecordID()));

                    for (int j = 0; j < localMasterQuestions.size(); j++) {
                        masterQuestionList.remove(localMasterQuestions.get(j));
                        dbAdapter.deleteFromAnswerTableWithRecordId(localMasterQuestions.get(j).getQuestions().getId(), currentResponseId, localMasterQuestions.get(j).getRecordID());
                        //Choices Table Call Remaining
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

    private void NextBackCommonCode() {
        // recordId = currentCategory.getId();
        Button addRecord = new Button(this);
        addRecord.setBackgroundResource(R.drawable.custom_button);
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
                onAddRecordClick(recordId);


            }
        });

        Cursor multiRecordCursor = dbAdapter.findNoOfEntries(currentCategory.getQuestionsList().get(0).getId(), currentResponseId);

        int entries = 0;
        if (multiRecordCursor != null) {
            addRecordCounter = entries = multiRecordCursor.getCount();
            Log.e("TAG", "LOCAL->> C" + entries);
        }
        if (entries == 0) {
            recordId = currentCategory.getCategoryId();
            buildMultiRecordTitle(currentCategory);
        } else {
            multiRecordCursor.moveToFirst();
            for (int i = 0; i < entries; i++) {
                int localRecordID = multiRecordCursor.getInt(multiRecordCursor.getColumnIndex(DBAdapter.DBhelper.RECORD_ID));
                buildMultiRecordTitle(currentCategory);
                onAddRecordClick(localRecordID);
                multiRecordCursor.moveToNext();
            }
        }
    }

    //on back is clicked
    public void onBackClicked() {


        markAsComplete.setVisibility(View.GONE);
        hideKeyBoard();
        addRecordCounter = 0;
        recordId = 0;
        //Need to add mandatory logic
        for (int i = 0; i < masterQuestionList.size(); i++) {
            Log.e("Master", "Master Details QID->>" + masterQuestionList.get(i).getQuestions().getId() + "RecordID->>" + masterQuestionList.get(i).getRecordID());

            if (!masterQuestionList.get(i).getQuestions().getType().equals("MultiChoiceQuestion") && !masterQuestionList.get(i).getQuestions().getType().equals("DropDownQuestion") && !masterQuestionList.get(i).getQuestions().getType().equals("RadioQuestion"))
                addAnswerNextClick(masterQuestionList.get(i).getQuestions(), masterQuestionList.get(i).getRecordID());
        }


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

            for (int j = 0; j < categoriesList.size(); j++) {
                if (categoriesList.get(j).getOrderNumber() == types.get(questionCounter)) {

                    currentCategory = categoriesList.get(j);
                    //for multi record questions
                    if (currentCategory.getType().equals("MultiRecordCategory")) {

                        NextBackCommonCode();

                    } else {
                        buildCategoryLayout(currentCategory);
                    }


                }
            }

            //build question layout
            for (int j = 0; j < questionsList.size(); j++) {
                if (questionsList.get(j).getOrderNumber() == types.get(questionCounter)) {
                    Questions cq = questionsList.get(j);
                    buildLayout(cq, false, recordId, defaultParentIndex);
                    //   checkForAnswer(cq, currentResponseId, recordId);
                    break;
                }
            }
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
//            byte[] byteArray = byteArrayOutputStream.toByteArray();
//            String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
//            imageViewPhotoQuestion.setImageBitmap(b);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return b;
    }


    //set category titles and build category layout
    public void setCategoryTitle(Categories categories) {
        LinearLayout nestedContainer = new LinearLayout(this);
        nestedContainer.setOrientation(LinearLayout.VERTICAL);
        TextView questionTextSingleLine = new TextView(this);
        questionTextSingleLine.setTextSize(20);
        questionTextSingleLine.setTextColor(Color.BLACK);
        questionTextSingleLine.setPadding(8, 12, 8, 20);
        questionTextSingleLine.setText("" + categories.getContent());
        nestedContainer.addView(questionTextSingleLine);
        nestedContainer.setId(categories.getId());
        nestedContainer.setTag(categories);
        fieldContainer.addView(nestedContainer);


        for (int j = 0; j < categories.getQuestionsList().size(); j++) {
            categoryQuestionCounter++;
            buildLayout(categories.getQuestionsList().get(j), false, recordId, defaultParentIndex);

            //  swer(categories.getQuestionsList().get(j), currentResponseId, recordId);

        }
        categoryQuestionCounter = 0;

    }

    public void setCategoryQuestion(Categories categories, int localRecordID) {

        for (int j = categories.getQuestionsList().size() - 1; j >= 0; j--) {
            categoryQuestionCounter++;
            buildLayout(categories.getQuestionsList().get(j), false, localRecordID, defaultParentIndex);

//            swer(categories.getQuestionsList().get(j), currentResponseId, localRecordID);

        }
        categoryQuestionCounter = 0;

    }

    //remove questions view from main container
    public void removeQuestionView(Options options) {
        try {
            for (int i = 0; i < options.getQuestions().size(); i++) {
                View myView = findViewById(options.getQuestions().get(i).getId());
                ViewGroup parent = (ViewGroup) myView.getParent();
                parent.removeView(myView);

                if (options.getQuestions().get(i).getOptions().size() > 0) {
                    for (int j = 0; j < options.getQuestions().get(i).getOptions().size(); j++) {
                        removeQuestionView(options.getQuestions().get(i).getOptions().get(j));
                        removeCategoryView(options.getQuestions().get(i).getOptions().get(j));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //remove category views from main container
    public void removeCategoryView(Options options) {

        try {
            for (int k = 0; k < options.getCategories().size(); k++) {

                View myView = findViewById(options.getCategories().get(k).getId());
                ViewGroup parent = (ViewGroup) myView.getParent();
                parent.removeView(myView);
                for (int h = 0; h < options.getCategories().get(k).getQuestionsList().size(); h++) {
                    View myView2 = findViewById(options.getCategories().get(k).getQuestionsList().get(h).getId());
                    ViewGroup parent2 = (ViewGroup) myView2.getParent();
                    parent2.removeView(myView2);
                    if (options.getCategories().get(k).getQuestionsList().get(h).getOptions().size() > 0) {
                        for (int j = 0; j < options.getCategories().get(k).getQuestionsList().get(h).getOptions().size(); j++) {
                            removeQuestionView(options.getCategories().get(k).getQuestionsList().get(h).getOptions().get(j));
                            removeCategoryView(options.getCategories().get(k).getQuestionsList().get(h).getOptions().get(j));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addAnswerNextClick(Questions questions, int localRecordID) {


        tsLong = System.currentTimeMillis() / 1000;
        Answers localAnswer;

        try {
            if (questions.getType().equals("SingleLineQuestion")) {
                String localAns = ((EditText) findViewById(questions.getId() + IntentConstants.VIEW_CONSTANT + localRecordID)).getText().toString().trim();
                if (localRecordID != 0) {
                    localAnswer = new Answers(localRecordID, currentResponseId, questions.getId(), localAns, tsLong, "multirecord");
                } else {
                    localAnswer = new Answers(localRecordID, currentResponseId, questions.getId(), localAns, tsLong);
                }
                if (!dbAdapter.doesAnswerExist(questions.getId(), currentResponseId, localRecordID)) {
                    dbAdapter.insertDataAnswersTable(localAnswer);
                }
            } else if (questions.getType().equals("MultilineQuestion")) {
                String localAns = ((EditText) findViewById(questions.getId() + IntentConstants.VIEW_CONSTANT + localRecordID)).getText().toString().trim();
                localAnswer = new Answers(localRecordID, (int) dbAdapter.getMaxID(), questions.getId(), localAns, tsLong);

                if (!dbAdapter.doesAnswerExist(questions.getId(), currentResponseId, localRecordID)) {
                    dbAdapter.insertDataAnswersTable(localAnswer);
                }
            } else if (questions.getType().equals("NumericQuestion")) {
                String localAns = ((EditText) findViewById(questions.getId() + IntentConstants.VIEW_CONSTANT + localRecordID)).getText().toString().trim();
                localAnswer = new Answers(localRecordID, (int) dbAdapter.getMaxID(), questions.getId(), localAns, tsLong);

                if (!dbAdapter.doesAnswerExist(questions.getId(), currentResponseId, localRecordID)) {
                    dbAdapter.insertDataAnswersTable(localAnswer);
                }
            } else if (questions.getType().equals("DateQuestion")) {

                localAnswer = new Answers(localRecordID, (int) dbAdapter.getMaxID(), questions.getId(), dateText.getText().toString(), tsLong);

                if (!dbAdapter.doesAnswerExist(questions.getId(), currentResponseId, localRecordID))
                    dbAdapter.insertDataAnswersTable(localAnswer);

            } else if (questions.getType().equals("RatingQuestion")) {
                RatingBar localRatingBar = (RatingBar) findViewById(questions.getId() + IntentConstants.VIEW_CONSTANT + localRecordID);
                localAnswer = new Answers(localRecordID, (int) dbAdapter.getMaxID(), questions.getId(), String.valueOf(localRatingBar.getRating()), tsLong);
                if (!dbAdapter.doesAnswerExist(questions.getId(), (int) dbAdapter.getMaxID(), localRecordID))
                    dbAdapter.insertDataAnswersTable(localAnswer);

            } else if (questions.getType().equals("PhotoQuestion")) {
                localAnswer = new Answers(localRecordID, (int) dbAdapter.getMaxID(), questions.getId(), "", tsLong, "PhotoQuestion", fname);
                if (!dbAdapter.doesAnswerExist(questions.getId(), (int) dbAdapter.getMaxID(), localRecordID)) {
                    dbAdapter.insertDataAnswersTable(localAnswer);
                }
                fname = "";


//            } else if (questions.getType().equals("PhotoQuestion")) {
//                localAnswer = new Answers(localRecordID, (int) dbAdapter.getMaxID(), questions.getId(), "", tsLong, "PhotoQuestion", fname);
//                if (!dbAdapter.doesAnswerExist(questions.getId(), (int) dbAdapter.getMaxID(), localRecordID)) {
//                    dbAdapter.insertDataAnswersTable(localAnswer);
//                }

            } else if (questions.getType().equals("RadioQuestion")) {
                localAnswer = new Answers(localRecordID, currentResponseId, questions.getId(), "", tsLong, "RadioQuestion");
                if (!dbAdapter.doesAnswerExist(questions.getId(), currentResponseId, localRecordID)) {
                    dbAdapter.insertDataAnswersTable(localAnswer);
                }
            } else if (questions.getType().equals("DropDownQuestion")) {
                localAnswer = new Answers(localRecordID, currentResponseId, questions.getId(), "", tsLong, "DropDownQuestion");
                if (!dbAdapter.doesAnswerExist(questions.getId(), currentResponseId, localRecordID)) {
                    dbAdapter.insertDataAnswersTable(localAnswer);
                }
            } else if (questions.getType().equals("MultiChoiceQuestion")) {
                localAnswer = new Answers(localRecordID, currentResponseId, questions.getId(), "", tsLong, "MultiChoiceQuestion");
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

        if ((qu.getType().equals("SingleLineQuestion")) || (qu.getType().equals("MultilineQuestion")) || (qu.getType().equals("NumericQuestion"))) {
            answer = (EditText) findViewById(qu.getId() + IntentConstants.VIEW_CONSTANT + localRecordId);
            answer.setText(dbAdapter.getAnswer(responseId, qu.getId(), localRecordId));
        } else if (qu.getType().equals("DateQuestion")) {
            dateText = (TextView) findViewById(qu.getId() + IntentConstants.VIEW_CONSTANT + localRecordId);
            dateText.setText(dbAdapter.getAnswer(responseId, qu.getId(), localRecordId));
        } else if (qu.getType().equals("RatingQuestion")) {

            ratingBar = (RatingBar) findViewById(qu.getId() + IntentConstants.VIEW_CONSTANT + localRecordId);
            dbAdapter.getAnswer(responseId, qu.getId(), localRecordId);
            try {
                float f = Float.parseFloat((dbAdapter.getAnswer(responseId, qu.getId(), localRecordId)));
                ratingBar.setRating(f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (qu.getType().equals("PhotoQuestion")) {
            String localfname = dbAdapter.getImage(responseId, qu.getId(), localRecordId);
            if (!localfname.equals("")) {
                Bitmap b = loadImageFromStorage(Environment.getExternalStorageDirectory().toString() + "/saved_images", localfname);
                findViewById(qu.getId() + IntentConstants.VIEW_CONSTANT_FOR_PHOTO3 + localRecordId).setVisibility(View.VISIBLE);
                findViewById(qu.getId() + IntentConstants.VIEW_CONSTANT_FOR_PHOTO4 + localRecordId).setVisibility(View.GONE);
                ((ImageView) findViewById(qu.getId() + IntentConstants.VIEW_CONSTANT_FOR_PHOTO2 + localRecordId)).setImageBitmap(b);
            }
        } else if (qu.getType().equals("MultiChoiceQuestion")) {


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

                //list2.get(i);
                CheckBox checkBox = (CheckBox) findViewById(optionId);

                checkBox.setChecked(true);
                for (int k = 0; k < options.size(); k++) {
                    if (options.get(k).getId() == list2.get(i)) {
                        Options options1 = options.get(k);
                        if (options1.getQuestions().size() > 0) {
                            for (int l = 0; l < options1.getQuestions().size(); l++) {
                                buildLayout(options1.getQuestions().get(l), false, localRecordId, defaultParentIndex);
                                Log.e("TAG", "CHECK BOX CALL");
                            }
                        }
                    }
                }
            }

        } else if (qu.getType().equals("RadioQuestion")) {

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

        } else if (qu.getType().equals("DropDownQuestion")) {
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
//                            spinner = (Spinner) findViewById(qu.getId() + IntentConstants.VIEW_CONSTANT + localRecordId);
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
        if ((type.equals("SingleLineQuestion")) || (type.equals("MultilineQuestion") || (type.equals("NumericQuestion")))) {


            if (((EditText) findViewById(qId + IntentConstants.VIEW_CONSTANT + recordId)).getText().toString().trim().equals("")) {
                return false;
            } else
                return true;
        }
        if ((type.equals("DateQuestion"))) {

            if (dbAdapter.doesAnswerExistAsNonNull(qId, currentResponseId, recordId).equals("")) {
                return false;
            } else
                return true;
        }
        if ((type.equals("RatingQuestion"))) {
            if (dbAdapter.doesAnswerExistAsNonNull(qId, currentResponseId, recordId).equals("0.0")) {
                return false;

            } else return true;
        }


        //this wil check questions having answers in choices tables have answers or not
        if (type.equals("RadioQuestion") || type.equals("MultiChoiceQuestion")) {
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

        if (type.equals("DropDownQuestion")) {
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
        if (type.equals("PhotoQuestion")) {

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
    public void showDialog() {
        final Dialog dialog = new Dialog(NewResponseActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //before
        dialog.setContentView(R.layout.mandatory_question_dialog);
        dialog.show();
        Button button = (Button) dialog.findViewById(R.id.okay);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

            tsLong = System.currentTimeMillis() / 1000;
            Answers localAnswer = new Answers(localRecordID, (int) dbAdapter.getMaxID(), localQuestions.getId(), dateText.getText().toString(), tsLong);
            saveAnswerIntoDB(new TagModel(localQuestions.getId(), localRecordID), localAnswer);

        }

    }

}