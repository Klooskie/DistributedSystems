package requests;

import java.io.Serializable;

public class OrderBookRequest extends Request implements Serializable {

    public OrderBookRequest(String bookTitle) {
        super(bookTitle);
    }

}
