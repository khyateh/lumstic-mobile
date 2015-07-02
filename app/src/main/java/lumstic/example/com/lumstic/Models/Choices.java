package lumstic.example.com.lumstic.Models;


public class Choices {
    int id;
    int optionId;
    int answerId;
    String option;
    String type;

    public int getId() {
        return id;
    }

    public String getOption() {
        return option;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOptionId() {
        return optionId;
    }

    public void setOptionId(int optionId) {
        this.optionId = optionId;
    }

    public int getAnswerId() {
        return answerId;
    }

    public void setAnswerId(int answerId) {
        this.answerId = answerId;
    }
}
