package bookshop;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import requests.CheckBookPriceRequest;
import requests.OrderBookRequest;
import requests.StreamBookRequest;

public class BookshopActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(CheckBookPriceRequest.class, checkBookPriceRequest -> {
                    log.info("checkBookPriceRequest");
                    log.info(checkBookPriceRequest.getBookTitle());
                })
                .match(OrderBookRequest.class, orderBookRequest -> {
                    log.info("orderBookRequest");
                })
                .match(StreamBookRequest.class, streamBookRequest -> {
                    System.out.println("streamBookRequest");
                })
                .matchAny(o -> log.info("Received unknown message"))
                .build();
    }
}
