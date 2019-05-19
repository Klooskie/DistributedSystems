package client;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class ClientApp {

    public static void main(String[] args) throws Exception {

        // wczyanie numeru portu z konsoli
        System.out.println("What port do you want the client actor system to use?");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int portNumber = Integer.parseInt(br.readLine());

        // wczytanie konfigu
        File configFile = new File("client.conf");
//        Config config = ConfigFactory.parseFile(configFile);
        Config config = ConfigFactory.parseString(String.format(
                "akka {\n" +
                        "  actor {\n" +
                        "    provider = remote\n" +
                        "    warn-about-java-serializer-usage = false\n" +
                        "  }\n" +
                        "  remote {\n" +
                        "    enabled-transports = [\"akka.remote.netty.tcp\"]\n" +
                        "    netty.tcp {\n" +
                        "      hostname = \"127.0.0.1\"\n" +
                        "      port = %d\n" +
                        "    }\n" +
                        "  }\n" +
                        "}", portNumber));

        // utworzenie systemu aktorow
        final ActorSystem system = ActorSystem.create("client_system_" + portNumber, config);

        // utworzenie aktora
        String bookshopPath = "akka.tcp://bookshop_system@127.0.0.1:33000/user/bookshop";
        final ActorRef client = system.actorOf(Props.create(ClientActor.class, bookshopPath), "client");

        // wczytywanie z konsoli i przesyłanie wiadomosci do aktora
        System.out.println("Usage: price book_title | order book_title | stream book_title");
        while (true) {
            String line = br.readLine();
            if (line.equals("q")) {
                break;
            }
            client.tell(line, null);
        }

        // zamkniecie systemu aktorow
        system.terminate();
    }
}