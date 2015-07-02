package lumstic.example.com.lumstic.Models;

/**
 * Created by work on 17/4/15.
 */
public class DatabaseSequence {
    int seq;
    String name;

    public int getSeq() {
        return seq;
    }
    public void setSeq(int seq) {
        this.seq = seq;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public DatabaseSequence(int seq, String name) {
        this.seq = seq;
        this.name = name;
    }
}
