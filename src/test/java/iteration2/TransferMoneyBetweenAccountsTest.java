package iteration2;

import io.restassured.RestAssured;
import io.restassured.filter.log.ErrorLoggingFilter;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class TransferMoneyBetweenAccountsTest {
    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter(),
                        new ErrorLoggingFilter()));
    }

    public static Stream<Arguments> validTransferData() {
        return Stream.of(
                Arguments.of(10000.00f, 0.01f, "Transfer successful"),
                Arguments.of(10000.00f, 9999.99f, "Transfer successful"),
                Arguments.of(10000.00f, 10000.00f, "Transfer successful")


        );
    }

    @ParameterizedTest
    @MethodSource("validTransferData")
    public void userCanTransferValidAmountTest(Float depositAmount, Float transfer, String message) {
        //create user by admin
        String username = "aaa" + UUID.randomUUID().toString().replaceAll("[^a-zA-Z0-9]", "").substring(0, 8);

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(String.format("""
                        {
                        "username": "%s",
                           "password": "olga1A$123",
                           "role": "USER"
                        }
                        """, username))
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

//get Token/login
        String userAuthHeader = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format("""
                        {
                        "username": "%s",
                           "password": "olga1A$123"
                        
                        }
                        """, username))
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        //create source account
        Integer sourceAccount = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");

        //create receiving account
        Integer receivingAccount = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");

        //make a deposit
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format("""
                        {
                            "id": %d,
                            "balance": %.2f
                        }
                        """, sourceAccount, depositAmount))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        //make a transfer
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format("""
                        {
                        
                          "senderAccountId": %d,
                          "receiverAccountId": %d,
                          "amount": %.2f
                        
                        }
                        """, sourceAccount, receivingAccount, transfer))
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("message", equalTo(message))
                .body("amount", Matchers.comparesEqualTo(transfer))
                .body("senderAccountId", equalTo(sourceAccount))
                .body("receiverAccountId", equalTo(receivingAccount));

        //check transfer-out in senderAccount
        Response responseOut = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .get(String.format("http://localhost:4111/api/v1/accounts/%s/transactions", sourceAccount))
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();

        //parse response into list
        List<Map<String, Object>> transactions = responseOut.jsonPath().getList("$");

        boolean isTransferOutFound = transactions.stream()
                .anyMatch(transaction -> "TRANSFER_OUT".equals(transaction.get("type")) &&
                        ((Float.parseFloat(transaction.get("amount").toString()) == transfer)) && (receivingAccount.equals(transaction.get("relatedAccountId"))));


        Assertions.assertTrue(isTransferOutFound);
        //check transfer-in in receiverAccount
        Response responseIn = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .get(String.format("http://localhost:4111/api/v1/accounts/%s/transactions", receivingAccount))
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();

        //parse response into list
        List<Map<String, Object>> transactionsIn = responseIn.jsonPath().getList("$");

        boolean isTransferInFound = transactionsIn.stream()
                .anyMatch(transaction -> "TRANSFER_IN".equals(transaction.get("type")) &&
                        ((Float.parseFloat(transaction.get("amount").toString()) == transfer)) && (sourceAccount.equals(transaction.get("relatedAccountId"))));


        Assertions.assertTrue(isTransferInFound);

    }

    public static Stream<Arguments> invalidTransferData() {
        return Stream.of(
                Arguments.of(5000.00f, 5100.01f, "Invalid transfer: insufficient funds or invalid accounts"),
                Arguments.of(10000.00f, 10000.01f, "Invalid transfer: insufficient funds or invalid accounts"),
                Arguments.of(10000.00f, 0.00f, "Invalid transfer: insufficient funds or invalid accounts"),
                Arguments.of(10000.00f, -0.01f, "Invalid transfer: insufficient funds or invalid accounts")

        );
    }

    @ParameterizedTest
    @MethodSource("invalidTransferData")
    public void userCanNotTransferInvalidAmountTest(Float depositAmount, Float transfer, String message) {
        //create user by admin
        String username = "aaa" + UUID.randomUUID().toString().replaceAll("[^a-zA-Z0-9]", "").substring(0, 8);

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(String.format("""
                        {
                        "username": "%s",
                           "password": "olga1A$123",
                           "role": "USER"
                        }
                        """, username))
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

//get Token/login
        String userAuthHeader = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format("""
                        {
                        "username": "%s",
                           "password": "olga1A$123"
                        
                        }
                        """, username))
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        //create source account
        Integer sourceAccount = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");

        //create receiving account
        Integer receivingAccount = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");

        //make a deposit
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format("""
                        {
                            "id": %d,
                            "balance": %.2f
                        }
                        """, sourceAccount, depositAmount))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        //make a transfer
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format("""
                        {
                        
                          "senderAccountId": %d,
                          "receiverAccountId": %d,
                          "amount": %.2f
                        
                        }
                        """, sourceAccount, receivingAccount, transfer))
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(equalTo(message));

    }

    @Test
    public void unauthorizedUserCanNotMakeTransferTest() {
        //create user by admin
        String username = "aaa" + UUID.randomUUID().toString().replaceAll("[^a-zA-Z0-9]", "").substring(0, 8);

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(String.format("""
                        {
                        "username": "%s",
                           "password": "olga1A$123",
                           "role": "USER"
                        }
                        """, username))
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        //get Token
        String userAuthHeader = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format("""
                        {
                        "username": "%s",
                           "password": "olga1A$123"
                        
                        }
                        """, username))
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");
        //create accounts
        Integer sourceAccount = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");

        //create receiving account
        Integer receivingAccount = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");

        //make  a deposit
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format("""
                        {
                            "id": %d,
                            "balance": 100.0
                        }
                        """, sourceAccount))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        //System.out.println("Response Body: " + response.asString());
        //System.out.println("Response Status Code: " + response.getStatusCode());

        //make a transfer
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format("""
                        {
                        
                          "senderAccountId": %d,
                          "receiverAccountId": %d,
                          "amount": 100.0f
                        
                        }
                        """, sourceAccount, receivingAccount))
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);


    }

    @Test
    public void userCanNotMakeTransferFromNonexistingAccountTest() {
        //create user by admin
        String username = "aaa" + UUID.randomUUID().toString().replaceAll("[^a-zA-Z0-9]", "").substring(0, 8);

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(String.format("""
                        {
                        "username": "%s",
                           "password": "olga1A$123",
                           "role": "USER"
                        }
                        """, username))
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        //get Token
        String userAuthHeader = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format("""
                        {
                        "username": "%s",
                           "password": "olga1A$123"
                        
                        }
                        """, username))
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");
        //create accounts
//        Integer sourceAccount = given()
//                .header("Authorization", userAuthHeader)
//                .contentType(ContentType.JSON)
//                .accept(ContentType.JSON)
//                .post("http://localhost:4111/api/v1/accounts")
//                .then()
//                .assertThat()
//                .statusCode(HttpStatus.SC_CREATED)
//                .extract()
//                .path("id");

        //create receiving account only
        Integer receivingAccount = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");

        //make  a deposit
//        given()
//                .header("Authorization", userAuthHeader)
//                .contentType(ContentType.JSON)
//                .accept(ContentType.JSON)
//                .body(String.format("""
//                        {
//                            "id": %d,
//                            "balance": 100.0
//                        }
//                        """, sourceAccount))
//                .post("http://localhost:4111/api/v1/accounts/deposit")
//                .then()
//                .assertThat()
//                .statusCode(HttpStatus.SC_OK);
        //System.out.println("Response Body: " + response.asString());
        //System.out.println("Response Status Code: " + response.getStatusCode());

        //make a transfer
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format("""
                        {
                        
                          "senderAccountId": 0,
                          "receiverAccountId": %d,
                          "amount": 100.0
                        
                        }
                        """, receivingAccount))
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);

    }

    @Test
    public void userCanNotMakeTransferToNonexistingAccountTest() {
        //create user by admin
        String username = "aaa" + UUID.randomUUID().toString().replaceAll("[^a-zA-Z0-9]", "").substring(0, 8);

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(String.format("""
                        {
                        "username": "%s",
                           "password": "olga1A$123",
                           "role": "USER"
                        }
                        """, username))
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        //get Token
        String userAuthHeader = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format("""
                        {
                        "username": "%s",
                           "password": "olga1A$123"
                        
                        }
                        """, username))
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");
        //create accounts
        Integer sourceAccount = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");


        //make  a deposit
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format("""
                        {
                            "id": %d,
                            "balance": 100.0
                        }
                        """, sourceAccount))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        //System.out.println("Response Body: " + response.asString());
        //System.out.println("Response Status Code: " + response.getStatusCode());

        //make a transfer
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format("""
                        {
                        
                          "senderAccountId": %d,
                          "receiverAccountId": 0,
                          "amount": 100.0
                        
                        }
                        """, sourceAccount))
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);

    }
    @Test
    public void userCanNotMakeTransferToSendingAccountTest() {
        //create user by admin
        String username = "aaa" + UUID.randomUUID().toString().replaceAll("[^a-zA-Z0-9]", "").substring(0, 8);

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(String.format("""
                        {
                        "username": "%s",
                           "password": "olga1A$123",
                           "role": "USER"
                        }
                        """, username))
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        //get Token
        String userAuthHeader = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format("""
                        {
                        "username": "%s",
                           "password": "olga1A$123"
                        
                        }
                        """, username))
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");
        //create accounts
        Integer sourceAccount = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");


        //make  a deposit
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format("""
                        {
                            "id": %d,
                            "balance": 100.0
                        }
                        """, sourceAccount))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        //System.out.println("Response Body: " + response.asString());
        //System.out.println("Response Status Code: " + response.getStatusCode());

        //make a transfer
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthHeader)
                .body(String.format("""
                        {
                        
                          "senderAccountId": %d,
                          "receiverAccountId": %d,
                          "amount": 100.0
                        
                        }
                        """, sourceAccount, sourceAccount))
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

    }
}

