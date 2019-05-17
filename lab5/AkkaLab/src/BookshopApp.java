import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class BookshopApp {

    public static void main(String[] args) throws Exception {

        // wczytanie konfigu
        File configFile = new File("remote_app2.conf");
        Config config = ConfigFactory.parseFile(configFile);

        // utworzenie systemu aktorow
        final ActorSystem system = ActorSystem.create("bookshop_system", config);


        // utworzenie aktora
        final ActorRef bookshop = system.actorOf(Props.create(BookshopActor.class), "bookshop");

        // zamkniecie po przeczytaniu z terminala dowolnego stringa
        (new BufferedReader(new InputStreamReader(System.in))).readLine();
        system.terminate();
    }
}
