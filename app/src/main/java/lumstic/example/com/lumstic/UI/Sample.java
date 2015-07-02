package lumstic.example.com.lumstic.UI;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import lumstic.example.com.lumstic.Adapters.DBAdapter;
import lumstic.example.com.lumstic.Models.Answers;
import lumstic.example.com.lumstic.Models.Categories;
import lumstic.example.com.lumstic.Models.Questions;
import lumstic.example.com.lumstic.Models.Surveys;
import lumstic.example.com.lumstic.R;
import lumstic.example.com.lumstic.Utils.IntentConstants;

public class Sample extends Activity {

    List<Questions> questionsList;
    List<Categories> categoriesList;
    List<Questions> nestedQuestions;
    ArrayList<Integer> types = null;
    ArrayList<String> stringTypes = null;
    List<Integer> idList;


    ActionBar actionBar;
    DBAdapter dbAdapter;

    int currentResponseId = 0;
    String fname = "";
    int categoryAndQuestionCount = 0;
    int totalQuestionCount = 0;
    int questionCount = 0;
    int categoryCount = 0;
    int CAMERA_REQUEST = 1;
    Bitmap photo = null;
    int PICK_FROM_CAMERA = 1;
    int questionCounter = 0;


    EditText answer;
    Button counterButton, markAsComplete;
    TextView dateText;
    Spinner spinner;
    LinearLayout fieldContainer;
    LayoutInflater inflater;
    RelativeLayout deleteImageRelativeLayout;
    ImageView imageViewPhotoQuestion;
    RelativeLayout imageContainer;
    Button nextQuestion, previousQuestion;
    RatingBar ratingBar;



    Surveys surveys;
    Categories currentCategory;
    Questions universalQuestion;












    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);



        actionBar = getActionBar();
        actionBar.setTitle("New Response Activity");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);

        markAsComplete = new Button(this);
        counterButton = (Button) findViewById(R.id.counter_button);
        fieldContainer = (LinearLayout) findViewById(R.id.field_container);
        nextQuestion = (Button) findViewById(R.id.next_queation);
        previousQuestion = (Button) findViewById(R.id.previous_question);
        previousQuestion.setText("BACK");


        inflater = getLayoutInflater();


        nestedQuestions = new ArrayList<Questions>();
        idList = new ArrayList<Integer>();
        dbAdapter = new DBAdapter(Sample.this);
        questionsList = new ArrayList<Questions>();
        categoriesList = new ArrayList<Categories>();
        stringTypes = new ArrayList<String>();
        types = new ArrayList<Integer>();


        surveys = (Surveys) getIntent().getExtras().getSerializable(IntentConstants.SURVEY);


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

        if (surveys.getCategories().size() > 0) {
            categoriesList = surveys.getCategories();
            categoryCount = categoriesList.size();
        }


        totalQuestionCount = categoryCount + questionCount;



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
                counterButton.setText("1 out of " + totalQuestionCount);
                buildLayout(cq);
               // checkForAnswer(cq, currentResponseId);
                break;
            }
        }
        for (int j = 0; j < categoriesList.size(); j++) {
            if (categoriesList.get(j).getOrderNumber() == types.get(0)) {

                counterButton.setText("1 out of " + totalQuestionCount);
                Categories currentCategory = categoriesList.get(j);
                //buildCategoryLayout(currentCategory);

//
//                for (int k = 0; k < currentCategory.getQuestionsList().size(); k++) {
//                    checkForAnswer(currentCategory.getQuestionsList().get(k), currentResponseId);
//
//                }
                break;
            }
        }





    }

    public void buildLayout(final Questions ques) {
        universalQuestion = ques;
        if (ques.getType().equals("SingleLineQuestion")) {
            LinearLayout nestedContainer = new LinearLayout(this);
            nestedContainer.setOrientation(LinearLayout.VERTICAL);
            TextView questionTextSingleLine = new TextView(this);
            questionTextSingleLine.setTextSize(20);
            questionTextSingleLine.setTextColor(getResources().getColor(R.color.text_color));
            questionTextSingleLine.setPadding(0, 0, 0, 16);
            questionTextSingleLine.setText("Q. " + (questionCounter + 1) + "   " + ques.getContent());
            if (ques.getParentId() > 0)
                questionTextSingleLine.setText("Q. " + "   " + ques.getContent());
            nestedContainer.addView(questionTextSingleLine);
            nestedContainer.addView(inflater.inflate(R.layout.answer_single_line, null));

            nestedContainer.setTag(ques);
            idList.add(ques.getId());
            fieldContainer.addView(nestedContainer);
            answer = (EditText) findViewById(R.id.answer_text);
            answer.setId(ques.getId());
            InputMethodManager imm = (InputMethodManager) getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(answer.getWindowToken(), 0);
            answer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {

                    if (!b) {

                        Answers answers = new Answers();
                        answers.setQuestion_id(ques.getId());
                        answers.setResponseId(currentResponseId);
                        answers.setContent(answer.getText().toString());
                        long x = dbAdapter.insertDataAnswersTable(answers);
                        Toast.makeText(Sample.this, ques.getId() + "answer is saved", Toast.LENGTH_LONG).show();

                    }}
            });

//            try {
//                checkForAnswer(ques, currentResponseId);
//            } catch (Exception e) {
//                e.printStackTrace();
//                checkHint();
//            }
        }


        if (ques.getType().contains("MultiLineQuestion")) {

            LinearLayout nestedContainer = new LinearLayout(this);
            nestedContainer.setOrientation(LinearLayout.VERTICAL);
            TextView questionTextSingleLine = new TextView(this);
            questionTextSingleLine.setTextSize(20);
            questionTextSingleLine.setTextColor(getResources().getColor(R.color.text_color));
            questionTextSingleLine.setPadding(0, 0, 0, 16);
            questionTextSingleLine.setText("Q. " + (questionCounter + 1) + "   " + ques.getContent());
            if (ques.getParentId() > 0)
                questionTextSingleLine.setText("Q. " + "   " + ques.getContent());
            nestedContainer.addView(questionTextSingleLine);
            nestedContainer.addView(inflater.inflate(R.layout.answer_multi_line, null));

            nestedContainer.setTag(ques);
            idList.add(ques.getId());
            fieldContainer.addView(nestedContainer);
            answer = (EditText) findViewById(R.id.answer_text);
            answer.setId(ques.getId());

            answer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {

                    if (!b) {

                        Answers answers = new Answers();
                        answers.setQuestion_id(ques.getId());
                        answers.setResponseId(currentResponseId);
                        answers.setContent(answer.getText().toString());
                        long x = dbAdapter.insertDataAnswersTable(answers);
                        Toast.makeText(Sample.this, ques.getId() + "answer is saved", Toast.LENGTH_LONG).show();

                    }}
            });
//            try {
//                checkForAnswer(ques, currentResponseId);
//            } catch (Exception e) {
//                e.printStackTrace();
//                checkHint();
//            }
//            checkHint();

        }

        if (ques.getType().contains("DropDownQuestion")) {


            LinearLayout nestedContainer = new LinearLayout(this);
            nestedContainer.setOrientation(LinearLayout.VERTICAL);
            TextView questionTextSingleLine = new TextView(this);
            questionTextSingleLine.setTextSize(20);
            questionTextSingleLine.setTextColor(getResources().getColor(R.color.text_color));
            questionTextSingleLine.setPadding(0, 0, 0, 16);
            questionTextSingleLine.setText("Q. " + (questionCounter + 1) + "   " + ques.getContent());
            if (ques.getParentId() > 0)
                questionTextSingleLine.setText("Q. " + "   " + ques.getContent());
            nestedContainer.addView(questionTextSingleLine);
            nestedContainer.addView(inflater.inflate(R.layout.answer_dropdown, null));

            nestedContainer.setTag(ques);
            idList.add(ques.getId());
            fieldContainer.addView(nestedContainer);

            spinner = (Spinner) findViewById(R.id.drop_down);
            List<String> listOptions = new ArrayList<String>();
            for (int i = 0; i < ques.getOptions().size(); i++) {
                listOptions.add(ques.getOptions().get(i).getContent());
            }
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_dropdown_item, listOptions);
            spinner.setAdapter(dataAdapter);
//            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                @Override
//                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//
//                    //Toast.makeText(NewResponseActivity.this,""+i,Toast.LENGTH_SHORT).show();
//                    Options options = ques.getOptions().get(i);
//                    addOptionToDataBase(options, ques);
//                    removeOthersFromDataBase(options, ques);
//
//                    if (options.getQuestions().size() > 0) {
//                        //  Toast.makeText(NewResponseActivity.this,"has options",Toast.LENGTH_SHORT).show();
//                        for (int j = 0; j < options.getQuestions().size(); j++) {
//                            buildLayout(options.getQuestions().get(j));
//                            checkForAnswer(options.getQuestions().get(j), currentResponseId);
//                        }
//                    }
//
//                    if (options.getQuestions().size() <= 0) {
//
//                    }
//                    if (options.getQuestions().size() <= 0) {
//
//                        for (int j = 0; j < ques.getOptions().size(); j++) {
//
//                            if (!ques.getOptions().get(j).getContent().equals(options.getContent())) {
//
//
//                                removeQuestionView(ques.getOptions().get(j));
//                                removeCategoryView(ques.getOptions().get(j));
//
//                            }
//
//                        }
//
//                    }
//
//                    if (options.getCategories().size() > 0) {
//                        setCategoryTitle(options);
//                    }
//
//
//                }
//
//                @Override
//                public void onNothingSelected(AdapterView<?> adapterView) {
//
//                }
//            });
        }


        if (ques.getType().contains("MultiChoiceQuestion")) {


            nestedQuestions.add(ques);


            Log.e("nestedquestionitem", nestedQuestions.size() + "");

            LinearLayout nestedContainer = new LinearLayout(this);
            nestedContainer.setOrientation(LinearLayout.VERTICAL);
            TextView questionTextSingleLine = new TextView(this);
            questionTextSingleLine.setTextSize(20);
            questionTextSingleLine.setTextColor(getResources().getColor(R.color.text_color));
            questionTextSingleLine.setPadding(0, 0, 0, 16);
            questionTextSingleLine.setText("Q. " + (questionCounter + 1) + "   " + ques.getContent());
            if (ques.getParentId() > 0)
                questionTextSingleLine.setText("Q. " + "   " + ques.getContent());
            nestedContainer.addView(questionTextSingleLine);

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
//                checkBox.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        if (((CheckBox) view).isChecked()) {
//
//                            CheckBox checkBox1 = (CheckBox) view;
//                            Options options = (Options) checkBox1.getTag();
//
//                            addOptionToDataBase(options, ques);
//
//
//                            if (options.getQuestions().size() > 0) {
//                                //        Toast.makeText(NewResponseActivity.this,"has options",Toast.LENGTH_SHORT).show();
//                                for (int i = 0; i < options.getQuestions().size(); i++) {
//                                    buildLayout(options.getQuestions().get(i));
//                                    checkForAnswer(options.getQuestions().get(i), currentResponseId);
//                                }
//                            }
//
//                            if (options.getCategories().size() > 0) {
//                                setCategoryTitle(options);
//                            }
//                        }
//
//                        if (!((CheckBox) view).isChecked()) {
//
//
//                            //  Toast.makeText(NewResponseActivity.this,"has been unchecked ",Toast.LENGTH_SHORT).show();
//                            CheckBox checkBox1 = (CheckBox) view;
//                            Options options = (Options) checkBox1.getTag();
//
//
//                            removeOptionFromDataBase(options, ques);
//
//                            if (options.getQuestions().size() > 0) {
//                                removeQuestionView(options);
//                            }
//
//                            if (options.getCategories().size() > 0) {
//                                removeCategoryView(options);
//                            }
//                        }
//
//                    }
//                });


            }
            fieldContainer.addView(nestedContainer);

        }


        if (ques.getType().contains("NumericQuestion")) {
            LinearLayout nestedContainer = new LinearLayout(this);
            nestedContainer.setOrientation(LinearLayout.VERTICAL);
            TextView questionTextSingleLine = new TextView(this);
            questionTextSingleLine.setTextSize(20);
            questionTextSingleLine.setTextColor(getResources().getColor(R.color.text_color));
            questionTextSingleLine.setPadding(0, 0, 0, 16);
            questionTextSingleLine.setText("Q. " + (questionCounter + 1) + "   " + ques.getContent());
            if (ques.getParentId() > 0)
                questionTextSingleLine.setText("Q. " + "   " + ques.getContent());

            nestedContainer.addView(questionTextSingleLine);
            nestedContainer.addView(inflater.inflate(R.layout.answer_numeric, null));

            nestedContainer.setTag(ques);
            idList.add(ques.getId());
            fieldContainer.addView(nestedContainer);
            answer = (EditText) findViewById(R.id.answer_text);
            answer.setId(ques.getId());
            answer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {

                    if (!b) {

                        Answers answers = new Answers();
                        answers.setQuestion_id(ques.getId());
                        answers.setResponseId(currentResponseId);
                        answers.setContent(answer.getText().toString());
                        long x = dbAdapter.insertDataAnswersTable(answers);
                        Toast.makeText(Sample.this, ques.getId() + "answer is saved", Toast.LENGTH_LONG).show();

                    }}
            });
//            try {
//                checkForAnswer(ques, currentResponseId);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }



        }


        if (ques.getType().contains("DateQuestion")) {
            LinearLayout nestedContainer = new LinearLayout(this);
            nestedContainer.setOrientation(LinearLayout.VERTICAL);
            TextView questionTextSingleLine = new TextView(this);
            questionTextSingleLine.setTextSize(20);
            questionTextSingleLine.setTextColor(getResources().getColor(R.color.text_color));
            questionTextSingleLine.setPadding(0, 0, 0, 16);
            questionTextSingleLine.setText("Q. " + (questionCounter + 1) + "   " + ques.getContent());
            if (ques.getParentId() > 0)
                questionTextSingleLine.setText("Q. " + "   " + ques.getContent());

            nestedContainer.addView(questionTextSingleLine);
            nestedContainer.addView(inflater.inflate(R.layout.answer_date_picker, null));

            nestedContainer.setTag(ques);
            idList.add(ques.getId());
            fieldContainer.addView(nestedContainer);


            dateText = (TextView) findViewById(R.id.answer_text_date);
            dateText.setId(ques.getId());
            dateText.setText("dd.yy.mm");
            dateText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Calendar c = Calendar.getInstance();
                    int mYear = c.get(Calendar.YEAR);
                    int mMonth = c.get(Calendar.MONTH);
                    int mDay = c.get(Calendar.DAY_OF_MONTH);
                    DatePickerDialog dialog = new DatePickerDialog(Sample.this,
                            new mDateSetListener(), mYear, mMonth, mDay);
                    dialog.show();
                }
            });

//            try {
//                checkForAnswer(ques, currentResponseId);
//            } catch (Exception e) {
//                e.printStackTrace();
//                checkHint();
//            }
        }


        //for radio question
        if (ques.getType().contains("RadioQuestion")) {


            nestedQuestions.add(ques);

            LinearLayout nestedContainer = new LinearLayout(this);
            nestedContainer.setOrientation(LinearLayout.VERTICAL);
            TextView questionTextSingleLine = new TextView(this);
            questionTextSingleLine.setTextSize(20);
            questionTextSingleLine.setTextColor(getResources().getColor(R.color.text_color));
            questionTextSingleLine.setPadding(0, 0, 0, 16);
            questionTextSingleLine.setText("Q. " + (questionCounter + 1) + "   " + ques.getContent());
            if (ques.getParentId() > 0)
                questionTextSingleLine.setText("Q. " + "   " + ques.getContent());
            nestedContainer.addView(questionTextSingleLine);

            nestedContainer.setTag(ques);
            idList.add(ques.getId());


            RadioGroup radioGroup = new RadioGroup(this);
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
//                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//                    @Override
//                    public void onCheckedChanged(RadioGroup group, int checkedId) {
//
//                        View myView = findViewById(checkedId);
//                        RadioButton radioButton1 = (RadioButton) myView;
//                        Options options = (Options) radioButton1.getTag();
//
//
//                        addOptionToDataBase(options, ques);
//                        removeOthersFromDataBase(options, ques);
//
//
//                        //Toast.makeText(NewResponseActivity.this,options.getId()+"",Toast.LENGTH_SHORT).show();
//                        if (options.getQuestions().size() > 0) {
//                            //  Toast.makeText(NewResponseActivity.this,"has options",Toast.LENGTH_SHORT).show();
//                            for (int i = 0; i < options.getQuestions().size(); i++) {
//                                buildLayout(options.getQuestions().get(i));
//                                checkForAnswer(options.getQuestions().get(i), currentResponseId);
//                            }
//                        }
//                        if (options.getQuestions().size() <= 0) {
//                            for (int i = 0; i < ques.getOptions().size(); i++) {
//                                if (!ques.getOptions().get(i).getContent().equals(options.getContent())) {
//                                    removeQuestionView(ques.getOptions().get(i));
//                                    removeCategoryView(ques.getOptions().get(i));
//                                }
//
//                            }
//
//                        }
//
//                    }
//                });

            }
            fieldContainer.addView(nestedContainer);


        }
        if (ques.getType().equals("RatingQuestion")) {
            LinearLayout nestedContainer = new LinearLayout(this);
            nestedContainer.setOrientation(LinearLayout.VERTICAL);
            TextView questionTextSingleLine = new TextView(this);
            questionTextSingleLine.setTextSize(20);
            questionTextSingleLine.setTextColor(getResources().getColor(R.color.text_color));
            questionTextSingleLine.setPadding(0, 0, 0, 16);
            questionTextSingleLine.setText("Q. " + (questionCounter + 1) + "   " + ques.getContent());
            if (ques.getParentId() > 0)
                questionTextSingleLine.setText("Q. " + "   " + ques.getContent());
            nestedContainer.addView(questionTextSingleLine);
            nestedContainer.addView(inflater.inflate(R.layout.answer_rating, null));


            nestedContainer.setTag(ques);
            idList.add(ques.getId());
            fieldContainer.addView(nestedContainer);
            ratingBar = (RatingBar) findViewById(R.id.ratingBar);
            ratingBar.setId(ques.getId());
            ratingBar.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    if(!b){
                        Answers answers = new Answers();
                        answers.setQuestion_id(ques.getId());
                        answers.setResponseId(currentResponseId);
                        answers.setContent(String.valueOf(ratingBar.getNumStars()));
                        long x = dbAdapter.insertDataAnswersTable(answers);
                        Toast.makeText(Sample.this, ques.getId() + "answer is saved", Toast.LENGTH_LONG).show();
                    }
                }
            });



        }

        //for image question
        if (ques.getType().equals("PhotoQuestion")) {
            LinearLayout nestedContainer = new LinearLayout(this);
            nestedContainer.setOrientation(LinearLayout.VERTICAL);
            TextView questionTextSingleLine = new TextView(this);
            questionTextSingleLine.setTextSize(20);
            questionTextSingleLine.setTextColor(getResources().getColor(R.color.text_color));
            questionTextSingleLine.setPadding(0, 0, 0, 16);
            questionTextSingleLine.setText("Q. " + (questionCounter + 1) + "   " + ques.getContent());
            if (ques.getParentId() > 0)
                questionTextSingleLine.setText("Q. " + "   " + ques.getContent());
            nestedContainer.addView(questionTextSingleLine);


            nestedContainer.addView(inflater.inflate(R.layout.answer_image_picker, null));
            fieldContainer.addView(nestedContainer);

            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.answer_text_image);

            deleteImageRelativeLayout = (RelativeLayout) findViewById(R.id.image_container);

            imageViewPhotoQuestion = (ImageView) findViewById(R.id.image);
            imageContainer = (RelativeLayout) findViewById(R.id.image_container);
            linearLayout.setOnClickListener(new View.OnClickListener() {
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
    }

    class mDateSetListener implements DatePickerDialog.OnDateSetListener {
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            int mYear = year;
            int mMonth = monthOfYear;
            int mDay = dayOfMonth;
            dateText.setText(new StringBuilder().append(mMonth + 1).append("/").append(mDay).append("/").append(mYear).append(" ").toString());
        }
    }
}
