package bookshop;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import requests.CheckBookPriceRequest;
import responses.CheckBookPriceResponse;

public class CheckBookPriceActor extends AbstractActor {

    private ActorRef clientActor;
    private String firstBooksDatabasePath;
    private String secondBooksDatabasePath;
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private ActorRef firstDatabaseSearchActor;
    private ActorRef secondDatabaseSearchActor;
    private int databasesSearched;


    public CheckBookPriceActor(ActorRef clientActor, String firstBooksDatabasePath, String secondBooksDatabasePath) {
        this.clientActor = clientActor;
        this.firstBooksDatabasePath = firstBooksDatabasePath;
        this.secondBooksDatabasePath = secondBooksDatabasePath;
        this.databasesSearched = 0;
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(CheckBookPriceRequest.class, checkBookPriceRequest -> {
                    log.info("got checkBookPriceRequest " + checkBookPriceRequest.getBookTitle());

                    // ustalenie nazw aktorów do przeszukiwania bazy
                    String firstDatabaseSearchActorName = "first_database_search_actor";
                    String secondDatabaseSearchActorName = "second_database_search_actor";

                    // stworzenie aktora do przeszukiwania pierwszej bazy i zlecenie mu requesta
                    this.firstDatabaseSearchActor = context()
                            .actorOf(Props.create(SearchDatabaseActor.class, this.firstBooksDatabasePath),
                                    firstDatabaseSearchActorName);
                    this.firstDatabaseSearchActor.tell(checkBookPriceRequest, getSelf());

                    // stworzenie aktora do przeszukiwania drugiej bazy i zlecenie mu requesta
                    this.secondDatabaseSearchActor = context()
                            .actorOf(Props.create(SearchDatabaseActor.class, this.secondBooksDatabasePath),
                                    secondDatabaseSearchActorName);
                    this.secondDatabaseSearchActor.tell(checkBookPriceRequest, getSelf());

                })
                .match(CheckBookPriceResponse.class, checkBookPriceResponse -> {

                    this.databasesSearched++;

                    if(this.databasesSearched == 2 || checkBookPriceResponse.isBookFound()) {
                        this.clientActor.tell(checkBookPriceResponse, getSelf());
                        //TODO pozabijaj podwladnych i siebie
//                        this.firstDatabaseSearchActor.tell(PoisonPill.getInstance(), getSelf());
//                        this.secondDatabaseSearchActor.tell(PoisonPill.getInstance(), getSelf());
//                        getSelf().tell(PoisonPill.getInstance(), getSelf());
                    }

//                    this.clientActor.tell(checkBookPriceResponse, getSelf());

                    //TODO pozabijaj podwladnych i siebie
//                    this.firstDatabaseSearchActor.tell(PoisonPill.getInstance(), getSelf());
//                    this.secondDatabaseSearchActor.tell(PoisonPill.getInstance(), getSelf());
//                    getSelf().tell(PoisonPill.getInstance(), getSelf());

                })
                .matchAny(o -> log.info("Received unknown message"))
                .build();
    }
}
