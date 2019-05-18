package requests;

import java.io.Serializable;

public class CheckBookPriceRequest implements Serializable {

    private String bookTitle;

    public CheckBookPriceRequest(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookTitle() {
        return bookTitle;
    }
}
