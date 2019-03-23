import java.io.Serializable;

public class RemovalNotification implements Serializable {
    String key;

    public RemovalNotification(String key) {
        this.key = key;
    }
}
