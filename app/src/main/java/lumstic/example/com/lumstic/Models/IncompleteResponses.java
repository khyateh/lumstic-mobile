package lumstic.example.com.lumstic.Models;

public class IncompleteResponses {
    String responseNumber;
    String responseText;

    public IncompleteResponses(String responseNumber, String responseText) {
        this.responseNumber = responseNumber;
        this.responseText = responseText;
    }
    public String getResponseNumber() {
        return responseNumber;
    }
    public void setResponseNumber(String responseNumber) {
        this.responseNumber = responseNumber;
    }
    public String getResponseText() {
        return responseText;
    }
    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }
}