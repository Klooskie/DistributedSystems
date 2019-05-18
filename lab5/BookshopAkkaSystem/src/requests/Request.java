package requests;

import java.io.Serializable;

public abstract class Request implements Serializable {

    private String bookTitle;

    Request(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookTitle() {
        return bookTitle;
    }

}
