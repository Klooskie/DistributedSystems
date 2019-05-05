package client;

import Bank.*;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Client {

    public static Currency getCurrency(String currency) {
        if(currency.equals("PLN"))
            return Currency.PLN;
        else if (currency.equals("EUR"))
            return Currency.EUR;
        else if (currency.equals("USD"))
            return Currency.USD;
        else if (currency.equals("GBP"))
            return Currency.GBP;
        else if (currency.equals("JPY"))
            return Currency.JPY;
        else
            return null;
    }

    public static void main(String[] args) {
        System.out.println("Client started");

        try (Communicator communicator = Util.initialize(args)) {

            ObjectPrx base = communicator.stringToProxy("manager/AccountsManager:default -p 10000");
            AccountsManagerPrx accountsManager = AccountsManagerPrx.checkedCast(base);
            if (accountsManager == null) {
                throw new Error("Invalid proxy");
            }


            Scanner scanner = new Scanner(System.in);

            while(true){
                System.out.println("Choose operation: createAccount, checkAccountState, getTransfer, makeTransfer, calculateCreditCost");
                String operation = scanner.nextLine();

                if(operation.equals("createAccount") || operation.equals("CA")){
                    System.out.println("Write in separate lines firstName, secondName, pesel, startingBalance, monthlyEarnings");
                    String firstName = scanner.nextLine();
                    String secondName = scanner.nextLine();
                    String pesel = scanner.nextLine();
                    double startingBalance = Double.parseDouble(scanner.nextLine());
                    double monthlyEarnings = Double.parseDouble(scanner.nextLine());

                    // creating account
                    AccountsManager.CreateAccountResult newAccountResult = null;
                    try {
                        newAccountResult = accountsManager.createAccount(firstName, secondName, pesel, startingBalance, monthlyEarnings);
                    } catch (PeselUsedException e) {
                        System.out.println(e.message);
                        continue;
                    }

                    System.out.println("Account created, the password is \'" + newAccountResult.returnValue + "\'");
                    if(newAccountResult.type == AccountType.STANDARD)
                        System.out.println("Account type is STANDARD");
                    else
                        System.out.println("Account type is PREMIUM");
                }
                else if(operation.equals("checkAccountState") || operation.equals("CAS")){
                    System.out.println("Write in separate lines pesel, password");
                    String pesel = scanner.nextLine();
                    String password = scanner.nextLine();

                    // accessing account
                    Map<String, String> context = new HashMap<>();
                    context.put("password", password);
                    Account account;
                    try {
                        account = accountsManager.accessAccount(pesel, context);
                    } catch (PeselNotRegisteredException | WrongPasswordException e) {
                        System.out.println(e.message);
                        continue;
                    }

                    // checking account state
                    AccountState state = account.accountStateHandler.checkAccountState();
                    System.out.println("The state is: \n balance: " + state.balance + "\n monthlyEarnings: " + state.monthlyEarnings);
                }
                else if(operation.equals("getTransfer") || operation.equals("GT")){
                    System.out.println("Write in separate lines pesel, password, currency, value");
                    String pesel = scanner.nextLine();
                    String password = scanner.nextLine();
                    Currency currency = Client.getCurrency(scanner.nextLine());
                    if(currency == null){
                        System.out.println("Bad currency");
                        continue;
                    }
                    double value = Double.parseDouble(scanner.nextLine());

                    // accessing account
                    Map<String, String> context = new HashMap<>();
                    context.put("password", password);
                    Account account;
                    try {
                        account = accountsManager.accessAccount(pesel, context);
                    } catch (PeselNotRegisteredException | WrongPasswordException e) {
                        System.out.println(e.message);
                        continue;
                    }

                    // getting a transfer
                    account.accountStateHandler.getTransfer(currency, value);
                    System.out.println("Transfer got");
                }
                else if(operation.equals("makeTransfer") || operation.equals("MT")){
                    System.out.println("Write in separate lines pesel, password, currency, value");
                    String pesel = scanner.nextLine();
                    String password = scanner.nextLine();
                    Currency currency = Client.getCurrency(scanner.nextLine());
                    if(currency == null){
                        System.out.println("Bad currency");
                        continue;
                    }
                    double value = Double.parseDouble(scanner.nextLine());

                    // accessing account
                    Map<String, String> context = new HashMap<>();
                    context.put("password", password);
                    Account account;
                    try {
                        account = accountsManager.accessAccount(pesel, context);
                    } catch (PeselNotRegisteredException | WrongPasswordException e) {
                        System.out.println(e.message);
                        continue;
                    }

                    // making a transfer
                    try {
                        account.accountStateHandler.makeTransfer(currency, value);
                    } catch (NotEnoughMoneyException e) {
                        System.out.println(e.message);
                        continue;
                    }
                    System.out.println("Transfer made");
                }
                else if(operation.equals("calculateCreditCost") || operation.equals("CCC")){
                    System.out.println("Write in separate lines pesel, password, currency, valueOfCredit");
                    String pesel = scanner.nextLine();
                    String password = scanner.nextLine();
                    Currency currency = Client.getCurrency(scanner.nextLine());
                    if(currency == null){
                        System.out.println("Bad currency");
                        continue;
                    }
                    double value = Double.parseDouble(scanner.nextLine());

                    // accessing account
                    Map<String, String> context = new HashMap<>();
                    context.put("password", password);
                    Account account;
                    try {
                        account = accountsManager.accessAccount(pesel, context);
                    } catch (PeselNotRegisteredException | WrongPasswordException e) {
                        System.out.println(e.message);
                        continue;
                    }
                    if(account.type != AccountType.PREMIUM){
                        System.out.println("This option is blocked for standard accounts");
                        continue;
                    }

                    // casting handler proxy to PremiumAccountStateHandlerPrx
                    PremiumAccountStateHandlerPrx handler = PremiumAccountStateHandlerPrx.checkedCast(account.accountStateHandler);

                    // calculating credit cost
                    if(currency == Currency.PLN)
                        System.out.println("Credit cost is: " + handler.calculateCreditCost(currency, value).returnValue + " PLN");
                    else {
                        PremiumAccountStateHandler.CalculateCreditCostResult result = handler.calculateCreditCost(currency, value);
                        System.out.println("Credit cost is: " + result.returnValue + " PLN, (or " + result.costInForeignCurrency.getAsDouble() + " " + currency + ")");
                    }
                }
                else {
                    System.out.println("Try again");
                }

            }

        }

    }

}
