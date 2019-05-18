package bookshop;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import requests.CheckBookPriceRequest;
import requests.OrderBookRequest;
import requests.StreamBookRequest;

import java.util.UUID;

public class BookshopActor extends AbstractActor {

    private String firstBooksDatabasePath;
    private String secondBooksDatabasePath;
    private String ordersDatabasePath;
    private String booksDirectoryPath;
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public BookshopActor(String firstBooksDatabasePath, String secondBooksDatabasePath, String ordersDatabasePath, String booksDirectoryPath) {
        this.firstBooksDatabasePath = firstBooksDatabasePath;
        this.secondBooksDatabasePath = secondBooksDatabasePath;
        this.ordersDatabasePath = ordersDatabasePath;
        this.booksDirectoryPath = booksDirectoryPath;
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(CheckBookPriceRequest.class, checkBookPriceRequest -> {
                    log.info("got checkBookPriceRequest " + checkBookPriceRequest.getBookTitle());

                    String actorName = "check_book_price_actor_" + UUID.randomUUID().toString();
                    context().actorOf(
                            Props.create(
                                    CheckBookPriceActor.class,
                                    context().sender(),
                                    this.firstBooksDatabasePath,
                                    this.secondBooksDatabasePath
                            ),
                            actorName)
                            .tell(checkBookPriceRequest, getSelf());
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
