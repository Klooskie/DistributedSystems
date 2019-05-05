package server;

import com.zeroc.Ice.*;
import Bank.Currency;

import java.lang.Exception;
import java.util.*;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Message;
import exchange.*;
//import exchange.Currency;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import exchange.ExchangeGrpc.ExchangeBlockingStub;
import exchange.ExchangeGrpc.ExchangeStub;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


public class BankService {

//    private final ManagedChannel channel;
//    private final ExchangeBlockingStub blockingStub;
//    private final ExchangeStub asyncStub;
//
//    public BankService(String host, int port) {
//        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext());
//    }
//
//    public BankService(ManagedChannelBuilder<?> channelBuilder) {
//        channel = channelBuilder.build();
//        blockingStub = ExchangeGrpc.newBlockingStub(channel);
//        asyncStub = ExchangeGrpc.newStub(channel);
//    }
//
//    public void shutdown() {
//        try {
//            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * Blocking server-streaming example. Calls listFeatures with a rectangle of interest. Prints each
//     * response feature as it arrives.
//     */
//    public void subscribeToExchange() {
//
//        List<Currency> listOfSupportedCurrencies = new LinkedList<>();
//        listOfSupportedCurrencies.add(Currency.EUR);
//        listOfSupportedCurrencies.add(Currency.USD);
//
//        CurrenciesList currenciesList = CurrenciesList.newBuilder().addAllCurrencies(listOfSupportedCurrencies).build();
//
////        Iterator<CurrencyUpdate> currencyUpdates;
//        CurrencyUpdate currencyUpdate;
//
//        try {
//            currencyUpdate = blockingStub.subscribeToExchange(currenciesList);
//
//            CurrencyValue newCurrencyValue = currencyUpdate.getNewCurrencyValue();
//            System.out.println(newCurrencyValue.getCurrency() + "   " + newCurrencyValue.getCurrencyValue());
//
////            for (int i = 1; currencyUpdates.hasNext(); i++) {
////
////                CurrencyUpdate update = currencyUpdates.next();
////                CurrencyValue newCurrencyValue = update.getNewCurrencyValue();
////                System.out.println(newCurrencyValue.getCurrency() + "   " + newCurrencyValue.getCurrencyValue());
////            }
//
//        } catch (StatusRuntimeException e) {
//            System.out.println(":(");
//        }
//    }
//
//    public static void main(String[] args) {
//
//        BankService client = new BankService("localhost", 50051);
//        try {
//
//            client.subscribeToExchange();
//
//        } finally {
//            client.shutdown();
//        }
//
//    }

    public static void main(String[] args) {

        Map<Currency, Double> currencies = new HashMap<>();
        currencies.put(Currency.EUR, 4.28);
        currencies.put(Currency.USD, 3.81);
        currencies.put(Currency.GBP, 5.03);
        currencies.put(Currency.JPY, 0.03);

        Scanner scanner = new Scanner(System.in);
        System.out.println("Insert the port");
        Integer portNumber = Integer.parseInt(scanner.nextLine());
        String endpoints = "default -p " + portNumber.toString();

        try(Communicator communicator = Util.initialize(args))
        {

            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("BankAdapter", endpoints);
            AccountsManagerI servant = new AccountsManagerI(currencies);

            adapter.add(servant, new Identity("AccountsManager", "manager"));
            adapter.activate();
            System.out.println("Bank service started");
            communicator.waitForShutdown();
        }

    }
}
