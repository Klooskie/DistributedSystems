package bookshop;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.DeciderBuilder;
import requests.CheckBookPriceRequest;
import requests.OrderBookRequest;
import requests.StreamBookRequest;
import responses.CheckBookPriceResponse;
import scala.concurrent.duration.Duration;

import java.io.FileNotFoundException;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import static akka.actor.SupervisorStrategy.*;

public class BookshopActor extends AbstractActor {

    private String firstBooksDatabasePath;
    private String secondBooksDatabasePath;
    private String ordersDatabasePath;
    private String booksDirectoryPath;

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private Semaphore ordersDatabaseSemaphore = new Semaphore(1);

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
                    log.info("got orderBookRequest " + orderBookRequest.getBookTitle());

                    String actorName = "order_book_actor_" + UUID.randomUUID().toString();
                    context().actorOf(
                            Props.create(
                                    OrderBookActor.class,
                                    context().sender(),
                                    this.ordersDatabasePath,
                                    this.ordersDatabaseSemaphore
                            ),
                            actorName)
                            .tell(orderBookRequest, getSelf());
                })
                .match(StreamBookRequest.class, streamBookRequest -> {
                    System.out.println("got streamBookRequest " + streamBookRequest.getBookTitle());

                    String actorName = "stream_book_actor_" + UUID.randomUUID().toString();
                    context().actorOf(
                            Props.create(
                                    StreamBookActor.class,
                                    context().sender(),
                                    this.booksDirectoryPath
                            ),
                            actorName)
                            .tell(streamBookRequest, getSelf());
                })
                .matchAny(o -> log.info("Received unknown message"))
                .build();
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return new OneForOneStrategy(10, Duration.create(1, "minute"),
                DeciderBuilder
                        .matchAny(o -> stop())
                        .build()
        );
    }
}
