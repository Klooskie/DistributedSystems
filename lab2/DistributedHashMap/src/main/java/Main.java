import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {

        System.setProperty("java.net.preferIPv4Stack", "true");
        System.out.println("Start");

        DistributedMap distributedMap = new DistributedMap();

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
                String value = scanner.nextLine();
                try {
                    distributedMap.put(key, Integer.parseInt(value));
                    System.out.println("Dodano");
                } catch (NumberFormatException e) {
                    System.out.println("To nie liczba!!");
                    continue;
                }
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

        distributedMap.close();

    }

}
