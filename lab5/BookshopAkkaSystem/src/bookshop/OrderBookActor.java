package bookshop;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import requests.CheckBookPriceRequest;
import requests.OrderBookRequest;
import responses.CheckBookPriceResponse;
import responses.OrderBookResponse;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.concurrent.Semaphore;

public class OrderBookActor extends AbstractActor {

    private ActorRef clientActor;
    private String ordersDatabasePath;
    private Semaphore ordersDatabaseSemaphore;

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public OrderBookActor(ActorRef clientActor, String ordersDatabasePath, Semaphore ordersDatabaseSemaphore) {
        this.clientActor = clientActor;
        this.ordersDatabasePath = ordersDatabasePath;
        this.ordersDatabaseSemaphore = ordersDatabaseSemaphore;
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(OrderBookRequest.class, orderBookRequest -> {
                    log.info("got orderBookRequest " + orderBookRequest.getBookTitle() + " to write it to orders db");

                    this.ordersDatabaseSemaphore.acquire();

                    try {

                        BufferedWriter databaseWriter = new BufferedWriter(new FileWriter(ordersDatabasePath, true));
                        databaseWriter.write(orderBookRequest.getBookTitle() + "\n");
                        databaseWriter.close();

                    }
                    catch (Exception e) {
                        throw e;
                    }
                    finally {
                        this.ordersDatabaseSemaphore.release();
                    }

                    this.clientActor.tell(new OrderBookResponse(true), getSelf());
                    getSelf().tell(PoisonPill.getInstance(), getSelf());

                })
                .matchAny(o -> log.info("Received unknown message"))
                .build();
    }

}
