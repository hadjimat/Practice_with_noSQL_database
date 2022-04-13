import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import static java.lang.System.out;

public class RedisTest {

    // Запуск докер-контейнера:
    // docker run --rm --name skill-redis -p 127.0.0.1:6379:6379/tcp -d redis

    private static final int USERS = 20;


    private static final SimpleDateFormat DF = new SimpleDateFormat("HH:mm:ss");

    private static void log(int UsersOnline) {
        String log = String.format("[%s] Пользователей онлайн: %d", DF.format(new Date()), UsersOnline);
        out.println(log);
    }

    public static void main(String[] args) {

        RedisDemo redis = new RedisDemo();
        redis.init();
        for (int request = 1; request <= USERS; request++) {
            redis.logPageVisit(request, request);
        }

        for (int i = 0; i < USERS; i++) {
            for (int number = 1; number <= USERS; number++) {
                printUser(redis,number);
                int random = new Random().nextInt(11);
                if (random == 10){
                    int random1 = new Random().nextInt(20) + 1;
                    String userId = String.valueOf(random1);
                    int uNumber = redis.getUserNumber(userId);
                    out.println(">> User " + userId + "\t payed the queue!");
                    if (uNumber > number) {
                        for (int shift = uNumber; shift > number; shift--) {
                            String shiftUser = redis.getUserByNumber(shift - 1);
                            redis.setUserNumber(shift, shiftUser);
                        }
                        redis.setUserNumber(number, userId);
                        printUser(redis, number);
                    }
                    if (uNumber < number) {
                        for (int shift = number; shift > uNumber; shift--) {
                            String shiftUser = redis.getUserByNumber(shift);
                            redis.setUserNumber(shift - 1, shiftUser);
                        }
                        redis.setUserNumber(number, userId);
                        printUser(redis, number);
                    }
                    if (uNumber == number) {
                        printUser(redis, number);
                    }
                }
            }
        }

        redis.shutdown();
    }

    private static void printUser(@NotNull RedisDemo redis, int number) {
        String user = redis.getUserByNumber(number);
        out.println("User " + user);
        pause();
    }

    private static void pause() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}