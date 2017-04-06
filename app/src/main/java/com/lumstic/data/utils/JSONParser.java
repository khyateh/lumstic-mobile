package com.lumstic.data.utils;

import com.lumstic.data.models.Categories;
import com.lumstic.data.models.Choices;
import com.lumstic.data.models.IdentifierChoices;
import com.lumstic.data.models.Identifiers;
import com.lumstic.data.models.Options;
import com.lumstic.data.models.Questions;
import com.lumstic.data.models.Respondent;
import com.lumstic.data.models.Surveys;
import com.lumstic.data.models.UserModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class JSONParser {

    List<Questions> questionsList;
    List<Surveys> surveysList;
    List<Respondent> respondentsList;
    UserModel userModel;
    List<Categories> categoriesList;


    public boolean parseForgotPassword(JSONObject jsonObjectForgotPassword) {
        String str = null;
        try {
            str = jsonObjectForgotPassword.getString("notice");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if ("Email address not valid".equals(str))
            return false;

        return true;
    }

    public UserModel parseLogin(JSONObject jsonObjectLogin) {
        userModel = new UserModel();
        try {
            userModel.setAccessToken(jsonObjectLogin.getString("access_token"));
            userModel.setOrganisationId(jsonObjectLogin.getInt("organization_id"));
            userModel.setUserId(jsonObjectLogin.getInt("user_id"));
            userModel.setUsername(jsonObjectLogin.getString("username"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return userModel;
    }


    public Categories parseCategories(JSONObject jsonObjectCategories) {
        Categories categories = new Categories();
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


        return categories;
    }

    public Options parseOptions(JSONObject jsonObjectOptions) {
        Options options = new Options();
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


        return options;

    }

    public Questions parseQuestions(JSONObject jsonObjectQuestions) {
        Questions questions = new Questions();
        List<Options> optionses;
        optionses = new ArrayList<>();
        try {
            questions.setId(jsonObjectQuestions.getInt("id"));

        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (jsonObjectQuestions.getBoolean("identifier")) {
                questions.setIdentifier(1);
            } else {
                questions.setIdentifier(0);
            }
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
          //  CommonUtil.printmsg("jsonObjectQuestions.getInt(\"max_value\"):: "+jsonObjectQuestions.getInt("max_value"));
            //TODO jyothi Feb 7 2017 To fix Number to E issue
            questions.setMaxValue(jsonObjectQuestions.getDouble("max_value"));
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
        try {
            questions.setType(jsonObjectQuestions.getString("type"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            questions.setContent(jsonObjectQuestions.getString("content"));
        } catch (Exception e) {
            e.printStackTrace();
        }

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
            if (jsonObjectQuestions.getBoolean("mandatory")) {
                questions.setMandatory(1);
            } else {
                questions.setMandatory(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (jsonObjectQuestions.has("options")) {
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

        return questions;
    }

    public Respondent parseRespondent(JSONObject jsonObjectRespondent)
    {
        Respondent respondent = new Respondent();
        List<Identifiers> identifiers = new ArrayList<>();

        try {
            respondent.setId(jsonObjectRespondent.getInt("id"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            respondent.setOrganisationId(jsonObjectRespondent.getInt("organization_id"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            respondent.setTag(jsonObjectRespondent.getString("tag"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            respondent.setStatus("incomplete");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (jsonObjectRespondent.has("identifiers")) {
            try {
                String tag ="";
                JSONArray jsonArrayIdentifiers = jsonObjectRespondent.getJSONArray("identifiers");
                for (int k = 0; k < jsonArrayIdentifiers.length(); k++) {

                    JSONObject jsonObjectIdentifier = jsonArrayIdentifiers.getJSONObject(k);
                    Identifiers identifier = parseIdentifier(jsonObjectIdentifier);
                    identifiers.add(k, identifier);
                    tag += (tag.isEmpty()?tag:", ") + identifier.getContent();
                }

                respondent.setTag(tag);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        respondent.setIdentifiers(identifiers);

        return respondent;
    }

    public Identifiers parseIdentifier(JSONObject jsonObjectIdentifier)
    {
        Identifiers identifier = new Identifiers();
        IdentifierChoices identifierChoices = new IdentifierChoices();
        List<Choices> choices = new ArrayList<>();


        try {
            identifier.setQuestionId(jsonObjectIdentifier.getInt("question_id"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            identifier.setType(jsonObjectIdentifier.getString("question_type"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            identifier.setContent(jsonObjectIdentifier.getString("answer_content"));
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (jsonObjectIdentifier.has("option")) {
            try {
                JSONObject identChoices = jsonObjectIdentifier.getJSONObject("option");
                JSONArray jsonArrayChoices = identChoices.getJSONArray("choices");
                for (int k = 0; k < jsonArrayChoices.length(); k++) {
                    JSONObject jsonchoice = jsonArrayChoices.getJSONObject(k);
                    Choices choice = new Choices();
                    choice.setOptionId(jsonchoice.getInt("option_id"));
                    choices.add(k, choice);
                }
                identifierChoices.setChoices(choices);
                identifier.setIdentifierChoices(identifierChoices);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        return identifier;
    }


    JSONArray jsonParse(JSONArray jsonArray) throws JSONException {
        List<JSONObject> jsonValues = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            jsonValues.add(jsonObject);

            if (jsonObject.has("options")) {
                jsonObject.put("options", jsonParse(jsonObject.getJSONArray("options")));
            }
            if (jsonObject.has("questions")) {
                jsonObject.put("questions", jsonParse(jsonObject.getJSONArray("questions")));
            }
            if (jsonObject.has("categories")) {
                jsonObject.put("categories", jsonParse(jsonObject.getJSONArray("categories")));
            }
        }
        Collections.sort(jsonValues, new Comparator<JSONObject>() {
            private static final String KEY_NAME = "order_number";

            @Override
            public int compare(JSONObject a, JSONObject b) {
                int valA = 0, valB = 0;
                try {
                    valA = Integer.parseInt(a.get(KEY_NAME).toString());
                    valB = Integer.parseInt(b.get(KEY_NAME).toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (valA > valB) {
                    return 1;
                } else if (valA < valB) {
                    return -1;
                }
                return 0;
            }
        });
        JSONArray jsonArray1 = new JSONArray();
        for (int j = 0; j < jsonValues.size(); j++) {
            jsonArray1.put(jsonValues.get(j));
        }
        return jsonArray1;
    }

    public List<Surveys> parseSurvey(JSONArray jsonArrayMain) {


        surveysList = new ArrayList<>();
        for (int i = 0; i < jsonArrayMain.length(); i++) {
            Surveys surveys;
            surveys = new Surveys();
            try {
                JSONObject jsonObject = jsonArrayMain.getJSONObject(i);
                questionsList = new ArrayList<>();
                categoriesList = new ArrayList<>();
                respondentsList = new ArrayList<>();

                surveys.setId(Integer.parseInt(jsonObject.getString("id")));
                surveys.setExpiryDate(jsonObject.getString("expiry_date"));
                surveys.setDescription(jsonObject.getString("description"));
                surveys.setName(jsonObject.getString("name"));
                surveys.setPublishedOn(jsonObject.getString("published_on"));

                JSONArray jsonArray = jsonObject.getJSONArray("questions");
                JSONArray sortedArrayForQuestion = jsonParse(jsonArray);
                for (int j = 0; j < sortedArrayForQuestion.length(); j++) {
                    JSONObject jsonObjectQuestion = sortedArrayForQuestion.getJSONObject(j);
                    //CommonUtil.printmsg("jsonObjectQuestion:: "+jsonObjectQuestion.toString());
                    Questions questions = parseQuestions(jsonObjectQuestion);
                    if (questions != null) {
                        questionsList.add(j, questions);
                    }
                }
                surveys.setQuestionsList(questionsList);


                JSONArray jsonArrayCategories = jsonObject.getJSONArray("categories");
                JSONArray sortedArrayForCategories = jsonParse(jsonArrayCategories);
                for (int l = 0; l < sortedArrayForCategories.length(); l++) {
                    JSONObject jsonObjectCategories = sortedArrayForCategories.getJSONObject(l);
                    Categories categories = parseCategories(jsonObjectCategories);
                    categoriesList.add(l, categories);

                }
                surveys.setCategoriesList(categoriesList);


                JSONArray jsonArrayRespondent = jsonObject.getJSONArray("respondents");
                //JSONArray sortedArrayForQuestion = jsonParse(jsonArray);
                for (int j = 0; j < jsonArrayRespondent.length(); j++) {
                    JSONObject jsonObjectRespondent = jsonArrayRespondent.getJSONObject(j);
                    Respondent respondent = parseRespondent(jsonObjectRespondent);
                    if (respondent != null) {
                        respondentsList.add(j, respondent);
                    }
                }
                surveys.setRespondentList(respondentsList);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            surveysList.add(surveys);
        }

        return surveysList;
    }

    public boolean parseSyncResult(String syncResponse) {
        try {
            JSONObject jsonObject = new JSONObject(syncResponse);
            if ("clean".equals(jsonObject.getString("state")) && "complete".equals(jsonObject.getString("status")))
                return true;

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
     //   CommonUtil.printmsg("After catch in parseSyncResult");
        return false;
    }

    public int parseRecordIdResult(String syncResponse) {
        int parseId = 0;
        try {
            JSONObject jsonObject = new JSONObject(syncResponse);
            parseId = jsonObject.getInt("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return parseId;
    }

}