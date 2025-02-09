package pesco.history_service.utils;

import java.util.Random;
import xyz.downgoon.snowflake.Snowflake;
import org.springframework.stereotype.Component;

@Component
public class KeysWrapper {
    private static final Snowflake snowflake = new Snowflake(1, 1);
    
    public Long generateSessionId() {
        long timestamp = System.currentTimeMillis();
        Random random = new Random();
        int randomNumber = 1000 + random.nextInt(9000);
        String combinedId = timestamp + String.valueOf(randomNumber);
        return Long.parseLong(combinedId);
    }
    public Long createSnowflakeUniqueId() {
        return snowflake.nextId();
    }
}
