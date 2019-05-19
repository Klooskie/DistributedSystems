package bookshop;

import akka.NotUsed;
import akka.stream.IOResult;
import akka.util.ByteString;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.ThrottleMode;
import akka.stream.javadsl.*;
import requests.StreamBookRequest;
import responses.StreamBookResponse;
import scala.concurrent.duration.Duration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletionStage;


public class StreamBookActor extends AbstractActor {

    private ActorRef clientActor;
    private String booksDirectoryPath;

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public StreamBookActor(ActorRef clientActor, String booksDirectoryPath) {
        this.clientActor = clientActor;
        this.booksDirectoryPath = booksDirectoryPath;
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(StreamBookRequest.class, streamBookRequest -> {
                    log.info("got orderBookRequest " + streamBookRequest.getBookTitle() + " to stream it");

                    Path requestedBookPath = Paths.get(this.booksDirectoryPath + streamBookRequest.getBookTitle());

                    if(Files.isRegularFile(requestedBookPath)) {

                        final Materializer materializer = ActorMaterializer.create(context().system());
                        final Source<ByteString, CompletionStage<IOResult>> bookSource = FileIO.fromPath(requestedBookPath);
                        final Flow<ByteString, ByteString, NotUsed> getBookLines = Framing.delimiter(ByteString.fromString("\n"), 10000, FramingTruncation.ALLOW);

                        bookSource
                                .via(getBookLines)
                                .map(ByteString::utf8String)
                                .map(line -> new StreamBookResponse(line, false, true))
                                .concat(Source.single(new StreamBookResponse("", true, true)))
                                .throttle(1, Duration.create(1, "seconds"), 1, ThrottleMode.shaping())
                                .runWith(Sink.foreach(response -> clientActor.tell(response, getSelf())), materializer);

                        log.info("Completed streaming " + streamBookRequest.getBookTitle() + " killing myself");
                        getSelf().tell(PoisonPill.getInstance(), getSelf());

                    }
                    else {

                        log.info("Requested book does not exist, killing myself");
                        clientActor.tell(new StreamBookResponse("", true, false), getSelf());
                        getSelf().tell(PoisonPill.getInstance(), getSelf());

                    }

                })
                .matchAny(o -> log.info("Received unknown message"))
                .build();
    }

}
