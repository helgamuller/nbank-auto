package requests.steps;

import models.*;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.skeleton.requesters.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static java.math.RoundingMode.HALF_UP;

public class UserSteps {

    public static int createAccountAndGetId(CreateUserRequest user){
        return new CrudRequester(
                RequestSpecs.authAsUserSpec(user.getUsername(), user.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .path("id");
    }

    public static BigDecimal getAccountBalance(CreateUserRequest userRequest, int accountId){
        CreateAccountResponse[] accountResponse = new ValidatedCrudRequester<CreateAccountResponse[]>(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpecs.requestReturnsOk())
                .getAll();

        return Arrays.stream(accountResponse)          // можно даже без intermediate List
                .filter(a -> a.getId() == accountId)
                .map(CreateAccountResponse::getBalance)
                .findFirst()
                .orElseThrow()
                .setScale(2, HALF_UP);
        //create a list of objects from array
//        List<CreateAccountResponse> accountList =
//                Arrays.stream(accountResponse)          // поток элементов массива в список
//                        .toList();
//
//        //fetch balance from account
//        BigDecimal balance = accountList.stream()
//                .filter(account->account.getId()==accountId)
//                .map(CreateAccountResponse::getBalance)
//                .findFirst()
//                .orElseThrow()
//                .setScale(2, HALF_UP);

//        return balance;
    }
    public static void makeDeposit(BigDecimal amount, int accountId, CreateUserRequest userRequest){
        //make a deposit request
        MakeDepositRequest makeDepositRequest = MakeDepositRequest.builder()
                .id(accountId)
                .balance(amount)
                .build();

        //CreateAccountResponse makeDeposit =
        new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNTS_DEPOSIT,
                ResponseSpecs.requestReturnsOk())
                .post(makeDepositRequest);

    }
    public static List<Transaction> getAccountTransactions(CreateUserRequest userRequest, int accountId){
        Transaction[] txnArr =
                new ValidatedCrudRequester<Transaction[]>(
                        RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                        Endpoint.ACCOUNT_TRANSACTIONS,
                        ResponseSpecs.requestReturnsOk())
                        .get(accountId);

        return  Arrays.asList(txnArr);
    }
    public static void makeTransfer(BigDecimal transferAmount, int senderAccountId, int receiverAccountId, CreateUserRequest userRequest){
        MakeTransferRequest transferRequest = MakeTransferRequest.builder()
                .amount(transferAmount)
                .receiverAccountId(receiverAccountId)
                .senderAccountId(senderAccountId)
                .build();

        //send transfer and get response as a class
         new ValidatedCrudRequester<MakeTransferResponse>(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNT_TRANSFER,
                ResponseSpecs.requestReturnsOk())
                .post(transferRequest);
    }
    public static Transaction getTransferFromTransactions(CreateUserRequest userRequest,
                                                          int accountId, int counterPartyId, BigDecimal transferAmount, TransactionType type){
        List <Transaction> senderAccountTransactions = UserSteps.getAccountTransactions(userRequest, accountId);

        return senderAccountTransactions.stream()
                .filter(t -> t.getType() == type)
                .filter(t -> t.getAmount().setScale(2, HALF_UP) .compareTo(transferAmount.setScale(2,HALF_UP)) == 0)
                .filter(t -> t.getRelatedAccountId() == counterPartyId)
                .findFirst()
                .orElse(null);
    }
}
