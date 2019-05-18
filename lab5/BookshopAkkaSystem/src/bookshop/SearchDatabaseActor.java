package bookshop;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import requests.CheckBookPriceRequest;
import responses.CheckBookPriceResponse;

public class SearchDatabaseActor extends AbstractActor {

    private String databasePath;
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public SearchDatabaseActor(String databasePath) {
        this.databasePath = databasePath;
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(CheckBookPriceRequest.class, checkBookPriceRequest -> {

                    log.info("got checkBookPriceRequest " + checkBookPriceRequest.getBookTitle() + "to search for it in base");



                    sender().tell(new CheckBookPriceResponse(checkBookPriceRequest.getBookTitle(), true, 12), getSelf());


                })
                .matchAny(o -> log.info("Received unknown message"))
                .build();
    }
}
