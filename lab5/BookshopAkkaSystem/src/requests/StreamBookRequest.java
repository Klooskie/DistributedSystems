package requests;

import java.io.Serializable;

public class StreamBookRequest extends Request implements Serializable {

    public StreamBookRequest(String bookTitle) {
        super(bookTitle);
    }

}
