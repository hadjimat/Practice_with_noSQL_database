import org.redisson.Redisson;
import org.redisson.api.RKeys;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisConnectionException;
import org.redisson.config.Config;


import static java.lang.System.out;

public class RedisDemo {

    private RedissonClient redisson;


    private RKeys rKeys;

    private RScoredSortedSet<String> onlineUsers;

    private final static String KEY = "ONLINE_USERS";

    public void listKeys() {
        Iterable<String> keys = rKeys.getKeys();
        for(String key: keys) {
            out.println("KEY: " + key + ", type:" + rKeys.getType(key));
        }
    }

    void init() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        try {
            redisson = Redisson.create(config);
        } catch (RedisConnectionException Exc) {
            out.println("Не удалось подключиться к Redis");
            out.println(Exc.getMessage());
        }
        rKeys = redisson.getKeys();
        onlineUsers = redisson.getScoredSortedSet(KEY);
        rKeys.delete(KEY);
    }

    void shutdown() {
        redisson.shutdown();
    }

    void logPageVisit(int number, int user_id)
    {
        onlineUsers.add(number, String.valueOf(user_id));
    }

    public String getUserByNumber(int number) {
        return onlineUsers.valueRange(number, true, number + 1, false).stream().findAny().get();
    }

    public int getUserNumber(String userId) {
        return (int)Math.round(onlineUsers.getScore(userId));
    }

    public void setUserNumber(int number, String userId) {
        onlineUsers.addAndGetRank(number, userId);
    }
}
