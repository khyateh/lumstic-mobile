package lumstic.ashoka.com.lumstic.Models;

/**
 * Created by work on 24/7/15.
 */
public class DropDown {
    String tag,value;
    public DropDown(String tag, String value) {
        this.tag = tag;
        this.value = value;

    }
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }


}
