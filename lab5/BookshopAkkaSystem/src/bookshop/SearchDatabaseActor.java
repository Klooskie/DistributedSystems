package bookshop;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import requests.CheckBookPriceRequest;
import responses.CheckBookPriceResponse;

import java.io.BufferedReader;
import java.io.FileReader;

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

                    log.info("got checkBookPriceRequest " + checkBookPriceRequest.getBookTitle() + " to search for it in base");

                    BufferedReader baseReader = new BufferedReader(new FileReader(databasePath));
                    System.out.println(baseReader.readLine());

                    sender().tell(new CheckBookPriceResponse(checkBookPriceRequest.getBookTitle(), true, 12), getSelf());


                })
                .matchAny(o -> log.info("Received unknown message"))
                .build();
    }
}
