package lumstic.ashoka.com.lumstic.Models;


public class MasterSurveyQuestionModel {
    public Object getObject() {
        return object;
    }

    public int getType() {
        return type;
    }

    public MasterSurveyQuestionModel(Object object, int type) {
        this.object = object;
        this.type = type;
    }

    Object object;
    int type;


}
