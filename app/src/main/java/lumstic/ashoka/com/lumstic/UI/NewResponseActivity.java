package lumstic.ashoka.com.lumstic.UI;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import android.widget.Toast;

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
import lumstic.ashoka.com.lumstic.Models.Options;
import lumstic.ashoka.com.lumstic.Models.Questions;
import lumstic.ashoka.com.lumstic.Models.Surveys;
import lumstic.ashoka.com.lumstic.R;
import lumstic.ashoka.com.lumstic.Utils.IntentConstants;
import lumstic.ashoka.com.lumstic.Utils.LumsticApp;

public class NewResponseActivity extends Activity {
    DBAdapter dbAdapter;
    ActionBar actionBar;
    private ArrayList<Integer> types = null;
    private ArrayList<String> stringTypes = null;
    private List<Questions> questionsList;
    private List<Categories> categoriesList;
    private List<Questions> nestedQuestions;
    private List<Questions> nestedQuestionList;
    private boolean proceed = true;
    private boolean hint = true;
    private boolean checked = false;
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
    private Questions universalQuestion;
    private Surveys surveys;
    private Answers answers;
    private Categories currentCategory;
    private Questions dateQuestion;
    private Bitmap photo = null;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_response);
        //action bar attributes
        setActionbar();
        //views decleration
        setViews();
        //layout inflater initialization
        inflater = getLayoutInflater();
        dbAdapter = new DBAdapter(NewResponseActivity.this);
        lumsticApp = (LumsticApp) getApplication();
        //decleration of list items
        stringTypes = new ArrayList<String>();
        types = new ArrayList<Integer>();
        nestedQuestions = new ArrayList<Questions>();
        questionsList = new ArrayList<Questions>();
        categoriesList = new ArrayList<Categories>();
        nestedQuestionList = new ArrayList<>();
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
        //get appt response id
        getResponseId();
        //getTotalCountOfQuestionAndCategory
        getCategoryAndQuestionsCount(surveys);
        //sort order of questions and category,type has order number stored and stringtypes has question or category
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

        try {
            answer.clearFocus();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (universalQuestion.getType().equals("SingleLineQuestion") || (universalQuestion.getType().equals("MultilineQuestion")) || (universalQuestion.getType().equals("NumericQuestion"))) {
            addAnswer(universalQuestion);
        }
        if (checkMandatory(nestedQuestionList)) {
            dbAdapter.UpldateCompleteResponse(currentResponseId, questionsList.get(0).getSurveyId());
            Intent intent = new Intent(NewResponseActivity.this, SurveyDetailsActivity.class);
            intent.putExtra(IntentConstants.SURVEY, (java.io.Serializable) surveys);
            startActivity(intent);
            finish();
        }
    }


    public void buildFirstQuestion() {
        //build from questions
        for (int j = 0; j < questionsList.size(); j++) {
            if (questionsList.get(j).getOrderNumber() == types.get(0)) {
                Questions cq = questionsList.get(j);
                counterButton.setText("1 out of " + totalQuestionCount);
                buildLayout(cq);
                checkForAnswer(cq, currentResponseId);
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
                    checkForAnswer(currentCategory.getQuestionsList().get(k), currentResponseId);
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

    public TextView createQuestionTitle(Questions ques) {
        TextView questionTextSingleLine = new TextView(this);
        questionTextSingleLine.setTextSize(20);
        questionTextSingleLine.setTextColor(getResources().getColor(R.color.text_color));
        questionTextSingleLine.setPadding(0, 0, 0, 16);

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

    public void buildLayout(final Questions ques) {

        //to adjust mark as complete position at last of the layout
        if (markAsComplete.getVisibility() == View.VISIBLE)
            fieldContainer.removeView(markAsComplete);
        nestedQuestionList.add(ques);
        universalQuestion = ques;


        //if question is single line question
        if (ques.getType().equals("SingleLineQuestion")) {
            LinearLayout nestedContainer = createNestedContainer();
            TextView questionTextSingleLine = createQuestionTitle(ques);
            nestedContainer.addView(questionTextSingleLine);
            nestedContainer.setId(ques.getId());
            nestedContainer.addView(inflater.inflate(R.layout.answer_single_line, null));
            nestedContainer.setTag(ques);
            fieldContainer.addView(nestedContainer);
            answer = (EditText) findViewById(R.id.answer_text);
            answer.setId(ques.getId() + IntentConstants.VIEW_CONSTANT + recordId);
            hideKeypad(answer);

            answer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    if (b) {
                        answer = (EditText) view;
                    }
                    if (!b) {
                        answer = (EditText) view;
                        Answers answers = new Answers();
                        answers.setQuestion_id(ques.getId());
                        answers.setResponseId(currentResponseId);

                        tsLong = System.currentTimeMillis() / 1000;
                        answers.setUpdated_at(tsLong);
                        answers.setContent(answer.getText().toString());


                        if (!dbAdapter.doesAnswerExist(ques.getId(), currentResponseId)) {
                            dbAdapter.insertDataAnswersTable(answers);
                        }
                        if (dbAdapter.doesAnswerExist(ques.getId(), currentResponseId)) {
                            dbAdapter.deleteFromAnswerTable(ques.getId(), currentResponseId);
                            dbAdapter.insertDataAnswersTable(answers);

                        }


                    }
                }
            });

            try {
                checkForAnswer(ques, currentResponseId);
            } catch (Exception e) {
                e.printStackTrace();
                checkHint();
            }
        }


        if (ques.getType().contains("MultilineQuestion")) {
            LinearLayout nestedContainer = createNestedContainer();
            TextView questionTextSingleLine = createQuestionTitle(ques);
            nestedContainer.addView(questionTextSingleLine);
            nestedContainer.setId(ques.getId());
            nestedContainer.addView(inflater.inflate(R.layout.answer_multi_line, null));
            nestedContainer.setId(ques.getId());
            nestedContainer.setTag(ques);
            fieldContainer.addView(nestedContainer);
            answer = (EditText) findViewById(R.id.answer_text);
            answer.setId(ques.getId() + IntentConstants.VIEW_CONSTANT + recordId);
            answer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    if (b) {
                        answer = (EditText) view;
                    }

                    if (!b) {

                        Answers answers = new Answers();
                        answers.setQuestion_id(ques.getId());
                        answers.setResponseId(currentResponseId);
                        tsLong = System.currentTimeMillis() / 1000;

                        answers.setUpdated_at(tsLong);
                        answers.setContent(answer.getText().toString());
                        if (!dbAdapter.doesAnswerExist(ques.getId(), currentResponseId))
                            dbAdapter.insertDataAnswersTable(answers);

                        //Toast.makeText(NewResponseActivity.this,"saved",Toast.LENGTH_LONG).show();
                        if (dbAdapter.doesAnswerExist(ques.getId(), currentResponseId)) {
                            dbAdapter.deleteFromAnswerTable(ques.getId(), currentResponseId);
                            dbAdapter.insertDataAnswersTable(answers);

                        }

                    }
                }
            });


            try {
                checkForAnswer(ques, currentResponseId);
            } catch (Exception e) {
                e.printStackTrace();
                checkHint();
            }
            checkHint();

        }

        //create layout for dropdown questions
        if (ques.getType().contains("DropDownQuestion")) {
            nestedQuestions.add(ques);
            if (!dbAdapter.doesAnswerExist(ques.getId(), currentResponseId)) {
                addAnswer(ques);
            }
            LinearLayout nestedContainer = createNestedContainer();
            TextView questionTextSingleLine = createQuestionTitle(ques);
            nestedContainer.addView(questionTextSingleLine);
            nestedContainer.setId(ques.getId());
            nestedContainer.setTag(ques);
            fieldContainer.addView(nestedContainer);
            spinner = new Spinner(NewResponseActivity.this);
            spinner = (Spinner) getLayoutInflater().inflate(R.layout.answer_dropdown, null);
            nestedContainer.addView(spinner);
            spinner.setTag(order);

            List<DropDown> dropDowns= new ArrayList<>();
            dropDowns.add(new DropDown(order,"Select one"));

            for (int i = 0; i < ques.getOptions().size(); i++) {
                dropDowns.add(new DropDown(order,ques.getOptions().get(i).getContent()));

            }

            DropDownAdapter dropDownAdapter= new DropDownAdapter(NewResponseActivity.this,dropDowns);
            spinner.setAdapter(dropDownAdapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {



                   order= view.findViewById(R.id.spinner_item).getTag().toString();
                    try {
                        if (i == 0) {
                            //  Toast.makeText(NewResponseActivity.this, "working", Toast.LENGTH_SHORT).show();
                            addOptionToDataBase(null, ques);
                            nestedQuestionList.clear();
                            nestedQuestionList.add(ques);
                            removeOthersFromDataBase(null, ques);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (i != 0) {
                        //add option selected to database table choices table
                        Options options = ques.getOptions().get(i - 1);
                        addOptionToDataBase(options, ques);
                        nestedQuestionList.clear();
                        nestedQuestionList.add(ques);
                        removeOthersFromDataBase(options, ques);

                        String temp=order;
                        //create nested questions
                        if (options.getQuestions().size() > 0) {
                            for (int j = 0; j < options.getQuestions().size(); j++) {
                                order=temp+Integer.toString(0 + 1);
                                buildLayout(options.getQuestions().get(j));
                                checkForAnswer(options.getQuestions().get(j), currentResponseId);
                            }
                        }

                        //create nested categories
                        if (options.getCategories().size() > 0) {
                            for (int j = 0; j < options.getCategories().size(); j++) {
                                buildCategoryLayout(options.getCategories().get(j));
                            }
                        }


                        //remove views from non selected nested categgories and questions
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
        }


        //create layout for check box questions
        if (ques.getType().contains("MultiChoiceQuestion")) {

            nestedQuestions.add(ques);
            if (!dbAdapter.doesAnswerExist(ques.getId(), currentResponseId)) {
                addAnswer(ques);

            }

            LinearLayout nestedContainer = createNestedContainer();
            TextView questionTextSingleLine = createQuestionTitle(ques);
            nestedContainer.addView(questionTextSingleLine);
            nestedContainer.setId(ques.getId());
            nestedContainer.setTag(ques);

            final LinearLayout ll = new LinearLayout(this);
            ll.setTag(order);
            ll.setOrientation(LinearLayout.VERTICAL);
            nestedContainer.addView(ll);
            for (int i = 0; i < ques.getOptions().size(); i++) {
                final CheckBox checkBox = new CheckBox(this);
                ll.addView(checkBox);
                checkBox.setId(ques.getOptions().get(i).getId());
                checkBox.setText(ques.getOptions().get(i).getContent());
                checkBox.setTextSize(16);
                checkBox.setTextColor(getResources().getColor(R.color.text_color));
                checkBox.setText(ques.getOptions().get(i).getContent());
                checkBox.setTag(ques.getOptions().get(i));


                checkBox.setTag(R.string.app_name, ll.getTag());

                checkBox.setButtonDrawable(R.drawable.custom_checkbox);
                checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (ques.getParentId() == 0) {

                        }

                        checked = true;
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
                                    tempOrder =  findViewById(view.getId()).getTag(R.string.app_name).toString();
                                order = "";
                                order = tempOrder + ".";
                            }


                            order = tempOrder + character + ".";
                            addOptionToDataBase(options, ques);
                            nestedQuestionList.clear();
                            nestedQuestionList.add(ques);


                            //create layout for nested question of check box questions
                            try {
                                if (options.getQuestions().size() > 0) {
                                    for (int i = 0; i < options.getQuestions().size(); i++) {
                                        order = order + Integer.toString(0 + 1);
                                        buildLayout(options.getQuestions().get(i));
                                        checkForAnswer(options.getQuestions().get(i), currentResponseId);
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
                            removeOptionFromDataBase(options, ques);
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
            fieldContainer.addView(nestedContainer);
            checkHint();
        }

        //check for numeric question
        if (ques.getType().contains("NumericQuestion")) {
            LinearLayout nestedContainer = createNestedContainer();
            TextView questionTextSingleLine = createQuestionTitle(ques);
            nestedContainer.addView(questionTextSingleLine);
            nestedContainer.addView(inflater.inflate(R.layout.answer_numeric, null));
            nestedContainer.setId(ques.getId());
            nestedContainer.setTag(ques);
            fieldContainer.addView(nestedContainer);
            answer = (EditText) findViewById(R.id.answer_text);
            answer.setId(ques.getId() + IntentConstants.VIEW_CONSTANT + recordId);
            if (ques.getMaxVlue() != 0 && ques.getMinValue() != 0)
                answer.setHint("Between  " + ques.getMinValue() + " to " + ques.getMaxVlue());
            answer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                public void onFocusChange(View view, boolean b) {
                    if (b) {
                        answer = (EditText) view;
                    }
                    if (!b) {
                        //add date to database
                        Answers answers = new Answers();
                        answers.setQuestion_id(ques.getId());
                        answers.setResponseId(currentResponseId);
                        tsLong = System.currentTimeMillis() / 1000;
                        answers.setUpdated_at(tsLong);
                        answers.setContent(answer.getText().toString());
                        if (!dbAdapter.doesAnswerExist(ques.getId(), currentResponseId))
                            dbAdapter.insertDataAnswersTable(answers);
                        if (dbAdapter.doesAnswerExist(ques.getId(), currentResponseId)) {
                            dbAdapter.deleteFromAnswerTable(ques.getId(), currentResponseId);
                            dbAdapter.insertDataAnswersTable(answers);

                        }
                    }
                }
            });
            try {
                checkForAnswer(ques, currentResponseId);
            } catch (Exception e) {
                e.printStackTrace();
                checkHint();
            }
            checkHint();
        }

        //check for date question
        if (ques.getType().contains("DateQuestion")) {
            dateQuestion = ques;
            LinearLayout nestedContainer = createNestedContainer();
            TextView questionTextSingleLine = createQuestionTitle(ques);
            nestedContainer.addView(questionTextSingleLine);
            nestedContainer.addView(inflater.inflate(R.layout.answer_date_picker, null));
            nestedContainer.setId(ques.getId());
            nestedContainer.setTag(ques);
            fieldContainer.addView(nestedContainer);

            dateText = (TextView) findViewById(R.id.answer_text_date);
            dateText.setText("dd.yy.mm");
            dateText.setId(ques.getId() + IntentConstants.VIEW_CONSTANT + recordId);

            dateText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dateQuestion = ques;

                    Calendar c = Calendar.getInstance();
                    int mYear = c.get(Calendar.YEAR);
                    int mMonth = c.get(Calendar.MONTH);
                    int mDay = c.get(Calendar.DAY_OF_MONTH);
                    DatePickerDialog dialog = new DatePickerDialog(NewResponseActivity.this,
                            new mDateSetListener(), mYear, mMonth, mDay);
                    dialog.show();
                }
            });
            try {
                checkForAnswer(ques, currentResponseId);
            } catch (Exception e) {
                e.printStackTrace();
                checkHint();
            }
        }

        //for radio question check
        if (ques.getType().contains("RadioQuestion")) {


            nestedQuestions.add(ques);
            if (!dbAdapter.doesAnswerExist(ques.getId(), currentResponseId)) {
                addAnswer(ques);
            }

            LinearLayout nestedContainer = createNestedContainer();
            TextView questionTextSingleLine = createQuestionTitle(ques);
            nestedContainer.addView(questionTextSingleLine);
            nestedContainer.setId(ques.getId());
            nestedContainer.setTag(ques);

            //create new radio group
            radioGroup = new RadioGroup(this);
            radioGroup.setOrientation(RadioGroup.VERTICAL);
            nestedContainer.addView(radioGroup);
            radioGroup.setTag(order);

            for (int i = 0; i < ques.getOptions().size(); i++) {

                final RadioButton radioButton = new RadioButton(this);
                radioGroup.addView(radioButton);
                radioButton.setId(ques.getOptions().get(i).getId());
                radioButton.setTextSize(16);
                radioButton.setTextColor(getResources().getColor(R.color.text_color));
                radioButton.setText(ques.getOptions().get(i).getContent());
                radioButton.setTag(ques.getOptions().get(i));


                radioButton.setTag(R.string.app_name, radioGroup.getTag());


                radioButton.setButtonDrawable(R.drawable.custom_radio_button);

                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
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
                        addOptionToDataBase(options, ques);
                        nestedQuestionList.clear();
                        nestedQuestionList.add(ques);
                        //remove others from database
                        removeOthersFromDataBase(options, ques);
                        String tmp = order;
                        //decide where to make questions for nested question
                        if (options.getQuestions().size() > 0) {
                            for (int i = 0; i < options.getQuestions().size(); i++) {
                                order = tmp + Integer.toString(i + 1);
                                buildLayout(options.getQuestions().get(i));
                                checkForAnswer(options.getQuestions().get(i), currentResponseId);
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
            fieldContainer.addView(nestedContainer);
            checkHint();


        }
        if (ques.getType().equals("RatingQuestion")) {
            nestedQuestions.add(ques);
            LinearLayout nestedContainer = createNestedContainer();
            TextView questionTextSingleLine = createQuestionTitle(ques);

            nestedContainer.addView(questionTextSingleLine);
            nestedContainer.addView(inflater.inflate(R.layout.answer_rating, null));
            nestedContainer.setId(ques.getId());

            nestedContainer.setTag(ques);
            fieldContainer.addView(nestedContainer);
            ratingBar = (RatingBar) findViewById(R.id.ratingBar);
            ratingBar.setId(ques.getId() + IntentConstants.VIEW_CONSTANT + recordId);
            ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                //answer saved on rating changed
                @Override
                public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                    if (dbAdapter.doesAnswerExist(ques.getId(), currentResponseId)) {
                        dbAdapter.deleteRatingAnswer(ques.getId(), currentResponseId);
                    }
                    Answers answers = new Answers();
                    answers.setResponseId((int) dbAdapter.getMaxID());
                    answers.setQuestion_id(ques.getId());
                    tsLong = System.currentTimeMillis() / 1000;
                    answers.setUpdated_at(tsLong);
                    answers.setContent(String.valueOf(v));
                    if (!dbAdapter.doesAnswerExist(ques.getId(), currentResponseId)) {
                        dbAdapter.insertDataAnswersTable(answers);
                        //Toast.makeText(NewResponseActivity.this, "rating saved", Toast.LENGTH_SHORT).show();
                    }
                }
            });


            checkHint();
        }

        //for image question
        if (ques.getType().equals("PhotoQuestion")) {
            nestedQuestionList.add(ques);
            LinearLayout nestedContainer = createNestedContainer();
            TextView questionTextSingleLine = createQuestionTitle(ques);
            nestedContainer.addView(questionTextSingleLine);
            nestedContainer.setId(ques.getId());
            nestedContainer.addView(inflater.inflate(R.layout.answer_image_picker, null));
            fieldContainer.addView(nestedContainer);

            Button lButton = (Button) findViewById(R.id.answer_text_image);
            lButton.setId(ques.getId() + IntentConstants.VIEW_CONSTANT + recordId);

            deleteImageRelativeLayout = (RelativeLayout) findViewById(R.id.image_container);

            imageViewPhotoQuestion = (ImageView) findViewById(R.id.image);

            imageViewPhotoQuestion.setId(ques.getId());
            imageContainer = (RelativeLayout) findViewById(R.id.image_container);
            lButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent cameraIntent = new Intent(
                            android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }
            });
            deleteImageRelativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imageContainer.setVisibility(View.GONE);
                    dbAdapter.deleteImagePath(currentResponseId, ques.getId());

                }
            });
        }
        if (markAsComplete.getVisibility() == View.VISIBLE) {

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


    //on next ic clicked
    public void onNextClick() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(answer.getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }


        //add answers not saved
        if ((universalQuestion.getType().equals("SingleLineQuestion")) || ((universalQuestion.getType().equals("MultilineQuestion")) || ((universalQuestion.getType().equals("DateQuestion")) || ((universalQuestion.getType().equals("NumericQuestion")))))) {
            addAnswer(universalQuestion);
        } else {
            if (!dbAdapter.doesAnswerExist(universalQuestion.getId(), currentResponseId))
                addAnswer(universalQuestion);
        }

        try {
            if (universalQuestion.getType().equals("NumericQuestion")) {
                int num = Integer.parseInt(answer.getText().toString());
                if (universalQuestion.getMinValue() != 0 && universalQuestion.getMaxVlue() != 0) {
                    if (num < universalQuestion.getMinValue() || num > universalQuestion.getMaxVlue()) {

                        numberlimitOk = false;
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

                    } else numberlimitOk = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //check for mandatory questions
        boolean x = checkMandatory(nestedQuestionList);
        if (x) {

            if (numberlimitOk) {
                try {
                    answer.clearFocus();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                nestedQuestionList.clear();

                //set next and previous buttons
                if (questionCounter < totalQuestionCount - 1) {
                    previousQuestion.setBackgroundColor(getResources().getColor(R.color.login_button_color));
                    previousQuestion.setTextColor(getResources().getColor(R.color.white));
                    previousQuestion.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_arrow_back, 0, 0, 0);
                    nestedQuestions.clear();
                    nestedQuestions.clear();
                    fieldContainer.removeAllViews();
                    questionCounter++;

                    counterButton.setText(questionCounter + 1 + " out of " + totalQuestionCount);
//for category type questions
                    for (int j = 0; j < categoriesList.size(); j++) {
                        if (categoriesList.get(j).getOrderNumber() == types.get(questionCounter)) {
                            currentCategory = categoriesList.get(j);

                            //for multirecord questions

                            buildCategoryLayout(currentCategory);
                        }
                    }

                    //for general questions
                    for (int j = 0; j < questionsList.size(); j++) {
                        if (questionsList.get(j).getOrderNumber() == types.get(questionCounter)) {
                            Questions cq = questionsList.get(j);
                            buildLayout(cq);
                            checkForAnswer(cq, currentResponseId);
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
        }
    }

    //on back is clicked
    public void onBackClicked() {
        nestedQuestionList.clear();
        markAsComplete.setVisibility(View.GONE);

        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(answer.getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (!universalQuestion.getType().equals("PhotoQuestion")) {
            if (questionCounter == totalQuestionCount - 1) {
                addAnswer(universalQuestion);
            }
        }
        if ((universalQuestion.getType().equals("SingleLineQuestion")) || ((universalQuestion.getType().equals("MultilineQuestion")) || ((universalQuestion.getType().equals("DateQuestion")) || ((universalQuestion.getType().equals("NumericQuestion")))))) {
            addAnswer(universalQuestion);
        } else {
            if (!dbAdapter.doesAnswerExist(universalQuestion.getId(), currentResponseId))
                addAnswer(universalQuestion);
        }


        if (questionCounter != 0) {
            try {
                answer.clearFocus();
            } catch (Exception e) {
                e.printStackTrace();
            }
            nestedQuestions.clear();
            fieldContainer.removeAllViews();
            //change count
            questionCounter--;


            counterButton.setText(questionCounter + 1 + " out of " + totalQuestionCount);
            for (int j = 0; j < categoriesList.size(); j++) {
                if (categoriesList.get(j).getOrderNumber() == types.get(questionCounter)) {
                    Categories currentCategory = categoriesList.get(j);
                    buildCategoryLayout(currentCategory);
//                    for (int k = 0; k < currentCategory.getQuestionsList().size(); k++) {
//                        checkForAnswer(currentCategory.getQuestionsList().get(k), currentResponseId);
//
//                    }
                    break;
                }
            }
            //build question layout
            for (int j = 0; j < questionsList.size(); j++) {
                if (questionsList.get(j).getOrderNumber() == types.get(questionCounter)) {
                    Questions cq = questionsList.get(j);
                    buildLayout(cq);
                    checkForAnswer(cq, currentResponseId);
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
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayUseLogoEnabled(true);
        }
    }

    //get data from camera activity
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            photo = (Bitmap) data.getExtras().get("data");
            deleteImageRelativeLayout.setVisibility(View.VISIBLE);
            imageViewPhotoQuestion.setImageBitmap(photo);
            SaveImage(photo);
            addAnswer(universalQuestion);
        }
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
    private void loadImageFromStorage(String path, String fileName) {
        try {
            File f = new File(path, fileName);
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
            imageViewPhotoQuestion.setImageBitmap(b);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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


        List<Questions> questionses = new ArrayList<>();
        Questions questions;


        for (int j = 0; j < categories.getQuestionsList().size(); j++) {
            categoryQuestionCounter++;
            buildLayout(categories.getQuestionsList().get(j));
            checkForAnswer(categories.getQuestionsList().get(j), currentResponseId);
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
                nestedQuestions.remove(options.getQuestions().get(i));
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

    public void addAnswer(Questions questions) {

        if (questions.getType().equals("SingleLineQuestion")) {

            Answers answers = new Answers();
            answers.setQuestion_id(questions.getId());
            answers.setResponseId(currentResponseId);
            answers.setContent(answer.getText().toString());
            tsLong = System.currentTimeMillis() / 1000;
            answers.setUpdated_at(tsLong);


            if (!dbAdapter.doesAnswerExist(questions.getId(), currentResponseId)) {
                dbAdapter.insertDataAnswersTable(answers);
            }
            if (dbAdapter.doesAnswerExist(questions.getId(), currentResponseId)) {
                dbAdapter.deleteFromAnswerTable(questions.getId(), currentResponseId);
                dbAdapter.insertDataAnswersTable(answers);

            }

            ;
        }

        if (questions.getType().equals("MultilineQuestion")) {
            Answers answers = new Answers();
            answers.setQuestion_id(questions.getId());
            answers.setResponseId((int) dbAdapter.getMaxID());
            answers.setContent(answer.getText().toString());
            tsLong = System.currentTimeMillis() / 1000;
            answers.setUpdated_at(tsLong);
            if (!dbAdapter.doesAnswerExist(questions.getId(), currentResponseId))
                dbAdapter.insertDataAnswersTable(answers);
            if (dbAdapter.doesAnswerExist(questions.getId(), currentResponseId)) {
                dbAdapter.deleteFromAnswerTable(questions.getId(), currentResponseId);
                dbAdapter.insertDataAnswersTable(answers);

            }
        }

        if (questions.getType().equals("NumericQuestion")) {
            Answers answers = new Answers();
            answers.setResponseId((int) dbAdapter.getMaxID());
            answers.setQuestion_id(questions.getId());
            tsLong = System.currentTimeMillis() / 1000;
            answers.setUpdated_at(tsLong);
            answers.setContent(answer.getText().toString());
            if (!dbAdapter.doesAnswerExist(questions.getId(), currentResponseId))
                dbAdapter.insertDataAnswersTable(answers);
            if (dbAdapter.doesAnswerExist(questions.getId(), currentResponseId)) {
                dbAdapter.deleteFromAnswerTable(questions.getId(), currentResponseId);
                dbAdapter.insertDataAnswersTable(answers);

            }
        }


        if (questions.getType().equals("DateQuestion")) {
            Answers answers = new Answers();
            answers.setResponseId((int) dbAdapter.getMaxID());
            answers.setQuestion_id(questions.getId());
            tsLong = System.currentTimeMillis() / 1000;
            answers.setUpdated_at(tsLong);
            answers.setContent(dateText.getText().toString());
            if (!dbAdapter.doesAnswerExist(questions.getId(), currentResponseId))
                dbAdapter.insertDataAnswersTable(answers);
            if (dbAdapter.doesAnswerExist(questions.getId(), currentResponseId)) {
                dbAdapter.deleteFromAnswerTable(questions.getId(), currentResponseId);
                dbAdapter.insertDataAnswersTable(answers);

            }
        }


        if (questions.getType().equals("RatingQuestion")) {

            if (dbAdapter.doesAnswerExist(questions.getId(), currentResponseId)) {
                dbAdapter.deleteRatingAnswer(questions.getId(), currentResponseId);
            }
            Answers answers = new Answers();
            answers.setResponseId((int) dbAdapter.getMaxID());
            answers.setQuestion_id(questions.getId());
            tsLong = System.currentTimeMillis() / 1000;
            answers.setUpdated_at(tsLong);
            answers.setContent(String.valueOf(ratingBar.getRating()));
            if (!dbAdapter.doesAnswerExist(questions.getId(), currentResponseId))
                dbAdapter.insertDataAnswersTable(answers);
            if (dbAdapter.doesAnswerExist(questions.getId(), currentResponseId)) {
                dbAdapter.deleteFromAnswerTable(questions.getId(), currentResponseId);
                dbAdapter.insertDataAnswersTable(answers);

            }
        }


        if (questions.getType().equals("PhotoQuestion")) {

            Answers answers = new Answers();
            answers.setResponseId((int) dbAdapter.getMaxID());
            answers.setQuestion_id(questions.getId());
            answers.setType("PhotoQuestion");
            answers.setImage(fname);
            tsLong = System.currentTimeMillis() / 1000;
            answers.setUpdated_at(tsLong);
            long x = dbAdapter.insertDataAnswersTable(answers);

        }

        if (questions.getType().equals("RadioQuestion")) {
            Answers answers = new Answers();
            answers.setQuestion_id(questions.getId());
            answers.setResponseId(currentResponseId);
            answers.setType("RadioQuestion");
            tsLong = System.currentTimeMillis() / 1000;
            answers.setUpdated_at(tsLong);
            answers.setContent("");
            long x = dbAdapter.insertDataAnswersTable(answers);
        }


        if (questions.getType().equals("DropDownQuestion")) {
            Answers answers = new Answers();
            answers.setType("DropDownQuestion");
            answers.setQuestion_id(questions.getId());
            answers.setResponseId(currentResponseId);
            tsLong = System.currentTimeMillis() / 1000;
            answers.setUpdated_at(tsLong);
            answers.setContent("");
            long x = dbAdapter.insertDataAnswersTable(answers);
        }

        if (questions.getType().equals("MultiChoiceQuestion")) {
            Answers answers = new Answers();
            answers.setType("MultiChoiceQuestion");
            answers.setQuestion_id(questions.getId());
            answers.setResponseId(currentResponseId);
            tsLong = System.currentTimeMillis() / 1000;
            answers.setUpdated_at(tsLong);
            answers.setContent("");
            long x = dbAdapter.insertDataAnswersTable(answers);
        }

    }

    //this will check if the ansdwer is saved and can be retrieved from the tables
    public void checkForAnswer(Questions qu, int responseId) {

        if ((qu.getType().equals("SingleLineQuestion")) || (qu.getType().equals("MultilineQuestion")) || (qu.getType().equals("NumericQuestion"))) {
            answer = (EditText) findViewById(qu.getId() + IntentConstants.VIEW_CONSTANT + recordId);
            answer.setText(dbAdapter.getAnswer(responseId, qu.getId()));
        }


        if (qu.getType().equals("DateQuestion")) {
            dateText = (TextView) findViewById(qu.getId() + IntentConstants.VIEW_CONSTANT + recordId);
            dateText.setText(dbAdapter.getAnswer(responseId, qu.getId()));
        }

        if (qu.getType().equals("RatingQuestion")) {

            ratingBar = (RatingBar) findViewById(qu.getId() + IntentConstants.VIEW_CONSTANT + recordId);
            dbAdapter.getAnswer(responseId, qu.getId());
            //Integer.parseInt(dbAdapter.getAnswer(responseId, qu.getId()));
            try {
                float f = Float.parseFloat((dbAdapter.getAnswer(responseId, qu.getId())));
                ratingBar.setRating(f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        if (qu.getType().equals("PhotoQuestion")) {
            if (!dbAdapter.getImage(responseId, qu.getId()).equals("")) {
                deleteImageRelativeLayout.setVisibility(View.VISIBLE);
                loadImageFromStorage(Environment.getExternalStorageDirectory().toString() + "/saved_images", dbAdapter.getImage(responseId, qu.getId()));
                Toast.makeText(NewResponseActivity.this, "image is" + qu.getId(), Toast.LENGTH_SHORT).show();
            }
        }


        //to remake multi choice view
        if (qu.getType().equals("MultiChoiceQuestion")) {
            List<Options> options = new ArrayList<Options>();
            options = qu.getOptions();
            for (int i = 0; i < options.size(); i++) {
                options.get(i).getId();
            }


            List<Integer> integers = new ArrayList<Integer>();
            integers = dbAdapter.getIdFromAnswerTable(responseId, qu.getId());
            List<Integer> list2 =
                    dbAdapter.getOptionIds(integers);
            for (int i = 0; i < list2.size(); i++) {
                list2.get(i);
                CheckBox checkBox = (CheckBox) findViewById(list2.get(i));
                checkBox.setChecked(true);
                for (int k = 0; k < options.size(); k++) {
                    if (options.get(k).getId() == list2.get(i)) {
                        Options options1 = options.get(k);
                        if (options1.getQuestions().size() > 0) {
                            for (int l = 0; l < options1.getQuestions().size(); l++) {
                                buildLayout(options1.getQuestions().get(l));
                                checkForAnswer(options1.getQuestions().get(l), currentResponseId);
                            }
                        }
                    }
                }
            }
        }


        // to remake radio button view
        if (qu.getType().equals("RadioQuestion")) {
            List<Options> options = new ArrayList<Options>();
            options = qu.getOptions();
            for (int i = 0; i < options.size(); i++) {
                options.get(i).getId();
            }

            List<Integer> integers = new ArrayList<Integer>();
            integers = dbAdapter.getIdFromAnswerTable(responseId, qu.getId());


            List<Integer> list2 =
                    dbAdapter.getOptionIds(integers);

            for (int i = 0; i < list2.size(); i++) {
                list2.get(i);
                try {
                    RadioButton radioButton = (RadioButton) findViewById(list2.get(i));
                    radioButton.setChecked(true);


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }


        //to remake dropdown view
        if (qu.getType().equals("DropDownQuestion")) {
            List<Options> options = new ArrayList<Options>();
            options = qu.getOptions();
            for (int i = 0; i < options.size(); i++) {
                options.get(i).getId();
            }

            List<Integer> integers = new ArrayList<Integer>();
            integers = dbAdapter.getIdFromAnswerTable(responseId, qu.getId());


            List<Integer> list2 =
                    dbAdapter.getOptionIds(integers);


            for (int i = 0; i < list2.size(); i++) {

                for (int j = 0; j < qu.getOptions().size(); j++) {
                    if (qu.getOptions().get(j).getId() == list2.get(i)) {

                        spinner.setSelection(j + 1);

                    }

                }
            }
        }


    }

    //add record to database in case of selectedlement
    public void addOptionToDataBase(Options options, Questions qu) {

        if (options == null) {

            Choices choices1 = new Choices();
            int answerId = dbAdapter.getIdFromAnswerTable(currentResponseId, qu.getId()).get(0);
            choices1.setAnswerId(answerId);
//            choices1.setOptionId(0);
            choices1.setOption("");
            //  Toast.makeText(NewResponseActivity.this, "work" + choices1.getOption() + "option", Toast.LENGTH_SHORT).show();
            choices1.setType(qu.getType());
            dbAdapter.insertDataChoicesTable(choices1);
        }

        if (options != null) {
            Choices choices = new Choices();
            int answerId = dbAdapter.getIdFromAnswerTable(currentResponseId, qu.getId()).get(0);
            choices.setAnswerId(answerId);
            choices.setOptionId(options.getId());
            choices.setOption(options.getContent());
            choices.setType(qu.getType());
            dbAdapter.insertDataChoicesTable(choices);
        }
    }

    //this is remove option from table in case of multichoice
    public void removeOptionFromDataBase(Options options, Questions qu) {
        dbAdapter.deleteOption(options);

    }

    //this is for not selected elements of radio
    public void removeOthersFromDataBase(Options options, Questions qu) {


        //this removes the answers entry from answers table as well as choices in choices table of rest of the options
        for (int i = 0; i < qu.getOptions().size(); i++) {
            if (options.getId() != qu.getOptions().get(i).getId()) {
                dbAdapter.deleteFromChoicesTableWhereOptionId(qu.getOptions().get(i).getId());
                if (qu.getOptions().get(i).getQuestions().size() > 0) {
                    for (int j = 0; j < qu.getOptions().get(i).getQuestions().size(); j++) {
                        dbAdapter.deleteFromAnswerTable(qu.getOptions().get(i).getQuestions().get(j).getId(), currentResponseId);

                    }
                }


                //thius removes all the category questions stored in answers table
                if (qu.getOptions().get(i).getCategories().size() > 0) {
                    for (int k = 0; k < qu.getOptions().get(i).getCategories().size(); k++) {
                        for (int l = 0; l < qu.getOptions().get(i).getCategories().get(k).getQuestionsList().size(); l++) {
                            //Toast.makeText(NewResponseActivity.this,"delete "+qu.getOptions().get(i).getCategories().get(k).getQuestionsList().get(l).getId(),Toast.LENGTH_SHORT).show();
                            dbAdapter.deleteFromAnswerTable(qu.getOptions().get(i).getCategories().get(k).getQuestionsList().get(l).getId(), currentResponseId);
                        }
                    }
                }
            }
        }
    }


    //function to check if the question is mandatory or not
    //this function will also check all the questions on the page if any question is mandatory or not
    public boolean checkMandatory(List<Questions> nestedQuestionList) {

        proceed = true;
        for (int i = 0; i < nestedQuestionList.size(); i++) {

            //this will check for answers for questions having answers in answeers table
            if ((nestedQuestionList.get(i).getType().equals("SingleLineQuestion")) || (nestedQuestionList.get(i).getType().equals("MultilineQuestion") || (nestedQuestionList.get(i).getType().equals("DateQuestion")) || (nestedQuestionList.get(i).getType().equals("NumericQuestion")))) {

                if (nestedQuestionList.get(i).getMandatory() == 1) {
                    if (dbAdapter.doesAnswerExistAsNonNull(nestedQuestionList.get(i).getId(), currentResponseId).equals("")) {
                        showDialog();
                        proceed = false;
                    } else proceed = true;
                }

            }
            if ((nestedQuestionList.get(i).getType().equals("RatingQuestion"))) {
                if (nestedQuestionList.get(i).getMandatory() == 1) {
                    if (dbAdapter.doesAnswerExistAsNonNull(nestedQuestionList.get(i).getId(), currentResponseId).equals("0.0")) {
                        showDialog();
                        proceed = false;
                    } else proceed = true;
                }
            }


            //this wil check questions having answers in choices tables have answers or not
            if ((nestedQuestionList.get(i).getType().equals("RadioQuestion")) || (nestedQuestionList.get(i).getType().equals("MultiChoiceQuestion")) || ((nestedQuestionList.get(i).getType().equals("DropDownQuestion")))) {
                if (nestedQuestionList.get(i).getMandatory() == 1) {
                    int count = dbAdapter.getAnswerId(currentResponseId, nestedQuestionList.get(i).getId());
                    if (count == 0) {
                        showDialog();
                        proceed = false;
                    }

                    if (count != 0) {
                        if (dbAdapter.getChoicesCountWhereAnswerIdIs(count) == 0) {
                            showDialog();
                            proceed = false;
                        }
                    }
                }
            }

            // this wil check if photo mandatory questions have answers in te anssweers table or not
            if (nestedQuestionList.get(i).getType().equals("PhotoQuestion")) {
                if (nestedQuestionList.get(i).getMandatory() == 1) {
                    if (dbAdapter.doesImageExistAsNonNull(nestedQuestionList.get(i).getId(), currentResponseId).equals("")) {
                        showDialog();
                        proceed = false;
                    } else proceed = true;
                }
            }
        }
        return proceed;
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

    //this is a class which implements datepicker dialog and shows up on item click of date questuions
    class mDateSetListener implements DatePickerDialog.OnDateSetListener {
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            int mYear = year;
            int mMonth = monthOfYear;
            int mDay = dayOfMonth;
            dateText = (TextView) findViewById(dateQuestion.getId() + IntentConstants.VIEW_CONSTANT + recordId);
            dateText.setText(new StringBuilder().append(mYear).append("/").append(mMonth + 1).append("/").append(mDay).toString());


            Answers answers = new Answers();
            answers.setResponseId((int) dbAdapter.getMaxID());
            answers.setQuestion_id(dateQuestion.getId());
            answers.setContent(dateText.getText().toString());
            tsLong = System.currentTimeMillis() / 1000;
            answers.setUpdated_at(tsLong);
            if (!dbAdapter.doesAnswerExist(dateQuestion.getId(), currentResponseId)) {
                dbAdapter.insertDataAnswersTable(answers);
            }
            if (dbAdapter.doesAnswerExist(dateQuestion.getId(), currentResponseId)) {
                dbAdapter.deleteFromAnswerTable(dateQuestion.getId(), currentResponseId);
                dbAdapter.insertDataAnswersTable(answers);


            }

        }
    }
}