package generators;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Random;

public class RandomData {
    private RandomData(){}

    public static String getUserName(){
        return RandomStringUtils.randomAlphabetic(10);
    }
    public static String getUserPassword(){
        return RandomStringUtils.randomAlphabetic(3).toUpperCase() +
            RandomStringUtils.randomAlphabetic(5).toLowerCase()+
                RandomStringUtils.randomNumeric(3) +
               "%$#";
    }
    public static String getNewUserName(){
        return RandomStringUtils.randomAlphabetic(3).toUpperCase() + " " + RandomStringUtils.randomAlphabetic(4).toLowerCase();
    }
}
