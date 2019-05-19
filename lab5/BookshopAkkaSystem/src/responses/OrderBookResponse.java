package responses;

import java.io.Serializable;

public class OrderBookResponse implements Serializable {

    private boolean orderedProperly;

    public OrderBookResponse(boolean orderedProperly) {
        this.orderedProperly = orderedProperly;
    }

    public boolean isOrderedProperly() {
        return orderedProperly;
    }
}
