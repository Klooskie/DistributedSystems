package client;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import requests.CheckBookPriceRequest;
import requests.OrderBookRequest;
import requests.StreamBookRequest;
import responses.CheckBookPriceResponse;
import responses.OrderBookResponse;
import responses.StreamBookResponse;

public class ClientActor extends AbstractActor {

    private String bookshopPath;
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public ClientActor(String bookshopPath) {
        this.bookshopPath = bookshopPath;
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(String.class, request -> {
                    if (request.startsWith("price")) {

                        CheckBookPriceRequest checkBookPriceRequest = new CheckBookPriceRequest(request.replaceFirst("price", "").trim());
                        getContext().actorSelection(this.bookshopPath).tell(checkBookPriceRequest, getSelf());

                        log.info("Sent CheckBookPriceRequest");
                    } else if (request.startsWith("order")) {

                        OrderBookRequest orderBookRequest = new OrderBookRequest(request.replaceFirst("order", "").trim());
                        getContext().actorSelection(this.bookshopPath).tell(orderBookRequest, getSelf());

                        log.info("Sent OrderBookRequest");
                    } else if (request.startsWith("stream")) {

                        StreamBookRequest streamBookRequest = new StreamBookRequest(request.replaceFirst("stream", "").trim());
                        getContext().actorSelection(this.bookshopPath).tell(streamBookRequest, getSelf());

                        log.info("Sent StreamBookRequest");
                    } else {
                        log.info("Received wrong request");
                    }
                })
                .match(CheckBookPriceResponse.class, checkBookPriceResponse -> {
                    log.info("Got CheckBookPriceResponse");
                    if(checkBookPriceResponse.isBookFound())
                        System.out.println("This book price is: " + checkBookPriceResponse.getBookPrice());
                    else
                        System.out.println("The requested boook was not found");

                })
                .match(OrderBookResponse.class, orderBookResponse -> {
                    log.info("Got OrderBookResponse");
                    if(orderBookResponse.isOrderedProperly())
                        System.out.println("The book has been ordered properly");
                    else
                        System.out.println("No order has been made");
                })
                .match(StreamBookResponse.class, streamBookResponse -> {
                    log.info("StreamBookResponse yay");


                })
                .matchAny(o -> log.info("Received unknown message"))
                .build();
    }
}
