package responses;

import java.io.Serializable;

public class CheckBookPriceResponse implements Serializable {

    private String bookTitle;
    private boolean bookFound;
    private double bookPrice;

    public CheckBookPriceResponse(String bookTitle, boolean bookFound, double bookPrice) {
        this.bookTitle = bookTitle;
        this.bookFound = bookFound;
        this.bookPrice = bookPrice;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public boolean isBookFound() {
        return bookFound;
    }

    public double getBookPrice() {
        return bookPrice;
    }
}
