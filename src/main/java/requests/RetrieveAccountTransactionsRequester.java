package requests;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;
import models.Transaction;
import org.apache.http.HttpStatus;

import java.util.List;

import static io.restassured.RestAssured.given;

public class RetrieveAccountTransactionsRequester extends Request{
    public RetrieveAccountTransactionsRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(BaseModel model) {
        return null;
    }

    public List<Transaction> get(int accountId){
        return  given()
                .spec(requestSpecification)
                .pathParam("id", accountId)
                .get("/api/v1/accounts/{id}/transactions")
                .then()
                .assertThat()
                .spec(responseSpecification)
                .extract()
                .jsonPath()
                .getList("", Transaction.class);
    }


    }

