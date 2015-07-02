package lumstic.example.com.lumstic.UI;

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

import lumstic.example.com.lumstic.Adapters.DBAdapter;
import lumstic.example.com.lumstic.Models.Answers;
import lumstic.example.com.lumstic.Models.Categories;
import lumstic.example.com.lumstic.Models.Choices;
import lumstic.example.com.lumstic.Models.Options;
import lumstic.example.com.lumstic.Models.Questions;
import lumstic.example.com.lumstic.Models.Surveys;
import lumstic.example.com.lumstic.R;
import lumstic.example.com.lumstic.Utils.IntentConstants;

public class NewResponseActivity extends Activity {


    final int PIC_CROP = 2;
    List<Questions> questionsList;
    List<Categories> categoriesList;
    boolean hint = true;
    TextView dateText;
    Spinner spinner;
    RadioGroup radioGroup;
    boolean checked = false;
    Surveys surveys;
    Long tsLong = null;
    boolean proceed = true;
    RelativeLayout deleteImageRelativeLayout;
    Answers answers;

    List<Questions> nestedQuestions;

    EditText answer;
    Categories currentCategory;
    Button counterButton, markAsComplete;
    String htmlStringWithMathSymbols = "&#60";
    ActionBar actionBar;
    DBAdapter dbAdapter;
    int currentResponseId = 0;
    Questions universalQuestion;
    String fname = "";
    int categoryAndQuestionCount = 0;
    int totalQuestionCount = 0;
    Questions dateQuestion;
    List<Questions> nestedQuestionList;


    int questionCount = 0;
    int categoryCount = 0;
    LinearLayout fieldContainer;
    LayoutInflater inflater;
    int CAMERA_REQUEST = 1;
    RelativeLayout imageContainer;
    ImageView imageViewPhotoQuestion;

    Bitmap photo = null;
    int PICK_FROM_CAMERA = 1;
    int questionCounter = 0;
    List<Integer> idList;
    RatingBar ratingBar;
    int questionOrderCounter = 0;
    List<Object> objects;

    ArrayList<Integer> types = null;

    ArrayList<String> stringTypes = null;

    Button nextQuestion, previousQuestion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_response);
        actionBar = getActionBar();
        actionBar.setTitle("New Response Activity");
        actionBar.setDisplayHomeAsUpEnabled(true);
//      actionBar.setHomeAsUpIndicator(R.drawable.ic_action_ic_back);
        actionBar.setDisplayShowTitleEnabled(true);
        fieldContainer = (LinearLayout) findViewById(R.id.field_container);
        inflater = getLayoutInflater();
        nestedQuestions = new ArrayList<Questions>();
        idList = new ArrayList<Integer>();
        counterButton = (Button) findViewById(R.id.counter_button);


        dbAdapter = new DBAdapter(NewResponseActivity.this);
        questionsList = new ArrayList<Questions>();
        categoriesList = new ArrayList<Categories>();
        nestedQuestionList = new ArrayList<>();
        objects = new ArrayList<Object>();
        markAsComplete = new Button(this);
        createMarkAsComplete();
        makeMandatoryText();


        surveys = (Surveys) getIntent().getExtras().getSerializable(IntentConstants.SURVEY);



        for(int j=surveys.getQuestions().size()-1;j>=0;j--){
            if(surveys.getQuestions().get(j).getCategoryId()>0)
                surveys.getQuestions().remove(j);

        }
        for(int j=surveys.getCategories().size()-1;j>=0;j--){
            if(surveys.getCategories().get(j).getParentId()>0)
                surveys.getCategories().remove(j);

        }

        if (getIntent().hasExtra(IntentConstants.RESPONSE_ID)) {
            currentResponseId = getIntent().getIntExtra(IntentConstants.RESPONSE_ID, 0);
        }
        if (!getIntent().hasExtra(IntentConstants.RESPONSE_ID)) {
            currentResponseId = (int) dbAdapter.getMaxID();
        }

        if (surveys.getQuestions().size() > 0) {
            questionsList = surveys.getQuestions();
            questionCount = questionsList.size();
        }

        categoryAndQuestionCount = questionCount + categoryCount;

        if (surveys.getCategories().size() > 0) {
            categoriesList = surveys.getCategories();
            categoryCount = categoriesList.size();
        }


        totalQuestionCount = categoryCount + questionCount;

        stringTypes = new ArrayList<String>();
        types = new ArrayList<Integer>();


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


        for (int j = 0; j < questionsList.size(); j++) {
            if (questionsList.get(j).getOrderNumber() == types.get(0)) {
                Questions cq = questionsList.get(j);
//                questionCounter++;
                counterButton.setText("1 out of " + totalQuestionCount);
                buildLayout(cq);
                checkForAnswer(cq, currentResponseId);
                break;
            }
        }
        for (int j = 0; j < categoriesList.size(); j++) {
            if (categoriesList.get(j).getOrderNumber() == types.get(0)) {

                counterButton.setText("1 out of " + totalQuestionCount);
//                questionCounter++;


                Categories currentCategory = categoriesList.get(j);


                buildCategoryLayout(currentCategory);


                for (int k = 0; k < currentCategory.getQuestionsList().size(); k++) {
                    checkForAnswer(currentCategory.getQuestionsList().get(k), currentResponseId);

                }
                break;
            }
        }

        nextQuestion = (Button) findViewById(R.id.next_queation);
        previousQuestion = (Button) findViewById(R.id.previous_question);
        previousQuestion.setText("BACK");

        if (questionCounter + 1 == totalQuestionCount) {

            createMarkAsComplete();
            fieldContainer.addView(markAsComplete);


            nextQuestion.setTextColor(getResources().getColor(R.color.back_button_text));
            nextQuestion.setText("NEXT");
            nextQuestion.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_next_disable, 0);

            nextQuestion.setBackgroundColor(getResources().getColor(R.color.back_button_background));
        }

        markAsComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (universalQuestion.getType().equals("RadioQuestion")) {
                    if (radioGroup.getCheckedRadioButtonId() == -1) {
                        addOptionToDataBase(null, universalQuestion);
                    }
                }
                if (universalQuestion.getType().equals("MultiChoiceQuestion")) {
                    if (!checked) {
                        addOptionToDataBase(null, universalQuestion);
                    }
                }

                if (universalQuestion.getType().equals("DropDownQuestion")) {
                    if (!checked) {
                        addOptionToDataBase(null, universalQuestion);
                    }
                }


                if (universalQuestion.getType().equals("RatingQuestion")) {
                    addAnswer(universalQuestion);
                }

                if (universalQuestion.getType().equals("SingleLineQuestion")) {
                    addAnswer(universalQuestion);
                }
                if (universalQuestion.getType().equals("MultilineQuestion")) {
                    addAnswer(universalQuestion);
                }
                if (universalQuestion.getType().equals("NumericQuestion")) {
                    addAnswer(universalQuestion);
                }


                if (!universalQuestion.getType().equals("PhotoQuestion"))

                    if (!universalQuestion.getType().equals("PhotoQuestion")) {
                        if (questionCounter == totalQuestionCount - 1) {
                        }
                    }


                dbAdapter.UpldateCompleteResponse(currentResponseId, questionsList.get(0).getSurveyId());
                Intent intent = new Intent(NewResponseActivity.this, SurveyDetailsActivity.class);
                intent.putExtra(IntentConstants.SURVEY, (java.io.Serializable) surveys);
                startActivity(intent);
                finish();
            }
        });

        nextQuestion.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                onNextClick();


            }
        });

        previousQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onBackClicked();
            }
        });


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
        return super.onOptionsItemSelected(item);
    }


    public void buildLayout(final Questions ques) {
        if (markAsComplete.getVisibility() == View.VISIBLE) {
            fieldContainer.removeView(markAsComplete);
        }
        nestedQuestionList.add(ques);

        universalQuestion = ques;
        if (ques.getType().equals("SingleLineQuestion")) {

            LinearLayout nestedContainer = new LinearLayout(this);
            nestedContainer.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            layoutParams.setMargins(10,10,10,30);
            nestedContainer.setLayoutParams(layoutParams);
            TextView questionTextSingleLine = new TextView(this);
            questionTextSingleLine.setTextSize(20);
            questionTextSingleLine.setTextColor(getResources().getColor(R.color.text_color));
            questionTextSingleLine.setPadding(0, 0, 0, 16);

            if (ques.getMandatory() == 1) {
                questionTextSingleLine.setText("Q. " + (questionCounter + 1) + "   " + ques.getContent()+"  *");
                if (ques.getParentId() > 0)
                    questionTextSingleLine.setText("Q. " + "   " + ques.getContent()+"  *");
            }

            else{
                questionTextSingleLine.setText("Q. " + (questionCounter + 1) + "   " + ques.getContent());
                if (ques.getParentId() > 0)
                    questionTextSingleLine.setText("Q. " + "   " + ques.getContent());

            }




            nestedContainer.addView(questionTextSingleLine);
            nestedContainer.setId(ques.getId());



            nestedContainer.addView(inflater.inflate(R.layout.answer_single_line, null));

            nestedContainer.setTag(ques);
            idList.add(ques.getId());
            fieldContainer.addView(nestedContainer);
            answer = (EditText) findViewById(R.id.answer_text);
            answer.setId(ques.getId() + 220);


            InputMethodManager imm = (InputMethodManager) getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(answer.getWindowToken(), 0);

            answers = new Answers();
            answers.setQuestion_id(ques.getId());
            answers.setResponseId(currentResponseId);
            tsLong = System.currentTimeMillis() / 1000;
            answers.setUpdated_at(tsLong);

            answer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {

                    if (b) {

                        // answer = (EditText) view;
                        // Toast.makeText(NewResponseActivity.this, answer.getId() + "answerr id", Toast.LENGTH_SHORT).show();


                    }

                    if (!b) {

                        answer = (EditText) view;

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
        }


        if (ques.getType().contains("MultilineQuestion")) {

            LinearLayout nestedContainer = new LinearLayout(this);
            nestedContainer.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            layoutParams.setMargins(10,10,10,30);
            nestedContainer.setLayoutParams(layoutParams);
            TextView questionTextSingleLine = new TextView(this);
            questionTextSingleLine.setTextSize(20);
            questionTextSingleLine.setTextColor(getResources().getColor(R.color.text_color));
            questionTextSingleLine.setPadding(0, 0, 0, 16);

            if (ques.getMandatory() == 1) {
                questionTextSingleLine.setText("Q. " + (questionCounter + 1) + "   " + ques.getContent()+"  *");
                if (ques.getParentId() > 0)
                    questionTextSingleLine.setText("Q. " + "   " + ques.getContent()+"  *");
            }

            else{
                questionTextSingleLine.setText("Q. " + (questionCounter + 1) + "   " + ques.getContent());
                if (ques.getParentId() > 0)
                    questionTextSingleLine.setText("Q. " + "   " + ques.getContent());

            }
            nestedContainer.addView(questionTextSingleLine);
            nestedContainer.addView(inflater.inflate(R.layout.answer_multi_line, null));
            nestedContainer.setId(ques.getId());

            nestedContainer.setTag(ques);
            idList.add(ques.getId());
            fieldContainer.addView(nestedContainer);
            answer = (EditText) findViewById(R.id.answer_text);
            answer.setId(ques.getId() + 220);

            answer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {

                    if (b) {

                        answer = (EditText) view;
                       // Toast.makeText(NewResponseActivity.this, answer.getId() + "answerr id", Toast.LENGTH_SHORT).show();


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

        if (ques.getType().contains("DropDownQuestion")) {
            nestedQuestions.add(ques);
            if (!dbAdapter.doesAnswerExist(ques.getId(), currentResponseId)) {
                addAnswer(ques);
            }
            LinearLayout nestedContainer = new LinearLayout(this);
            nestedContainer.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            layoutParams.setMargins(10,10,10,30);
            nestedContainer.setLayoutParams(layoutParams);
            TextView questionTextSingleLine = new TextView(this);
            questionTextSingleLine.setTextSize(20);
            questionTextSingleLine.setTextColor(getResources().getColor(R.color.text_color));
            questionTextSingleLine.setPadding(0, 0, 0, 16);

            if (ques.getMandatory() == 1) {
                questionTextSingleLine.setText("Q. " + (questionCounter + 1) + "   " + ques.getContent()+"  *");
                if (ques.getParentId() > 0)
                    questionTextSingleLine.setText("Q. " + "   " + ques.getContent()+"  *");
            }

            else{
                questionTextSingleLine.setText("Q. " + (questionCounter + 1) + "   " + ques.getContent());
                if (ques.getParentId() > 0)
                    questionTextSingleLine.setText("Q. " + "   " + ques.getContent());

            }
            nestedContainer.addView(questionTextSingleLine);
            //nestedContainer.addView(inflater.inflate(R.layout.answer_dropdown, null));
            nestedContainer.setId(ques.getId());

            nestedContainer.setTag(ques);
            idList.add(ques.getId());
            fieldContainer.addView(nestedContainer);
            spinner = new Spinner(NewResponseActivity.this);
            spinner = (Spinner) getLayoutInflater().inflate(R.layout.answer_dropdown, null);
            nestedContainer.addView(spinner);
            //spinner = (Spinner) findViewById(R.id.drop_down);
            List<String> listOptions = new ArrayList<String>();
            listOptions.add("Select one");
            for (int i = 0; i < ques.getOptions().size(); i++) {
                listOptions.add(ques.getOptions().get(i).getContent());
            }

            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_dropdown_item, listOptions);
            spinner.setAdapter(dataAdapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {


                    if (i != 0) {
                        Options options = ques.getOptions().get(i - 1);
                        addOptionToDataBase(options, ques);
                        nestedQuestionList.clear();
                        nestedQuestionList.add(ques);
                        removeOthersFromDataBase(options, ques);

                        if (options.getQuestions().size() > 0) {

                            for (int j = 0; j < options.getQuestions().size(); j++) {
                                buildLayout(options.getQuestions().get(j));
                                checkForAnswer(options.getQuestions().get(j), currentResponseId);
                            }

                        }

                        if(options.getCategories().size()>0){
                            for(int j=0;j<options.getCategories().size();j++){
                                buildCategoryLayout(options.getCategories().get(j));
                            }
                        }


                        for (int j = 0; j < ques.getOptions().size(); j++) {

                            if (!ques.getOptions().get(j).getContent().equals(options.getContent())) {


                                removeQuestionView(ques.getOptions().get(j));
                                removeCategoryView(ques.getOptions().get(j));

                            }

                        }

                    }
//
//                    if (options.getCategories().size() > 0) {
//                        setCategoryTitle(options);
//                    }


                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }


        if (ques.getType().contains("MultiChoiceQuestion")) {


            nestedQuestions.add(ques);

            if (!dbAdapter.doesAnswerExist(ques.getId(), currentResponseId)) {
                addAnswer(ques);

            }
            Log.e("nestedquestionitem", nestedQuestions.size() + "");
            LinearLayout nestedContainer = new LinearLayout(this);
            nestedContainer.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            layoutParams.setMargins(10,10,10,30);
            nestedContainer.setLayoutParams(layoutParams);
            TextView questionTextSingleLine = new TextView(this);
            questionTextSingleLine.setTextSize(20);
            questionTextSingleLine.setTextColor(getResources().getColor(R.color.text_color));
            questionTextSingleLine.setPadding(0, 0, 0, 16);

            if (ques.getMandatory() == 1) {
                questionTextSingleLine.setText("Q. " + (questionCounter + 1) + "   " + ques.getContent()+"  *");
                if (ques.getParentId() > 0)
                    questionTextSingleLine.setText("Q. " + "   " + ques.getContent()+"  *");
            }

            else{
                questionTextSingleLine.setText("Q. " + (questionCounter + 1) + "   " + ques.getContent());
                if (ques.getParentId() > 0)
                    questionTextSingleLine.setText("Q. " + "   " + ques.getContent());

            }
            nestedContainer.addView(questionTextSingleLine);
            nestedContainer.setId(ques.getId());

            nestedContainer.setTag(ques);
            idList.add(ques.getId());

            LinearLayout ll = new LinearLayout(this);
            ll.setOrientation(LinearLayout.VERTICAL);
            nestedContainer.addView(ll);
            for (int i = 0; i < ques.getOptions().size(); i++) {
                CheckBox checkBox = new CheckBox(this);
                ll.addView(checkBox);
                checkBox.setId(ques.getOptions().get(i).getId());
                checkBox.setText(ques.getOptions().get(i).getContent());
                checkBox.setTextSize(16);
                checkBox.setTextColor(getResources().getColor(R.color.text_color));
                checkBox.setTag(ques.getOptions().get(i));
                checkBox.setButtonDrawable(R.drawable.custom_checkbox);
                checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        checked = true;
                        if (((CheckBox) view).isChecked()) {

                            CheckBox checkBox1 = (CheckBox) view;
                            Options options = (Options) checkBox1.getTag();

                            addOptionToDataBase(options, ques);
                            nestedQuestionList.clear();
                            nestedQuestionList.add(ques);



                            try {
                                if (options.getQuestions().size() > 0) {
                                    for (int i = 0; i < options.getQuestions().size(); i++) {
                                        buildLayout(options.getQuestions().get(i));
                                        checkForAnswer(options.getQuestions().get(i), currentResponseId);
                                    }
                                }

                                if(options.getCategories().size()>0){
                                    for(int i=0;i<options.getCategories().size();i++){
                                        buildCategoryLayout(options.getCategories().get(i));
                                    }
                                }


                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

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


        if (ques.getType().contains("NumericQuestion")) {
            LinearLayout nestedContainer = new LinearLayout(this);
            nestedContainer.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            layoutParams.setMargins(10,10,10,30);
            nestedContainer.setLayoutParams(layoutParams);
            TextView questionTextSingleLine = new TextView(this);
            questionTextSingleLine.setTextSize(20);
            questionTextSingleLine.setTextColor(getResources().getColor(R.color.text_color));
            questionTextSingleLine.setPadding(0, 0, 0, 16);

            if (ques.getMandatory() == 1) {
                questionTextSingleLine.setText("Q. " + (questionCounter + 1) + "   " + ques.getContent()+"  *");
                if (ques.getParentId() > 0)
                    questionTextSingleLine.setText("Q. " + "   " + ques.getContent()+"  *");
            }

            else{
                questionTextSingleLine.setText("Q. " + (questionCounter + 1) + "   " + ques.getContent());
                if (ques.getParentId() > 0)
                    questionTextSingleLine.setText("Q. " + "   " + ques.getContent());

            }
            nestedContainer.addView(questionTextSingleLine);
            nestedContainer.addView(inflater.inflate(R.layout.answer_numeric, null));
            nestedContainer.setId(ques.getId());

            nestedContainer.setTag(ques);
            idList.add(ques.getId());
            fieldContainer.addView(nestedContainer);
            answer = (EditText) findViewById(R.id.answer_text);
            answer.setId(ques.getId() + 220);
            answer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {

                    if (b) {

                        answer = (EditText) view;
                        //Toast.makeText(NewResponseActivity.this, answer.getId() + "answerr id", Toast.LENGTH_SHORT).show();


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

                       // Toast.makeText(NewResponseActivity.this, "saved", Toast.LENGTH_LONG).show();
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


        if (ques.getType().contains("DateQuestion")) {
            dateQuestion = ques;
            LinearLayout nestedContainer = new LinearLayout(this);
            nestedContainer.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            layoutParams.setMargins(10,10,10,30);
            nestedContainer.setLayoutParams(layoutParams);
            TextView questionTextSingleLine = new TextView(this);
            questionTextSingleLine.setTextSize(20);
            questionTextSingleLine.setTextColor(getResources().getColor(R.color.text_color));
            questionTextSingleLine.setPadding(0, 0, 0, 16);

            if (ques.getMandatory() == 1) {
                questionTextSingleLine.setText("Q. " + (questionCounter + 1) + "   " + ques.getContent()+"  *");
                if (ques.getParentId() > 0)
                    questionTextSingleLine.setText("Q. " + "   " + ques.getContent()+"  *");
            }

            else{
                questionTextSingleLine.setText("Q. " + (questionCounter + 1) + "   " + ques.getContent());
                if (ques.getParentId() > 0)
                    questionTextSingleLine.setText("Q. " + "   " + ques.getContent());

            }
            nestedContainer.addView(questionTextSingleLine);
            nestedContainer.addView(inflater.inflate(R.layout.answer_date_picker, null));
            nestedContainer.setId(ques.getId());

            nestedContainer.setTag(ques);
            idList.add(ques.getId());
            fieldContainer.addView(nestedContainer);


            dateText = (TextView) findViewById(R.id.answer_text_date);
            dateText.setId(ques.getId() + 220);
            dateText.setText("dd.yy.mm");
            dateText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
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


        //for radio question
        if (ques.getType().contains("RadioQuestion")) {


            nestedQuestions.add(ques);
            if (!dbAdapter.doesAnswerExist(ques.getId(), currentResponseId)) {
                addAnswer(ques);
            }

            LinearLayout nestedContainer = new LinearLayout(this);
            nestedContainer.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            layoutParams.setMargins(10,10,10,30);
            nestedContainer.setLayoutParams(layoutParams);
            TextView questionTextSingleLine = new TextView(this);
            questionTextSingleLine.setTextSize(20);
            questionTextSingleLine.setTextColor(getResources().getColor(R.color.text_color));
            questionTextSingleLine.setPadding(0, 0, 0, 16);

            if (ques.getMandatory() == 1) {
                questionTextSingleLine.setText("Q. " + (questionCounter + 1) + "   " + ques.getContent()+"  *");
                if (ques.getParentId() > 0)
                    questionTextSingleLine.setText("Q. " + "   " + ques.getContent()+"  *");
            }

            else{
                questionTextSingleLine.setText("Q. " + (questionCounter + 1) + "   " + ques.getContent());
                if (ques.getParentId() > 0)
                    questionTextSingleLine.setText("Q. " + "   " + ques.getContent());

            }
            nestedContainer.addView(questionTextSingleLine);
            nestedContainer.setId(ques.getId());

            nestedContainer.setTag(ques);
            idList.add(ques.getId());


            radioGroup = new RadioGroup(this);
            radioGroup.setOrientation(RadioGroup.VERTICAL);
            nestedContainer.addView(radioGroup);
            for (int i = 0; i < ques.getOptions().size(); i++) {
                final RadioButton radioButton = new RadioButton(this);
                radioGroup.addView(radioButton);
                radioButton.setId(ques.getOptions().get(i).getId());
                radioButton.setTextSize(16);
                radioButton.setTextColor(getResources().getColor(R.color.text_color));
                radioButton.setText(ques.getOptions().get(i).getContent());
                radioButton.setTag(ques.getOptions().get(i));
                radioButton.setButtonDrawable(R.drawable.custom_radio_button);
                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {

                        View myView = findViewById(checkedId);
                        RadioButton radioButton1 = (RadioButton) myView;
                        Options options = (Options) radioButton1.getTag();


                        addOptionToDataBase(options, ques);
                        nestedQuestionList.clear();
                        nestedQuestionList.add(ques);
                        removeOthersFromDataBase(options, ques);




                        if (options.getQuestions().size() > 0) {
                            for (int i = 0; i < options.getQuestions().size(); i++) {
                                buildLayout(options.getQuestions().get(i));
                                checkForAnswer(options.getQuestions().get(i), currentResponseId);
                            }
                        }

                        if(options.getCategories().size()>0){
                            for(int i=0;i<options.getCategories().size();i++){
                                buildCategoryLayout(options.getCategories().get(i));
                            }
                        }


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
            LinearLayout nestedContainer = new LinearLayout(this);
            nestedContainer.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            layoutParams.setMargins(10,10,10,30);
            nestedContainer.setLayoutParams(layoutParams);
            TextView questionTextSingleLine = new TextView(this);
            questionTextSingleLine.setTextSize(20);
            questionTextSingleLine.setTextColor(getResources().getColor(R.color.text_color));
            questionTextSingleLine.setPadding(0, 0, 0, 16);

            if (ques.getMandatory() == 1) {
                questionTextSingleLine.setText("Q. " + (questionCounter + 1) + "   " + ques.getContent()+"  *");
                if (ques.getParentId() > 0)
                    questionTextSingleLine.setText("Q. " + "   " + ques.getContent()+"  *");
            }

            else{
                questionTextSingleLine.setText("Q. " + (questionCounter + 1) + "   " + ques.getContent());
                if (ques.getParentId() > 0)
                    questionTextSingleLine.setText("Q. " + "   " + ques.getContent());

            }
            nestedContainer.addView(questionTextSingleLine);
            nestedContainer.addView(inflater.inflate(R.layout.answer_rating, null));
            nestedContainer.setId(ques.getId());


            nestedContainer.setTag(ques);
            idList.add(ques.getId());
            fieldContainer.addView(nestedContainer);
            ratingBar = (RatingBar) findViewById(R.id.ratingBar);
            ratingBar.setId(ques.getId() + 220);
            ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
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
            LinearLayout nestedContainer = new LinearLayout(this);
            nestedContainer.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            layoutParams.setMargins(10,10,10,30);
            nestedContainer.setLayoutParams(layoutParams);
            TextView questionTextSingleLine = new TextView(this);
            questionTextSingleLine.setTextSize(20);
            questionTextSingleLine.setTextColor(getResources().getColor(R.color.text_color));
            questionTextSingleLine.setPadding(0, 0, 0, 16);

            if (ques.getMandatory() == 1) {
                questionTextSingleLine.setText("Q. " + (questionCounter + 1) + "   " + ques.getContent()+"  *");
                if (ques.getParentId() > 0)
                    questionTextSingleLine.setText("Q. " + "   " + ques.getContent()+"  *");
            }

            else{
                questionTextSingleLine.setText("Q. " + (questionCounter + 1) + "   " + ques.getContent());
                if (ques.getParentId() > 0)
                    questionTextSingleLine.setText("Q. " + "   " + ques.getContent());

            }
            nestedContainer.addView(questionTextSingleLine);
            nestedContainer.setId(ques.getId());


            nestedContainer.addView(inflater.inflate(R.layout.answer_image_picker, null));
            fieldContainer.addView(nestedContainer);

            Button   lButton= (Button) findViewById(R.id.answer_text_image);
            lButton.setId(ques.getId() + 220);

            deleteImageRelativeLayout = (RelativeLayout) findViewById(R.id.image_container);

            imageViewPhotoQuestion = (ImageView) findViewById(R.id.image);

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

//        if (hint) {
//
//            fieldContainer.addView(inflater.inflate(R.layout.hint_helper, null));
//            final RelativeLayout hintContainer = (RelativeLayout) findViewById(R.id.hint_container);
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


    public void onNextClick() {


        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(answer.getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if ((universalQuestion.getType().equals("SingleLineQuestion")) || ((universalQuestion.getType().equals("MultilineQuestion")) || ((universalQuestion.getType().equals("DateQuestion")) || ((universalQuestion.getType().equals("NumericQuestion")))))) {
            addAnswer(universalQuestion);
        } else {
            if (!dbAdapter.doesAnswerExist(universalQuestion.getId(), currentResponseId))
                addAnswer(universalQuestion);
        }
//
//        if (!universalQuestion.getType().equals("PhotoQuestion")) {
//            if (questionCounter == totalQuestionCount - 1) {
//                addAnswer(universalQuestion);
//            }
//        }

        boolean x = checkMandatory(nestedQuestionList);
        if (x) {
            nestedQuestionList.clear();
            if (questionCounter < totalQuestionCount - 1) {
                previousQuestion.setBackgroundColor(getResources().getColor(R.color.login_button_color));
                previousQuestion.setTextColor(getResources().getColor(R.color.white));
                previousQuestion.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_arrow_back, 0, 0, 0);
                nestedQuestions.clear();
                nestedQuestions.clear();
                idList.clear();
                fieldContainer.removeAllViews();




                questionCounter++;


                counterButton.setText(questionCounter + 1 + " out of " + totalQuestionCount);

                for (int j = 0; j < categoriesList.size(); j++) {
                    if (categoriesList.get(j).getOrderNumber() == types.get(questionCounter)) {
                        currentCategory = categoriesList.get(j);


                    if (currentCategory.getType().equals("MultiRecordCategory")) {
                        Button addRecord = new Button(this);
                        addRecord.setBackgroundResource(R.drawable.custom_button);
                        addRecord.setText("+  Add Record");
                        addRecord.setTextColor(getResources().getColor(R.color.white));
                        fieldContainer.addView(addRecord);
                        addRecord.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                buildCategoryLayout(currentCategory);
                                for (int k = 0; k < currentCategory.getQuestionsList().size(); k++) {
//                                    checkForAnswer(currentCategory.getQuestionsList().get(k), currentResponseId);
                                }
                            }
                        });
                    }
                        buildCategoryLayout(currentCategory);

                    }
                }

                for (int j = 0; j < questionsList.size(); j++) {
                    if (questionsList.get(j).getOrderNumber() == types.get(questionCounter)) {
                        Questions cq = questionsList.get(j);
                        buildLayout(cq);
                        checkForAnswer(cq, currentResponseId);
                        break;
                    }
                }

                if (questionCounter + 1 == totalQuestionCount) {


                    markAsComplete.setVisibility(View.VISIBLE);
                    nextQuestion.setTextColor(getResources().getColor(R.color.back_button_text));
                    nextQuestion.setText("NEXT");
                    nextQuestion.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_next_disable, 0);
                    nextQuestion.setBackgroundColor(getResources().getColor(R.color.back_button_background));
                    fieldContainer.addView(markAsComplete);
                }
            }

            if (questionCounter != 0) {
                actionBar.setDisplayHomeAsUpEnabled(false);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setDisplayShowHomeEnabled(false);
                actionBar.setDisplayUseLogoEnabled(false);
            }
        }
    }

    public void createMarkAsComplete() {


        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 5;
        params.topMargin = 30;
        params.gravity=Gravity.CENTER;


        markAsComplete.setBackgroundResource(R.drawable.custom_button);
        markAsComplete.setText("mark as complete");
        markAsComplete.setGravity(Gravity.CENTER_HORIZONTAL);
        markAsComplete.setTextColor(getResources().getColor(R.color.white));
        markAsComplete.setLayoutParams(params);
        markAsComplete.setVisibility(View.GONE);

    }


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
            nestedQuestions.clear();
            idList.clear();
            fieldContainer.removeAllViews();




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

            for (int j = 0; j < questionsList.size(); j++) {
                if (questionsList.get(j).getOrderNumber() == types.get(questionCounter)) {
                    Questions cq = questionsList.get(j);
                    buildLayout(cq);
                    checkForAnswer(cq, currentResponseId);
                    break;
                }
            }

            if (questionCounter == 0) {
                previousQuestion.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_arrow_back_enable, 0, 0, 0);
                previousQuestion.setTextColor(getResources().getColor(R.color.back_button_text));
                previousQuestion.setBackgroundColor(getResources().getColor(R.color.back_button_background));
            }


            if (questionCounter + 1 != totalQuestionCount) {

                nextQuestion.setTextColor(getResources().getColor(R.color.white));
                nextQuestion.setText("NEXT");
                nextQuestion.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_next, 0);
                nextQuestion.setBackgroundColor(getResources().getColor(R.color.login_button_color));
            }


        }
        if (questionCounter == 0) {

            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayUseLogoEnabled(true);


        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {

            photo = (Bitmap) data.getExtras().get("data");

            deleteImageRelativeLayout.setVisibility(View.VISIBLE);
            imageViewPhotoQuestion.setImageBitmap(photo);
            SaveImage(photo);
            addAnswer(universalQuestion);

        }
    }

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


    public void setCategoryTitle(Options options) {


        for (int i = 0; i < options.getCategories().size(); i++) {
            Categories categories = options.getCategories().get(i);
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
            idList.add(categories.getId());
            fieldContainer.addView(nestedContainer);
            for (int j = 0; j < 1; j++) {

                buildLayout(categories.getQuestionsList().get(0));
                checkForAnswer(categories.getQuestionsList().get(0), currentResponseId);
            }

        }

    }

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
        idList.add(categories.getId());
        fieldContainer.addView(nestedContainer);

        for (int j = categories.getQuestionsList().size()-1; j >= 0; j--) {

            buildLayout(categories.getQuestionsList().get(j));
            checkForAnswer(categories.getQuestionsList().get(j), currentResponseId);
        }


    }

    public void removeQuestionView(Options options) {


        try {
            for (int i = 0; i < options.getQuestions().size(); i++) {

                View myView = findViewById(options.getQuestions().get(i).getId());
                ViewGroup parent = (ViewGroup) myView.getParent();
                parent.removeView(myView);
                // idList.remove(options.getQuestions().get(i).getId());
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

    public void checkForAnswer(Questions qu, int responseId) {

        if (qu.getType().equals("SingleLineQuestion")) {
            answer = (EditText) findViewById(qu.getId() + 220);
            answer.setText(dbAdapter.getAnswer(responseId, qu.getId()));
        }


        if (qu.getType().equals("MultilineQuestion")) {
            answer = (EditText) findViewById(qu.getId() + 220);
            answer.setText(dbAdapter.getAnswer(responseId, qu.getId()));
        }


        if (qu.getType().equals("DateQuestion")) {
            dateText = (TextView) findViewById(qu.getId() + 220);
            dateText.setText(dbAdapter.getAnswer(responseId, qu.getId()));
        }

        if (qu.getType().equals("NumericQuestion")) {
            answer = (EditText) findViewById(qu.getId() + 220);
            answer.setText(dbAdapter.getAnswer(responseId, qu.getId()));
        }
        if (qu.getType().equals("RatingQuestion")) {

            ratingBar = (RatingBar) findViewById(qu.getId() + 220);
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
                                //checkForAnswer(options1.getQuestions().get(l),currentResponseId);
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

                        spinner.setSelection(j);

                    }

                }
            }
        }


    }

    //add record to database in case of selectedlement
    public void addOptionToDataBase(Options options, Questions qu) {

//        Answers answers = new Answers();
//        answers.setQuestion_id(qu.getId());
//        answers.setResponseId(currentResponseId);
//        tsLong= System.currentTimeMillis() / 1000;
//        answers.setUpdated_at(tsLong);
//        answers.setContent("");
//        long x = dbAdapter.insertDataAnswersTable(answers);
        if (options != null) {
            Choices choices = new Choices();
            int answerId = dbAdapter.getIdFromAnswerTable(currentResponseId, qu.getId()).get(0);
            choices.setAnswerId(answerId);
            choices.setOptionId(options.getId());
            choices.setOption(options.getContent());
            choices.setType(qu.getType());
            long io = dbAdapter.insertDataChoicesTable(choices);
        }
    }

    //this is remove option from table in case of multichoice
    public void removeOptionFromDataBase(Options options, Questions qu) {
        dbAdapter.deleteOption(options);

    }

    //this is for not selected elements of radio
    public void removeOthersFromDataBase(Options options, Questions qu) {


        for (int i = 0; i < qu.getOptions().size(); i++) {
            if (options.getId() != qu.getOptions().get(i).getId()) {
                dbAdapter.deleteFromChoicesTableWhereOptionId(qu.getOptions().get(i).getId());
                if(qu.getOptions().get(i).getQuestions().size()>0)
                {
                    for(int j=0;j<qu.getOptions().get(i).getQuestions().size();j++){
                        dbAdapter.deleteFromAnswerTable(qu.getOptions().get(i).getQuestions().get(j).getId(),currentResponseId);

                    }
                }



                if(qu.getOptions().get(i).getCategories().size()>0){
                    for(int k=0;k<qu.getOptions().get(i).getCategories().size();k++){
                        for(int l=0;l<qu.getOptions().get(i).getCategories().get(k).getQuestionsList().size();l++){
                            //Toast.makeText(NewResponseActivity.this,"delete "+qu.getOptions().get(i).getCategories().get(k).getQuestionsList().get(l).getId(),Toast.LENGTH_SHORT).show();
                            dbAdapter.deleteFromAnswerTable(qu.getOptions().get(i).getCategories().get(k).getQuestionsList().get(l).getId(),currentResponseId);
                        }
                    }
                }
            }
        }
    }

    public boolean checkMandatory(List<Questions> nestedQuestionList) {

        proceed = true;
        for (int i = 0; i < nestedQuestionList.size(); i++) {
            if (nestedQuestionList.get(i).getType().equals("SingleLineQuestion")) {

                if (nestedQuestionList.get(i).getMandatory() == 1) {
                    if (dbAdapter.doesAnswerExistAsNonNull(nestedQuestionList.get(i).getId(), currentResponseId).equals("")) {
                        showDialog();
                        proceed = false;
                    } else proceed = true;
                }

            }
            if (nestedQuestionList.get(i).getType().equals("MultilineQuestion")) {
                if (nestedQuestionList.get(i).getMandatory() == 1) {
                    if (dbAdapter.doesAnswerExistAsNonNull(nestedQuestionList.get(i).getId(), currentResponseId).equals("")) {
                        showDialog();
                        proceed = false;
                    } else proceed = true;
                }
            }
            if (nestedQuestionList.get(i).getType().equals("RadioQuestion")) {
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
                }}
            if (nestedQuestionList.get(i).getType().equals("MultiChoiceQuestion")) {
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
                }}
            if (nestedQuestionList.get(i).getType().equals("RatingQuestion")) {
                if (nestedQuestionList.get(i).getMandatory() == 1) {
                    if (dbAdapter.doesAnswerExistAsNonNull(nestedQuestionList.get(i).getId(), currentResponseId).equals("")) {
                        showDialog();
                        proceed = false;
                    } else proceed = true;
                }
            }
            if (nestedQuestionList.get(i).getType().equals("PhotoQuestion")) {

            }
            if (nestedQuestionList.get(i).getType().equals("DateQuestion")) {
                if (nestedQuestionList.get(i).getMandatory() == 1) {
                    if (dbAdapter.doesAnswerExistAsNonNull(nestedQuestionList.get(i).getId(), currentResponseId).equals("")) {
                        showDialog();
                        proceed = false;
                    } else proceed = true;
                }
            }
            if (nestedQuestionList.get(i).getType().equals("NumericQuestion")) {
                if (nestedQuestionList.get(i).getMandatory() == 1) {
                    if (dbAdapter.doesAnswerExistAsNonNull(nestedQuestionList.get(i).getId(), currentResponseId).equals("")) {
                        showDialog();
                        proceed = false;
                    } else proceed = true;
                }
            }
            if (nestedQuestionList.get(i).getType().equals("DropDownQuestion")) {
                if (nestedQuestionList.get(i).getMandatory() == 1) {
                    int count = dbAdapter.getAnswerId(currentResponseId, nestedQuestionList.get(i).getId());
                    if (count == 0) {
                        showDialog();
                        proceed = false;
                    }
                    if (count != 0) {
                        if (dbAdapter.getChoicesCountWhereAnswerIdIs(count) == 0) {
                            showDialog();
                            proceed=false;
                        }
                    }
                }}

        }
        return proceed;
    }


    public TextView makeMandatoryText() {
        TextView mandatoryText = new TextView(this);
        mandatoryText.setTextSize(16);
        mandatoryText.setTextColor(getResources().getColor(R.color.login_button_color));
        mandatoryText.setPadding(0, 0, 0, 8);
        mandatoryText.setText(" The Question Is Mandatory");
        return mandatoryText;

    }

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
    class mDateSetListener implements DatePickerDialog.OnDateSetListener {
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            int mYear = year;
            int mMonth = monthOfYear;
            int mDay = dayOfMonth;
            dateText.setText(new StringBuilder().append(mYear).append("/").append(mMonth + 1).append("/").append(mDay).toString());


            Answers answers = new Answers();
            answers.setResponseId((int) dbAdapter.getMaxID());
            answers.setQuestion_id(dateQuestion.getId());
            answers.setContent(dateText.getText().toString());
            tsLong= System.currentTimeMillis() / 1000;
            answers.setUpdated_at(tsLong);
            if (!dbAdapter.doesAnswerExist(universalQuestion.getId(), currentResponseId))
                dbAdapter.insertDataAnswersTable(answers);
            if (dbAdapter.doesAnswerExist(universalQuestion.getId(), currentResponseId)) {
                dbAdapter.deleteFromAnswerTable(universalQuestion.getId(), currentResponseId);
                dbAdapter.insertDataAnswersTable(answers);

            }

        }
    }


}