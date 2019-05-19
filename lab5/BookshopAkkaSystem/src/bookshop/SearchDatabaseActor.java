package bookshop;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import requests.CheckBookPriceRequest;
import responses.CheckBookPriceResponse;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

public class SearchDatabaseActor extends AbstractActor {

    private String databasePath;
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public SearchDatabaseActor(String databasePath) {
        this.databasePath = databasePath;
    }

    private double searchDatabase(String bookTitle) throws IOException {

        BufferedReader databaseReader = new BufferedReader(new FileReader(databasePath));
        String line = databaseReader.readLine();
        double bookPrice = -1;

        while(line != null) {

            String[] splitLine = line.split(Pattern.quote("|"));
            String title = splitLine[0].trim();

            if(title.equals(bookTitle)) {
                bookPrice = Double.parseDouble(splitLine[1].trim());
                break;
            }

            line = databaseReader.readLine();
        }

        databaseReader.close();
        return bookPrice;
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(CheckBookPriceRequest.class, checkBookPriceRequest -> {

                    log.info("got checkBookPriceRequest " + checkBookPriceRequest.getBookTitle() + " to search for it in base");
                    double bookPrice = searchDatabase(checkBookPriceRequest.getBookTitle());

                    if(bookPrice == -1)
                        sender().tell(new CheckBookPriceResponse(checkBookPriceRequest.getBookTitle(), false, -1), getSelf());
                    else
                        sender().tell(new CheckBookPriceResponse(checkBookPriceRequest.getBookTitle(), true, bookPrice), getSelf());

                })
                .matchAny(o -> log.info("Received unknown message"))
                .build();
    }
}
