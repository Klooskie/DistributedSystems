package server;

import Bank.AccountStateHandler;
import Bank.AccountState;
import Bank.Currency;
import Bank.NotEnoughMoneyException;
import com.zeroc.Ice.Current;

import java.util.Map;


public class AccountStateHandlerI implements AccountStateHandler {

    private AccountState accountState;
    Map<Currency, Double> currencies;

    AccountStateHandlerI(double startingBalance, double monthlyEarnings, Map<Currency, Double> currencies) {
        this.accountState = new AccountState(startingBalance, monthlyEarnings);
        this.currencies = currencies;
    }

    @Override
    public AccountState checkAccountState(Current current) {
        return this.accountState;
    }

    @Override
    public void getTransfer(Currency currency, double value, Current current) {

        if(currency != Currency.PLN)
            value *= this.currencies.get(currency);

        //TODO bank nie obsluguje waluty

        System.out.println("Transfer goten by user");

        this.accountState.balance += value;
    }

    @Override
    public void makeTransfer(Currency currency, double value, Current current) throws NotEnoughMoneyException {

        if(currency != Currency.PLN)
            value *= this.currencies.get(currency);

        //TODO bank nie obsluguje waluty

        if(value > this.accountState.balance)
            throw new NotEnoughMoneyException("you dont have enough money");

        System.out.println("Transfer made by user");

        this.accountState.balance -= value;
    }

}
