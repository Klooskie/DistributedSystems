import org.jgroups.JChannel;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.GMS;
import org.jgroups.protocols.pbcast.NAKACK2;
import org.jgroups.protocols.pbcast.STABLE;
import org.jgroups.stack.ProtocolStack;

import java.net.InetAddress;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        System.setProperty("java.net.preferIPv4Stack", "true");

        System.out.println("Start");

        try {

            JChannel communicationChannel = new JChannel(false);

            ProtocolStack stack = new ProtocolStack();
            communicationChannel.setProtocolStack(stack);

            stack.addProtocol(new UDP().setValue("mcast_group_addr", InetAddress.getByName("224.1.2.3")))
                    .addProtocol(new PING())
                    .addProtocol(new MERGE3())
                    .addProtocol(new FD_SOCK())
                    .addProtocol(new FD_ALL().setValue("timeout", 12000).setValue("interval", 3000))
                    .addProtocol(new VERIFY_SUSPECT())
                    .addProtocol(new BARRIER())
                    .addProtocol(new NAKACK2())
                    .addProtocol(new UNICAST3())
                    .addProtocol(new STABLE())
                    .addProtocol(new GMS())
                    .addProtocol(new UFC())
                    .addProtocol(new MFC())
                    .addProtocol(new FRAG2());
            stack.init();

            communicationChannel.connect("distributed_map_cluster");
//            communicationChannel.setDiscardOwnMessages(true);

            DistributedMap distributedMap = new DistributedMap(communicationChannel);


            Scanner scanner = new Scanner(System.in);
            String operation = "";

            while (true) {
                System.out.println("Wpisz jaka operacje chcesz wykonac lub quit? (containsKey|get|put|remove)");
                operation = scanner.nextLine();

                if (operation.equals("containsKey")) {
                    System.out.println("Wpisz klucz");
                    String key = scanner.nextLine();
                    System.out.println(distributedMap.containsKey(key));
                } else if (operation.equals("get")) {
                    System.out.println("Wpisz klucz");
                    String key = scanner.nextLine();
                    System.out.println(distributedMap.get(key));
                } else if (operation.equals("put")) {
                    System.out.println("Wpisz klucz");
                    String key = scanner.nextLine();
                    System.out.println("Wpisz wartosc");
                    int value = scanner.nextInt();
                    scanner.nextLine();
                    distributedMap.put(key, value);
                    System.out.println("Dodano");
                } else if (operation.equals("remove")) {
                    System.out.println("Wpisz klucz");
                    String key = scanner.nextLine();
                    distributedMap.remove(key);
                    System.out.println("Usunieto");
                } else if (operation.equals("quit")) {
                    System.out.println("Wylaczanie");
                    break;
                } else {
                    System.out.println("Niepoprawna operacja, sprobuj ponownie");
                }
            }

            communicationChannel.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
