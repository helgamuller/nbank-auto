package api.generators;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;

public class RandomData {
//    private RandomData(){}
//
//    public static String getUserName(){
//        return RandomStringUtils.randomAlphabetic(10);
//    }
//    public static String getUserPassword(){
//        return RandomStringUtils.randomAlphabetic(3).toUpperCase() +
//            RandomStringUtils.randomAlphabetic(5).toLowerCase()+
//                RandomStringUtils.randomNumeric(3) +
//               "%$#";
//    }
//    public static String getNewUserName(){
//        return RandomStringUtils.randomAlphabetic(3).toUpperCase() + " " + RandomStringUtils.randomAlphabetic(4).toLowerCase();
//
//    }
//    public static float randomTransfer (BigDecimal min, BigDecimal max){
//        BigDecimal rnd = ThreadLocalRandom.current().next(min, max);
//        return Math.round(rnd*100f)/100f;
//    }

    public static BigDecimal randomTransfer(BigDecimal min, BigDecimal max) {
        // 1. переводим диапазон в «центы» (× 100) → целые числа
        BigInteger minCents = min.movePointRight(2).toBigIntegerExact();
        BigInteger maxCents = max.movePointRight(2).toBigIntegerExact();

        // 2. случайное long в [minCents, maxCents] (включительно)
        long rndCents = ThreadLocalRandom.current()
                .nextLong(minCents.longValueExact(), maxCents.longValueExact() + 1);

        // 3. возвращаем BigDecimal и делим на 100, scale = 2
        return BigDecimal.valueOf(rndCents)
                .movePointLeft(2)
                .setScale(2, RoundingMode.UNNECESSARY);
    }

}
