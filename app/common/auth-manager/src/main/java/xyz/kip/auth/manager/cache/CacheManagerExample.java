package xyz.kip.auth.manager.cache;

import xyz.kip.auth.manager.util.RedisKeyUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Example usage of CacheManager for Redis operations.
 *
 * @author xiaoshichuan
 */
public class CacheManagerExample {

    private final CacheManager cacheManager;

    public CacheManagerExample(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * String operations example.
     */
    public void stringOperations() {
        // Set with TTL
        cacheManager.set("user:1001", "John Doe", 3600);

        // Set without TTL
        cacheManager.set("config:app", "production");

        // Get
        String userName = cacheManager.get("user:1001", String.class);

        // Check exists
        boolean exists = cacheManager.exists("user:1001");

        // Delete
        cacheManager.delete("user:1001");

        // Increment/Decrement
        Long count = cacheManager.increment("counter:visits");
        cacheManager.decrement("counter:stock", 5);
    }

    /**
     * Hash operations example.
     */
    public void hashOperations() {
        String key = RedisKeyUtil.userInfoKey("1001");

        // Set hash field
        cacheManager.hSet(key, "name", "John");
        cacheManager.hSet(key, "age", 30);

        // Get hash field
        String name = cacheManager.hGet(key, "name", String.class);

        // Get all fields
        Map<String, Object> userInfo = cacheManager.hGetAll(key);

        // Check field exists
        boolean hasEmail = cacheManager.hExists(key, "email");

        // Delete field
        cacheManager.hDelete(key, "age");
    }

    /**
     * List operations example.
     */
    public void listOperations() {
        String key = "queue:tasks";

        // Push to list
        cacheManager.lPush(key, "task1");
        cacheManager.rPush(key, "task2");

        // Pop from list
        Object task = cacheManager.lPop(key);

        // Get range
        List<String> tasks = cacheManager.lRange(key, 0, 10, String.class);

        // Trim list
        cacheManager.lTrim(key, 0, 99);
    }

    /**
     * Set operations example.
     */
    public void setOperations() {
        String key = "tags:article:1001";

        // Add to set
        cacheManager.sAdd(key, "java", "redis", "spring");

        // Get all members
        Set<String> tags = cacheManager.sMembers(key, String.class);

        // Check membership
        boolean hasTag = cacheManager.sIsMember(key, "java");

        // Remove from set
        cacheManager.sRemove(key, "redis");
    }

    /**
     * Sorted Set operations example.
     */
    public void sortedSetOperations() {
        String key = "leaderboard:game";

        // Add with score
        cacheManager.zAdd(key, "player1", 100.0);
        cacheManager.zAdd(key, "player2", 200.0);

        // Get range
        Set<String> topPlayers = cacheManager.zRange(key, 0, 9, String.class);

        // Get by score range
        Set<String> highScorers = cacheManager.zRangeByScore(key, 150.0, 300.0, String.class);

        // Get score
        Double score = cacheManager.zScore(key, "player1");
    }

    /**
     * Batch operations example.
     */
    public void batchOperations() {
        // Multi set
        Map<String, Object> entries = new HashMap<>();
        entries.put("key1", "value1");
        entries.put("key2", "value2");
        cacheManager.multiSet(entries, 3600);

        // Multi get
        List<String> keys = Arrays.asList("key1", "key2", "key3");
        List<Object> values = cacheManager.multiGet(keys);
    }

    /**
     * Using RedisKeyUtil for consistent key naming.
     */
    public void keyUtilExample() {
        // Generate keys
        String tokenKey = RedisKeyUtil.userTokenKey("1001");
        String bizKey = RedisKeyUtil.bizInfoKey("BIZ001");
        String sessionKey = RedisKeyUtil.sessionKey("sess_abc123");
        String codeKey = RedisKeyUtil.verifyCodeKey("13800138000");
        String rateLimitKey = RedisKeyUtil.rateLimitKey("api:/login");
        String lockKey = RedisKeyUtil.lockKey("order:1001");

        // Use with cache manager
        cacheManager.set(tokenKey, "jwt_token_here", 7200);
        cacheManager.set(codeKey, "123456", 300);
    }
}
