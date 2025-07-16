package api.requests.steps;

import api.generators.RandomModelGenerator;
import api.models.*;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import com.codeborne.selenide.Selenide;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static com.codeborne.selenide.Selenide.executeJavaScript;
import static java.math.RoundingMode.HALF_UP;


public class UserSteps {
    private String username;
    private String password;

    public UserSteps(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public List<CreateAccountResponse> getAllAccounts(){
        return  new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUserSpec(username, password),
                Endpoint.CUSTOMER_ACCOUNTS_ALTER,
                ResponseSpecs.requestReturnsOk())
                .getAll(CreateAccountResponse[].class);
    }

    public static int createAccountAndGetId(CreateUserRequest user){
        return new CrudRequester(
                RequestSpecs.authAsUserSpec(user.getUsername(), user.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .path("id");
    }

    public static String createAccountAndGetNumber(CreateUserRequest user){
        return new CrudRequester(
                RequestSpecs.authAsUserSpec(user.getUsername(), user.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .path("accountNumber");
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
                .setScale(2, HALF_UP);}

        public static BigDecimal getAccountBalanceByAccNumber(CreateUserRequest userRequest, String accNumber){
            CreateAccountResponse[] accountResponse = new ValidatedCrudRequester<CreateAccountResponse[]>(
                    RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                    Endpoint.CUSTOMER_ACCOUNTS,
                    ResponseSpecs.requestReturnsOk())
                    .getAll();

            return Arrays.stream(accountResponse)          // можно даже без intermediate List
                    .filter(a -> a.getAccountNumber().equals(accNumber))
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
    public static void saveAuthHeaderToLocalStorage(CreateUserRequest user){
        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOk())
                .post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build())
                .extract()
                .header("Authorization");

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
    }
    public static String userChangesTheirName(CreateUserRequest user){
        String name = RandomModelGenerator.generate(ChangeNameRequest.class).getName();
        ChangeNameRequest changeName = ChangeNameRequest.builder()
                .name(name)
                .build();
        //change a name
        new CrudRequester(
                RequestSpecs.authAsUserSpec(user.getUsername(), user.getPassword()),
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOk())
                .update(changeName);

        return name;
    }

    public static String getNewName(CreateUserRequest user){
        return new ValidatedCrudRequester<CreateUserResponse>(
                RequestSpecs.authAsUserSpec(user.getUsername(),user.getPassword()),
                Endpoint.CUSTOMER_PROFILE_GET,
                ResponseSpecs.requestReturnsOk())
                .getAll().getName();
    }
}
