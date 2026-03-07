package xyz.kip.auth.infrastructure.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author xiaoshichuan
 * @version 2022-11-30 12:17
 */
@Component
public class SnowFlakeUtil {

    /**
     * 起始时间戳，从2021-12-01开始生成
     */
    private final static long START_STAMP = 1638288000000L;

    /**
     * 序列号占用的位数 12
     */
    private final static long SEQUENCE_BIT = 12;

    /**
     * 机器标识占用的位数
     */
    private final static long MACHINE_BIT = 10;

    /**
     * 机器数量最大值
     */
    private final static long MAX_MACHINE_NUM = ~(-1L << MACHINE_BIT);

    /**
     * 序列号最大值
     */
    private final static long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);

    /**
     * 每一部分向左的位移
     */
    private final static long MACHINE_LEFT = SEQUENCE_BIT;
    private final static long TIMESTAMP_LEFT = SEQUENCE_BIT + MACHINE_BIT;

    /**
     * 机器标识
     */
    private static long machineId;

    /**
     * 号段前缀（字符串），用于与雪花ID拼接组成业务ID
     * 例如：业务线/租户/环境号段等。
     */
    private static String idSegmentPrefix = "";

    /**
     * 通过构造器注入配置，避免静态字段无法注入的问题
     */
    public SnowFlakeUtil(
            @Value("${config.machineId:1}") long machineIdProp,
            @Value("${config.idSegment:}") String segmentProp
    ) {
        machineId = machineIdProp & MAX_MACHINE_NUM;
        idSegmentPrefix = segmentProp == null ? "" : segmentProp.trim();
    }

    /**
     * 序列号
     */
    private static long sequence = 0L;

    /**
     * 上一次时间戳
     */
    private static long lastStamp = -1L;

    /**
     * 产生下一个ID
     */
    public static synchronized long nextId() {
        long currStamp = getNewStamp();
        if (currStamp < lastStamp) {
            throw new RuntimeException("时钟后移，拒绝生成ID！");
        }
        if (currStamp == lastStamp) {
            // 相同毫秒内，序列号自增
            sequence = (sequence + 1) & MAX_SEQUENCE;
            // 同一毫秒的序列数已经达到最大
            if (sequence == 0L) {
                currStamp = getNextMill();
            }
        } else {
            // 不同毫秒内，序列号置为0
            sequence = 0L;
        }
        lastStamp = currStamp;
        // 时间戳部分 | 机器标识部分 | 序列号部分
        return (currStamp - START_STAMP) << TIMESTAMP_LEFT
                | machineId << MACHINE_LEFT
                | sequence;
    }

    /**
     * 生成 号段 + 雪花 的字符串ID。
     * 号段来自配置项 config.idSegment，可为空；当为空时仅返回雪花ID字符串。
     *
     * 示例：idSegment=1001，返回 "1001" + snowflakeId。
     */
    public static String nextSegmentId() {
        long snowflake = nextId();
        if (idSegmentPrefix == null || idSegmentPrefix.isEmpty()) {
            return String.valueOf(snowflake);
        }
        return idSegmentPrefix + snowflake;
    }

    private static long getNextMill() {
        long mill = getNewStamp();
        while (mill <= lastStamp) {
            mill = getNewStamp();
        }
        return mill;
    }

    private static long getNewStamp() {
        return System.currentTimeMillis();
    }
}
