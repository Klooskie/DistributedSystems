import java.io.Serializable;

public class PutNotification implements Serializable {
    String key;
    Integer value;

    public PutNotification(String key, Integer value) {
        this.key = key;
        this.value = value;
    }
}