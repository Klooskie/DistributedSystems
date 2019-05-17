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
                .match(String.class, s -> {
                    if (s.equals("hi")) {
                        System.out.println("hello");
                    } else if (s.startsWith("m")) {
                        context().child("multiplyWorker").get().tell(s, getSelf()); // send task to child
                    } else if (s.startsWith("d")) {
                        context().child("divideWorker").get().tell(s, getSelf()); // send task to child
                    } else if (s.startsWith("result")) {
                        System.out.println(s);              // result from child
                    }
                })
                .match(CheckBookPriceRequest.class, checkBookPriceRequest -> {
                    System.out.println("checkBookPriceRequest");
                })
                .match(OrderBookRequest.class, orderBookRequest -> {
                    System.out.println("orderBookRequest");
                })
                .match(StreamBookRequest.class, streamBookRequest -> {
                    System.out.println("streamBookRequest");
                })
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }
}
