package responses;

import java.io.Serializable;

public class StreamBookResponse implements Serializable {

    private String line;
    private boolean last;
    private boolean bookFound;

    public StreamBookResponse(String line, boolean last, boolean bookFound) {
        this.line = line;
        this.last = last;
        this.bookFound = bookFound;
    }

    public String getLine() {
        return line;
    }

    public boolean isLast() {
        return last;
    }

    public boolean isBookFound() {
        return bookFound;
    }
}
