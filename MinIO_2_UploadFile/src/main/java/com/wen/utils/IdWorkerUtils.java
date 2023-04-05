package com.wen.utils;

import java.security.SecureRandom;

/**
 * @author wen
 * @version 1.0
 * @description TODO 基于Snowflake雪花算法的ID生成器
 * @date 2023/4/4 1:22
 */
public final class IdWorkerUtils {
    public static void main(String[] args) {
        System.out.println(IdWorkerUtils.getInstance().nextId());
    }
    // ============================== Fields ===================================
    private static final SecureRandom RANDOM = new SecureRandom();

    // 定义各组成部分占用位数
    private static final long WORKER_ID_BITS = 5L; // workerID所占用的位数
    private static final long DATACENTER_ID_BITS = 5L; // 数据中心ID所占用的位数
    private static final long SEQUENCE_BITS = 12L; // 序列号所占用的位数

    // 定义各组成部分的最大值
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    // 定义组成部分左移的位数
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;

    // 定义起始时间戳（这里取值为2014-09-01 00:00:00.000）
    private static final long EPOCH = 1409529600000L;

    // 单例模式对象
    private static final IdWorkerUtils INSTANCE = new IdWorkerUtils();

    // ============================== Fields ===================================

    private long workerId;
    private long datacenterId;
    private long lastTimestamp = -1L;
    private long sequence = 0L;

    // ============================== Constructors ===================================
    /**
     * 私有构造器
     */
    private IdWorkerUtils() {
        this(RANDOM.nextInt((int) MAX_WORKER_ID), RANDOM.nextInt((int) MAX_DATACENTER_ID));
    }

    /**
     * 构造函数
     *
     * @param workerId      给定的worker ID
     * @param datacenterId  给定的datacenter ID
     */
    private IdWorkerUtils(final long workerId, final long datacenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", MAX_WORKER_ID));
        }
        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", MAX_DATACENTER_ID));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    // ============================== Methods ===================================
    /**
     * 获取单例对象
     *
     * @return 单例对象
     */
    public static IdWorkerUtils getInstance() {
        return INSTANCE;
    }

    /**
     * 生成唯一ID
     *
     * @return 唯一ID
     */
    public synchronized long nextId() {
        long timestamp = timeGen();
        if (timestamp < lastTimestamp) { // 如果当前时间小于上一次生成ID的时间，说明系统时钟回退过，抛出异常
            throw new RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }
        if (lastTimestamp == timestamp) { // 如果当前时间等于上一次生成ID的时间，那么对序列号进行+1操作（取模运算）
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) { // 如果序列号已经达到最大值，那么一直循环，直到获取到比上一个时间戳大的时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else { // 如果当前时间大于上一次生成ID的时间，那么重新从0开始生成序列号
            sequence = 0L;
        }

        lastTimestamp = timestamp; // 更新上一次生成ID的时间
        return ((timestamp - EPOCH) << TIMESTAMP_LEFT_SHIFT) | (datacenterId << DATACENTER_ID_SHIFT) | (workerId << WORKER_ID_SHIFT) | sequence;
    }

    /**
     * 生成长整型类型的唯一ID
     *
     * @return 长整型类型的唯一ID
     */
    public long nextLongId() {
        return nextId();
    }

    /**
     * 生成字符串类型的唯一ID，格式为：{workerId}-{datacenterId}-{sequence}
     *
     * @return 字符串类型的唯一ID
     */
    public String nextStringId() {
        return String.format("%d-%d-%d", workerId, datacenterId, sequence);
    }

    /**
     * 获取下一个时间戳，如果当前时间戳小于等于上一个时间戳，则一直循环等待，直到获取到比上一个时间戳大的时间戳
     *
     * @param lastTimestamp 上一个时间戳
     * @return 下一个时间戳
     */
    private long tilNextMillis(final long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 获取当前时间戳
     *
     * @return 当前时间戳
     */
    private long timeGen() {
        return System.currentTimeMillis();
    }
}
