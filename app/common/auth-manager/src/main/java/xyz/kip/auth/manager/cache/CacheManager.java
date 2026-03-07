package xyz.kip.auth.manager.cache;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Cache manager interface for Redis operations.
 * Provides unified API for common Redis data structures and operations.
 *
 * @author xiaoshichuan
 * @version 2026-03-07 17:19
 */
public interface CacheManager {
    // -------- String / 普通 key --------

    /**
     * Get value by key and cast to specified type.
     *
     * @param key  cache key
     * @param type target type class
     * @param <T>  target type
     * @return value or null if not exists
     */
    <T> T get(String key, Class<T> type);

    /**
     * Set key-value with TTL.
     *
     * @param key        cache key
     * @param value      cache value
     * @param ttlSeconds time to live in seconds, 0 or negative means no expiration
     */
    void set(String key, Object value, long ttlSeconds);

    /**
     * Set key-value without expiration.
     *
     * @param key   cache key
     * @param value cache value
     */
    void set(String key, Object value);

    /**
     * Delete single key.
     *
     * @param key cache key to delete
     */
    void delete(String key);

    /**
     * Delete multiple keys.
     *
     * @param keys collection of keys to delete
     */
    void delete(Collection<String> keys);

    /**
     * Check if key exists.
     *
     * @param key cache key
     * @return true if exists, false otherwise
     */
    boolean exists(String key);

    /**
     * Set expiration time for key.
     *
     * @param key        cache key
     * @param ttlSeconds time to live in seconds
     * @return true if expiration was set, false otherwise
     */
    boolean expire(String key, long ttlSeconds);

    /**
     * Increment value by 1.
     *
     * @param key cache key
     * @return value after increment
     */
    Long increment(String key);

    /**
     * Increment value by delta.
     *
     * @param key   cache key
     * @param delta increment amount
     * @return value after increment
     */
    Long increment(String key, long delta);

    /**
     * Decrement value by 1.
     *
     * @param key cache key
     * @return value after decrement
     */
    Long decrement(String key);

    /**
     * Decrement value by delta.
     *
     * @param key   cache key
     * @param delta decrement amount
     * @return value after decrement
     */
    Long decrement(String key, long delta);

    // -------- Hash --------

    /**
     * Get hash field value.
     *
     * @param key     cache key
     * @param hashKey hash field name
     * @param type    target type class
     * @param <T>     target type
     * @return field value or null if not exists
     */
    <T> T hGet(String key, String hashKey, Class<T> type);

    /**
     * Set hash field value.
     *
     * @param key     cache key
     * @param hashKey hash field name
     * @param value   field value
     */
    void hSet(String key, String hashKey, Object value);

    /**
     * Delete hash fields.
     *
     * @param key      cache key
     * @param hashKeys field names to delete
     */
    void hDelete(String key, String... hashKeys);

    /**
     * Get all hash fields and values.
     *
     * @param key cache key
     * @return map of field names to values
     */
    Map<String, Object> hGetAll(String key);

    /**
     * Check if hash field exists.
     *
     * @param key     cache key
     * @param hashKey hash field name
     * @return true if field exists, false otherwise
     */
    boolean hExists(String key, String hashKey);

    /**
     * Get hash size (number of fields).
     *
     * @param key cache key
     * @return number of fields in hash
     */
    Long hSize(String key);

    // -------- List --------

    /**
     * Push value to list head (left).
     *
     * @param key   cache key
     * @param value value to push
     */
    void lPush(String key, Object value);

    /**
     * Push value to list tail (right).
     *
     * @param key   cache key
     * @param value value to push
     */
    void rPush(String key, Object value);

    /**
     * Pop value from list head (left).
     *
     * @param key cache key
     * @return popped value or null if list is empty
     */
    Object lPop(String key);

    /**
     * Pop value from list tail (right).
     *
     * @param key cache key
     * @return popped value or null if list is empty
     */
    Object rPop(String key);

    /**
     * Get list range.
     *
     * @param key   cache key
     * @param start start index (0-based, inclusive)
     * @param end   end index (inclusive, -1 means last element)
     * @param type  target type class
     * @param <T>   target type
     * @return list of values in range
     */
    <T> List<T> lRange(String key, long start, long end, Class<T> type);

    /**
     * Trim list to specified range.
     *
     * @param key   cache key
     * @param start start index (0-based, inclusive)
     * @param end   end index (inclusive)
     */
    void lTrim(String key, long start, long end);

    /**
     * Get list size.
     *
     * @param key cache key
     * @return number of elements in list
     */
    Long lSize(String key);

    // -------- Set --------

    /**
     * Add values to set.
     *
     * @param key    cache key
     * @param values values to add
     */
    void sAdd(String key, Object... values);

    /**
     * Get all set members.
     *
     * @param key  cache key
     * @param type target type class
     * @param <T>  target type
     * @return set of all members
     */
    <T> Set<T> sMembers(String key, Class<T> type);

    /**
     * Remove values from set.
     *
     * @param key    cache key
     * @param values values to remove
     */
    void sRemove(String key, Object... values);

    /**
     * Check if value is member of set.
     *
     * @param key   cache key
     * @param value value to check
     * @return true if value is member, false otherwise
     */
    boolean sIsMember(String key, Object value);

    /**
     * Get set size.
     *
     * @param key cache key
     * @return number of members in set
     */
    Long sSize(String key);

    // -------- Sorted Set --------

    /**
     * Add value to sorted set with score.
     *
     * @param key   cache key
     * @param value value to add
     * @param score sort score
     */
    void zAdd(String key, Object value, double score);

    /**
     * Get sorted set range by rank.
     *
     * @param key   cache key
     * @param start start rank (0-based, inclusive)
     * @param end   end rank (inclusive, -1 means last element)
     * @param type  target type class
     * @param <T>   target type
     * @return set of values in rank range
     */
    <T> Set<T> zRange(String key, long start, long end, Class<T> type);

    /**
     * Get sorted set range by score.
     *
     * @param key  cache key
     * @param min  minimum score (inclusive)
     * @param max  maximum score (inclusive)
     * @param type target type class
     * @param <T>  target type
     * @return set of values in score range
     */
    <T> Set<T> zRangeByScore(String key, double min, double max, Class<T> type);

    /**
     * Remove values from sorted set.
     *
     * @param key    cache key
     * @param values values to remove
     */
    void zRemove(String key, Object... values);

    /**
     * Get sorted set size.
     *
     * @param key cache key
     * @return number of members in sorted set
     */
    Long zSize(String key);

    /**
     * Get score of value in sorted set.
     *
     * @param key   cache key
     * @param value value to get score for
     * @return score or null if value not exists
     */
    Double zScore(String key, Object value);

    // -------- 批量操作 --------

    /**
     * Set multiple key-value pairs with TTL.
     *
     * @param entries    map of keys to values
     * @param ttlSeconds time to live in seconds, 0 or negative means no expiration
     */
    void multiSet(Map<String, Object> entries, long ttlSeconds);

    /**
     * Get multiple values by keys.
     *
     * @param keys collection of keys
     * @return list of values (null for non-existent keys)
     */
    List<Object> multiGet(Collection<String> keys);
}
