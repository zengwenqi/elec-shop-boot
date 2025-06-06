package elec.shop.utils;

public class SnowflakeLogIdGenerator {
    // 时间起始标记点，作为基准时间（可自定义，例如项目启动时间）
    private static final long START_TIMESTAMP = 1677721600000L; // 2023-01-01 00:00:00（毫秒）

    // 各部分占用的位数
    private static final int DATA_CENTER_ID_BITS = 5;  // 数据中心ID位数
    private static final int WORKER_ID_BITS = 5;       // 节点ID位数
    private static final int SEQUENCE_BITS = 12;       // 序列号位数

    // 各部分的最大值
    private static final long MAX_DATA_CENTER_ID = -1L ^ (-1L << DATA_CENTER_ID_BITS);
    private static final long MAX_WORKER_ID = -1L ^ (-1L << WORKER_ID_BITS);
    private static final long MAX_SEQUENCE = -1L ^ (-1L << SEQUENCE_BITS);

    // 移位偏移量
    private static final int WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final int DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final int TIMESTAMP_SHIFT = DATA_CENTER_ID_SHIFT + DATA_CENTER_ID_BITS;

    // 成员变量
    private final long dataCenterId; // 数据中心ID（0~31）
    private final long workerId;     // 节点ID（0~31）
    private long sequence = 0L;     // 序列号（0~4095）
    private long lastTimestamp = -1L; // 上一次生成ID的时间戳

    /**
     * 构造方法，初始化数据中心ID和节点ID
     * @param dataCenterId 数据中心ID（0~31）
     * @param workerId     节点ID（0~31）
     */
    public SnowflakeLogIdGenerator(long dataCenterId, long workerId) {
        if (dataCenterId > MAX_DATA_CENTER_ID || dataCenterId < 0) {
            throw new IllegalArgumentException("Data center ID must be between 0 and " + MAX_DATA_CENTER_ID);
        }
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException("Worker ID must be between 0 and " + MAX_WORKER_ID);
        }
        this.dataCenterId = dataCenterId;
        this.workerId = workerId;
    }

    /**
     * 生成唯一ID
     * @return 64位长整型ID
     */
    public synchronized long generateId() {
        long timestamp = System.currentTimeMillis();

        // 时钟回退处理（如果发生，抛出异常或等待）
        if (timestamp < lastTimestamp) {
            throw new IllegalStateException(
                "Clock moved backwards. Refusing to generate id for " + (lastTimestamp - timestamp) + " milliseconds"
            );
        }

        // 同一时间戳内，序列号自增
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp); // 等待下一个毫秒
            }
        } else {
            sequence = 0; // 新时间戳，序列号重置
        }

        lastTimestamp = timestamp;

        // 计算ID：时间戳部分 + 数据中心ID部分 + 节点ID部分 + 序列号部分
        return (timestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT |
               dataCenterId << DATA_CENTER_ID_SHIFT |
               workerId << WORKER_ID_SHIFT |
               sequence;
    }

    /**
     * 等待直到获取新的时间戳
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    // 示例：生成日志ID
    public static void main(String[] args) {
        // 初始化生成器（数据中心ID=0，节点ID=0）
        SnowflakeLogIdGenerator generator = new SnowflakeLogIdGenerator(0, 0);

        // 生成ID
        long logId = generator.generateId();
        System.out.println("Log ID: " + logId); // 输出类似：1623456789012345678
    }
}
