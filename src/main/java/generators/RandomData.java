package generators;

import org.apache.commons.lang3.RandomStringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

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
    public static float randomTransfer (float min, float max){
        float rnd = ThreadLocalRandom.current().nextFloat(min, max);
        return Math.round(rnd*100f)/100f;
    }
}
