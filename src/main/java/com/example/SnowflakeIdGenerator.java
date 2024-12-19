package com.example;

public class SnowflakeIdGenerator {
    
    // 起始时间戳，用于避免时间戳较小，负数产生
    private final long twepoch = 1288834974657L;

    // 机器ID所占的位数
    private final long workerIdBits = 10L;

    // 序列号所占的位数
    private final long sequenceBits = 12L;

    // 机器ID的最大值 (1024个节点)
    private final long maxWorkerId = ~(-1L << workerIdBits);

    // 每毫秒产生的最大序列号
    private final long sequenceMask = ~(-1L << sequenceBits);

    // 工作机器ID (0~1023)
    private long workerId;

    // 毫秒内序列 (0~4095)
    private long sequence = 0L;

    // 上次生成ID的时间戳
    private long lastTimestamp = -1L;

    public SnowflakeIdGenerator(long workerId) {
        // 检查workerId是否合法
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException("worker Id can't be greater than " + maxWorkerId + " or less than 0");
        }
        this.workerId = workerId;
    }

    // 生成ID
    public synchronized long generateId() {
        long timestamp = timeGen();

        // 如果当前时间小于上次生成ID的时间戳，说明系统时钟回退，抛出异常
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id for " + (lastTimestamp - timestamp) + " milliseconds");
        }

        // 如果是同一毫秒内，则使用序列号进行递增
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                // 如果序列号达到最大值，则等待下一毫秒
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            // 不同毫秒内，重置序列号
            sequence = 0;
        }

        lastTimestamp = timestamp;

        // 生成最终ID
        return ((timestamp - twepoch) << (workerIdBits + sequenceBits)) | (workerId << sequenceBits) | sequence;
    }

    // 获取当前时间戳
    private long timeGen() {
        return System.currentTimeMillis();
    }

    // 等待到下一毫秒
    private long waitNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

}
