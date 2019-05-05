package server;

import com.zeroc.Ice.*;
import Bank.Currency;

import java.util.HashMap;
import java.util.Map;

public class BankService {

    public static void main(String[] args) {

        Map<Currency, Double> currencies = new HashMap<>();
        currencies.put(Currency.EUR, 4.28);
        currencies.put(Currency.USD, 3.81);
        currencies.put(Currency.GBP, 5.03);
        currencies.put(Currency.JPY, 0.03);

        try(Communicator communicator = Util.initialize(args))
        {

            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("BankAdapter", "default -p 10000");
            AccountsManagerI servant = new AccountsManagerI(currencies);

            adapter.add(servant, new Identity("AccountsManager", "manager"));
            adapter.activate();
            communicator.waitForShutdown();

        }

    }
}
