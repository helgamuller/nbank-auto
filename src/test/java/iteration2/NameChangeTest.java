package iteration2;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

public class NameChangeTest {
    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()));
    }

    @Test
    public void userCanChangeNameToValidOneTest() {
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
                           "password": "olga1A$123",
         
                        }
                        """, username))
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");
        //login by a user


        //change a name
            given()
                    .contentType(ContentType.JSON)
                    .accept(ContentType.JSON)
                    .header("Authorization", userAuthHeader)
                    .body("""
                        {
                           "name": "olga M"
                           
                        }
                        """)
                    .put("http://localhost:4111/api/v1/customer/profile")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatus.SC_OK)
                    .body("customer.name", Matchers.equalTo("olga M"));
    }
    public static Stream<Arguments> invalidNameData(){
        return Stream.of(
                Arguments.of(""),
                Arguments.of("   "),
                Arguments.of("olga"),
                Arguments.of("olga olga olga"),
                Arguments.of("!12$%^& *()")
               // Arguments.of(null)


        );

    }

    @ParameterizedTest
    @MethodSource("invalidNameData")
    public void userCanNotChangeNameToInvalidOneTest(String name) {
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
                           "password": "olga1A$123",
                           "role": "USER"
                           }
                        """, username))
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");
        //login by a user


        //change a name
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthHeader)
                .body(String.format("""
            {
               "name": "%s"
            }
            """, name))
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("customer.name", Matchers.not(Matchers.equalTo(name)));
    }
    @Test
    public void unauthorizedUserCanNotChangeNameTest() {
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

        //change a name
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                           "name": "olga M"
                           
                        }
                        """)
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);

    }
}


