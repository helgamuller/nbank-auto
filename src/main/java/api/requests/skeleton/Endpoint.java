package api.requests.skeleton;

import api.models.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Endpoint {
    ADMIN_USER(
            "/admin/users",
            CreateUserRequest.class,
            CreateUserResponse.class
            ),
    LOGIN(
            "/auth/login",
            LoginUserRequest.class,
            LoginUserResponse.class

    ),
    ACCOUNTS(
            "/accounts",
            BaseModel.class,
            CreateAccountResponse.class
    ),
    @SuppressWarnings("unchecked")
    CUSTOMER_ACCOUNTS(
            "/customer/accounts",
            BaseModel.class,
            (Class<? extends BaseModel>) (Class<?>) CreateAccountResponse[].class
    ),
    CUSTOMER_ACCOUNTS_ALTER(
            "/customer/accounts",
            BaseModel.class,
            CreateAccountResponse.class
    ),
    CUSTOMER_PROFILE(
          "customer/profile",
          ChangeNameRequest.class,
          ChangeNameResponse.class
    ),
    CUSTOMER_PROFILE_GET(
            "/customer/profile",
            BaseModel.class,
            CreateUserResponse.class),

    ACCOUNTS_DEPOSIT(
            "accounts/deposit",
            MakeDepositRequest.class,
            CreateAccountResponse.class

    ),
    @SuppressWarnings("unchecked")
    ACCOUNT_TRANSACTIONS(
            "accounts/{id}/transactions",
            BaseModel.class,
            (Class<? extends BaseModel>) (Class<?>) Transaction[].class

    ),
    ACCOUNT_TRANSFER(
            "accounts/transfer",
            MakeTransferRequest.class,
            MakeTransferResponse.class
    ),
    ADMIN_DELETE(
            "admin/users/{id}",
            BaseModel.class,
            BaseModel.class
    )
    ;

    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
}
