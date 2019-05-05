module Bank
{
    enum Currency
    {
        PLN = 0,
        EUR = 1,
        USD = 2,
        GBP = 3,
        JPY = 4
    };

    enum AccountType
    {
        STANDARD = 0,
        PREMIUM = 1
    };


    // Exceptions

    exception GenericException { string message; };
    exception PeselUsedException extends GenericException {};
    exception PeselNotRegisteredException extends GenericException {};
    exception WrongPasswordException extends GenericException {};
    exception NotSupportedCurrencyException extends GenericException {};
    exception NotEnoughMoneyException extends GenericException {};


    // Account state handlers

    struct AccountState
    {
        double balance;
        double monthlyEarnings;
    };

    interface AccountStateHandler
    {
        AccountState checkAccountState();
        void getTransfer(Currency currency, double value);
        void makeTransfer(Currency currency, double value)
            throws NotEnoughMoneyException;
    };

    interface PremiumAccountStateHandler extends AccountStateHandler
    {
        double calculateCreditCost(Currency currency, double value, out optional(1) double costInForeignCurrency);
    };


    // Accounts management

    struct Account
    {
        string firstName;
        string secondName;
        string pesel;
        AccountType type;
        AccountStateHandler * accountStateHandler;
    };

    interface AccountsManager
    {
        string createAccount(string firstName, string secondName, string pesel, double startingBalance, double monthlyEarnings, out AccountType type)
            throws PeselUsedException;
        Account accessAccount(string pesel)
            throws WrongPasswordException, PeselNotRegisteredException;
    };

};