package client;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import responses.CheckBookPriceResponse;
import responses.OrderBookResponse;
import responses.StreamBookResponse;

public class ClientActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(String.class, request -> {
                    if (request.startsWith("check") || request.startsWith("price")) {
                        log.info("check price xd");
                    } else if (request.startsWith("order")) {
                        log.info("order xd");
//                        getContext().actorSelection("akka.tcp://bookshop_system@127.0.0.1:8002/user/bookshop").tell(s, getSelf());
                    } else if (request.startsWith("stream")) {
                        log.info("strum xd");
                    } else {
                        log.info("Received wrong request");
                    }
                })
                .match(CheckBookPriceResponse.class, checkBookPriceResponse -> {
                    log.info("CheckBookPriceResponse yay");
                })
                .match(OrderBookResponse.class, orderBookResponse -> {
                    log.info("OrderBookResponse yay");
                })
                .match(StreamBookResponse.class, streamBookResponse -> {
                    log.info("StreamBookResponse yay");
                })
                .matchAny(o -> log.info("Received unknown message"))
                .build();
    }
}
