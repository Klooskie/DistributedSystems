package requests;

import java.io.Serializable;

public class CheckBookPriceRequest extends Request implements Serializable {

    public CheckBookPriceRequest(String bookTitle) {
        super(bookTitle);
    }

}
