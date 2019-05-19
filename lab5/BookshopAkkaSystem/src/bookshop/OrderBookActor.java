package bookshop;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import requests.OrderBookRequest;
import responses.OrderBookResponse;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
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

                    if(Files.isRegularFile(Paths.get(this.ordersDatabasePath))) {

                        this.ordersDatabaseSemaphore.acquire();

                        BufferedWriter databaseWriter = new BufferedWriter(new FileWriter(ordersDatabasePath, true));
                        databaseWriter.write(orderBookRequest.getBookTitle() + "\n");
                        databaseWriter.close();

                        this.ordersDatabaseSemaphore.release();

                        log.info("placed order for " + orderBookRequest.getBookTitle() + " in order database properly, killing myself");
                        this.clientActor.tell(new OrderBookResponse(true), getSelf());
                        getSelf().tell(PoisonPill.getInstance(), getSelf());

                    }
                    else {

                        log.info("orders database does not exist, killing myself");
                        this.clientActor.tell(new OrderBookResponse(false), getSelf());
                        getSelf().tell(PoisonPill.getInstance(), getSelf());

                    }

                })
                .matchAny(o -> log.info("Received unknown message"))
                .build();
    }

}
