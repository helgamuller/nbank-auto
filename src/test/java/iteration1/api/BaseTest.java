package iteration1.api;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import requests.steps.AdminSteps;



public class BaseTest {
    protected SoftAssertions softly;
    protected int userId;
    @BeforeEach
    public void setupTest(){
        this.softly = new SoftAssertions();
    }

    @AfterEach
    public void cleanData(){
        if(userId!=0) {
            AdminSteps.deleteUser(userId);
        }
    }
    @AfterEach
    public void afterTest(){
        softly.assertAll();
    }
}
