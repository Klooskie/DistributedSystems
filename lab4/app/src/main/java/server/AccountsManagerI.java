package server;

import Bank.*;
import com.zeroc.Ice.Current;
import com.zeroc.Ice.Identity;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AccountsManagerI implements AccountsManager {

    private List<Account> accounts;
    private Map<String, String> authentication;
    private int freePassword;

    Map<Currency, Double> currencies;

    public AccountsManagerI(Map<Currency, Double> currencies) {
        this.accounts = new LinkedList<>();
        this.authentication = new HashMap<>();
        this.freePassword = 1;
        this.currencies = currencies;
    }

    private String generatePassword() {
        String password = Integer.toString(freePassword);
        freePassword++;
        return password;
    }


    @Override
    public CreateAccountResult createAccount(String firstName, String secondName, String pesel, double startingBalance, double monthlyEarnings, Current current) throws PeselUsedException {

        Account newAccount = new Account();

        if (authentication.containsKey(pesel)) {
            throw new PeselUsedException("this pesel is already registered");
        }

        newAccount.firstName = firstName;
        newAccount.secondName = secondName;
        newAccount.pesel = pesel;

        if (monthlyEarnings < 1000) {
            newAccount.type = AccountType.STANDARD;

            AccountStateHandlerPrx newAccountStateHandlerProxy = AccountStateHandlerPrx.uncheckedCast(
                    current.adapter.add(
                            new AccountStateHandlerI(startingBalance, monthlyEarnings, currencies),
                            new Identity(pesel, "standard")
                    )
            );
            newAccount.accountStateHandler = newAccountStateHandlerProxy;
        } else {
            newAccount.type = AccountType.PREMIUM;

            PremiumAccountStateHandlerPrx newAccountStateHandlerProxy = PremiumAccountStateHandlerPrx.uncheckedCast(
                    current.adapter.add(
                            new PremiumAccountStateHandlerI(startingBalance, monthlyEarnings, currencies),
                            new Identity(pesel, "premium")
                    )
            );
            newAccount.accountStateHandler = newAccountStateHandlerProxy;
        }

        this.accounts.add(newAccount);
        String password = generatePassword();
        this.authentication.put(pesel, password);
        return new CreateAccountResult(password, newAccount.type);
    }

    @Override
    public Account accessAccount(String pesel, Current current) throws WrongPasswordException, PeselNotRegisteredException {
        if (authentication.containsKey(pesel)) {
            if (authentication.get(pesel).equals(current.ctx.get("password"))) {
                Account account = null;
                for (Account a : accounts)
                    if (a.pesel.equals(pesel))
                        account = a;

                return account;
            } else {
                throw new WrongPasswordException("wrong password");
            }
        } else {
            throw new PeselNotRegisteredException("this pesel was not registered");
        }
    }

}
