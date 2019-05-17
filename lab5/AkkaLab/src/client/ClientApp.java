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

        // wczytanie konfigu
        File configFile = new File("remote_app.conf");
        Config config = ConfigFactory.parseFile(configFile);

        // utworzenie systemu aktorow
        final ActorSystem system = ActorSystem.create("client_system", config);

        // utworzenie aktora
        final ActorRef client = system.actorOf(Props.create(ClientActor.class), "client");

        // wczytywanie z konsoli i przesyłanie wiadomosci do aktora
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
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