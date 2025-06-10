package iteration1;

import models.CreateUserRequest;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import requests.steps.AdminSteps;


public class BaseTest {
    protected SoftAssertions softly;
    @BeforeEach
    public void setupTest(){
        this.softly = new SoftAssertions();
    }

    @AfterEach
    public void afterTest(){
        softly.assertAll();
    }

}
