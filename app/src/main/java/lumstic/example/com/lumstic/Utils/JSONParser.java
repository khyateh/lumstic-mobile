package lumstic.example.com.lumstic.Utils;

import android.database.DatabaseUtils;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import lumstic.example.com.lumstic.Models.Categories;
import lumstic.example.com.lumstic.Models.Options;
import lumstic.example.com.lumstic.Models.Questions;
import lumstic.example.com.lumstic.Models.Surveys;
import lumstic.example.com.lumstic.Models.UserModel;


public class JSONParser {

    List<Questions> questionses;
    List<Surveys> surveyses;
    UserModel userModel;
    List<Categories> categorieses;


    public boolean parseForgotPassword(JSONObject jsonObjectForgotPassword){
        String str= null;
        try {
            str = jsonObjectForgotPassword.getString("notice");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(str.equals("Email address not valid"))
    return false;
        else
return true;
    }

    public UserModel parseLogin(JSONObject jsonObjectLogin){
        userModel= new UserModel();
        try {
            userModel.setAccess_token(jsonObjectLogin.getString("access_token"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            userModel.setOrganisation_id(jsonObjectLogin.getInt("organization_id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            userModel.setUser_id(jsonObjectLogin.getInt("user_id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            userModel.setUsername(jsonObjectLogin.getString("username"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  userModel;
    }


    public Categories parseCategories(JSONObject jsonObjectCategories) {
        Categories categories = new Categories();
        List<Questions> questionsList= new ArrayList<Questions>();
        try {
            try {
                categories.setId(jsonObjectCategories.getInt("id"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                categories.setSurveyId(jsonObjectCategories.getInt("survey_id"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                categories.setOrderNumber(jsonObjectCategories.getInt("order_number"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                categories.setCategoryId(jsonObjectCategories.getInt("category_id"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                categories.setContent(jsonObjectCategories.getString("content"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                categories.setType(jsonObjectCategories.getString("type"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                categories.setParentId(jsonObjectCategories.getInt("parent_id"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {

                for(int i=0;i<jsonObjectCategories.getJSONArray("questions").length();i++){
                    questionsList.add(parseQuestions(jsonObjectCategories.getJSONArray("questions").getJSONObject(i)));
                }
                categories.setQuestionsList(questionsList);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return categories;
    }

    public Options parseOptions(JSONObject jsonObjectOptions) {
        Options options = new Options();
        List<Questions> questionsList= new ArrayList<Questions>();
        List<Categories> categoriesList= new ArrayList<Categories>();
        try {
            options.setId(jsonObjectOptions.getInt("id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            options.setOrderNumber(jsonObjectOptions.getInt("order_number"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            options.setContent(jsonObjectOptions.getString("content"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            options.setQuestionId(jsonObjectOptions.getInt("question_id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {

            for(int i=0;i<jsonObjectOptions.getJSONArray("questions").length();i++){
                questionsList.add(parseQuestions(jsonObjectOptions.getJSONArray("questions").getJSONObject(i)));
            }
            options.setQuestions(questionsList);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {

            for(int i=0;i<jsonObjectOptions.getJSONArray("categories").length();i++){
                categoriesList.add(parseCategories(jsonObjectOptions.getJSONArray("categories").getJSONObject(i)));
            }
            options.setCategories(categoriesList);
        } catch (JSONException e) {
            e.printStackTrace();
        }



        return options;

    }

    public Questions parseQuestions(JSONObject jsonObjectQuestions) {
        Questions questions = new Questions();
        List<Options> optionses;
        optionses = new ArrayList<Options>();
        try {
            try {
                questions.setId(jsonObjectQuestions.getInt("id"));

            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (jsonObjectQuestions.getBoolean("identifier"))
                    questions.setIdentifier(1);
                else
                    questions.setIdentifier(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                questions.setParentId(jsonObjectQuestions.getInt("parent_id"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                questions.setMinValue(jsonObjectQuestions.getInt("min_value"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                questions.setMaxVlue(jsonObjectQuestions.getInt("max_value"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                questions.setMaxLength(jsonObjectQuestions.getInt("max_length"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                questions.setImageUrl(jsonObjectQuestions.getString("image_url"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            questions.setType(jsonObjectQuestions.getString("type"));
            questions.setContent(jsonObjectQuestions.getString("content"));
            try {
                questions.setSurveyId(jsonObjectQuestions.getInt("survey_id"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                questions.setOrderNumber(jsonObjectQuestions.getInt("order_number"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                questions.setCategoryId(jsonObjectQuestions.getInt("category_id"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (jsonObjectQuestions.getBoolean("mandatory"))
                    questions.setMandatory(1);
                else
                    questions.setMandatory(0);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (jsonObjectQuestions.has("options"))
        {
            try {

                JSONArray jsonArrayOptions = jsonObjectQuestions.getJSONArray("options");
                for (int k = 0; k < jsonArrayOptions.length(); k++) {

                    JSONObject jsonObjectOptions = jsonArrayOptions.getJSONObject(k);
                    Options options = parseOptions(jsonObjectOptions);
                    optionses.add(k, options);

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        questions.setOptions(optionses);

        try {

            if((!jsonObjectQuestions.has("options"))&&(jsonObjectQuestions.getInt("parent_id")>=0)){
                Log.e("workingwork","abc");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return questions;
    }


    public List<Surveys> parseSurvey(JSONArray jsonArrayMain) {


        surveyses = new ArrayList<Surveys>();
        for (int i = 0; i < jsonArrayMain.length(); i++) {
            Surveys surveys;
            surveys = new Surveys();
            try {
                JSONObject jsonObject = jsonArrayMain.getJSONObject(i);
                questionses = new ArrayList<Questions>();
                categorieses = new ArrayList<Categories>();
                surveys.setId(Integer.parseInt(jsonObject.getString("id")));
                surveys.setExpiryDate(jsonObject.getString("expiry_date"));
                surveys.setDescription(jsonObject.getString("description"));
                surveys.setName(jsonObject.getString("name"));
                surveys.setPublishedOn(jsonObject.getString("published_on"));

                JSONArray jsonArray = jsonObject.getJSONArray("questions");
                for (int j = 0; j < jsonArray.length(); j++) {
                    JSONObject jsonObjectQuestion = jsonArray.getJSONObject(j);
                    Questions questions = parseQuestions(jsonObjectQuestion);
                    if(questions!=null){
                    questionses.add(j, questions);
                }}
                surveys.setQuestions(questionses);


                JSONArray jsonArrayCategories = jsonObject.getJSONArray("categories");
                for (int l = 0; l < jsonArrayCategories.length(); l++) {
                    JSONObject jsonObjectCategories = jsonArrayCategories.getJSONObject(l);
                    Categories categories = parseCategories(jsonObjectCategories);
                    categorieses.add(l, categories);

                }
                surveys.setCategories(categorieses);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            surveyses.add(surveys);
        }

        return surveyses;
    }
    public boolean parseSyncResult(String syncResponse) {
        try {
            JSONObject jsonObject = new JSONObject(syncResponse);
            if ((jsonObject.getString("state").equals("clean")) && (jsonObject.getString("status").equals("complete"))) {

                return true;
            } else return false;


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }
}