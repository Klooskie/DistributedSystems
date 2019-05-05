package server;

import Bank.Currency;
import Bank.PremiumAccountStateHandler;
import com.zeroc.Ice.Current;

import java.util.Map;
import java.util.OptionalDouble;

public class PremiumAccountStateHandlerI extends AccountStateHandlerI implements PremiumAccountStateHandler {

    PremiumAccountStateHandlerI(double startingBalance, double monthlyEarnings, Map<Currency, Double> currencies) {
        super(startingBalance, monthlyEarnings, currencies);
    }

    @Override
    public CalculateCreditCostResult calculateCreditCost(Currency currency, double value, Current current) {

        //TODO bank nie obsluguje waluty

        CalculateCreditCostResult result;

        if(currency == Currency.PLN)
            result = new CalculateCreditCostResult(value * 2, OptionalDouble.empty());
        else {
            double pln_value = value * this.currencies.get(currency) * 2;
            result = new CalculateCreditCostResult(pln_value, value * 2);
        }

        System.out.println("Credit cost calculated for premium user");

        return result;
    }
}
