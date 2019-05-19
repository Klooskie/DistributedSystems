package responses;

import java.io.Serializable;

public class CheckBookPriceResponse implements Serializable {

    private boolean bookFound;
    private double bookPrice;

    public CheckBookPriceResponse(boolean bookFound, double bookPrice) {
        this.bookFound = bookFound;
        this.bookPrice = bookPrice;
    }

    public boolean isBookFound() {
        return bookFound;
    }

    public double getBookPrice() {
        return bookPrice;
    }
}
