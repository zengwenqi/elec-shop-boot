package elec.shop.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期工具类
 */
public class DateUtil {

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String TIME_PATTERN = "HH:mm:ss";
    private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String DATETIME_SIMPLE_PATTERN = "yyyyMMddHHmmss";
    private static final String DATE_SIMPLE_PATTERN = "yyyyMMdd";
    
    private static final SimpleDateFormat DEFAULT_FORMAT = new SimpleDateFormat(DEFAULT_PATTERN);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_PATTERN);
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

    /**
     * 格式化日期时间为字符串
     * @param date 日期对象
     * @return 格式化后的字符串
     */
    public static String formatDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return DEFAULT_FORMAT.format(date);
    }

    /**
     * 解析日期时间字符串为Date对象
     * @param dateStr 日期字符串
     * @return Date对象
     */
    public static Date parseDateTime(String dateStr) {
        if (StringUtils.isBlank(dateStr)) {
            return null;
        }
        try {
            return DEFAULT_FORMAT.parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解析日期字符串为Date对象（仅日期部分）
     * @param dateStr 日期字符串
     * @return Date对象
     */
    public static Date parseDate(String dateStr) {
        if (StringUtils.isBlank(dateStr)) {
            return null;
        }
        try {
            return DateUtils.parseDate(dateStr, DATE_PATTERN, DATETIME_PATTERN, DATE_SIMPLE_PATTERN);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 格式化日期为指定格式
     * @param date 日期对象
     * @param pattern 格式模式
     * @return 格式化后的字符串
     */
    public static String format(Date date, String pattern) {
        if (date == null) {
            return null;
        }
        return DateFormatUtils.format(date, pattern);
    }

    /**
     * 解析日期字符串为Date对象
     * @param dateStr 日期字符串
     * @param pattern 格式模式
     * @return Date对象
     */
    public static Date parse(String dateStr, String pattern) {
        if (StringUtils.isBlank(dateStr)) {
            return null;
        }
        try {
            return DateUtils.parseDate(dateStr, pattern);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 获取当前日期时间
     * @return 当前日期时间
     */
    public static Date now() {
        return new Date();
    }

    /**
     * 获取当前日期时间字符串
     * @return 格式化的当前日期时间
     */
    public static String nowString() {
        return formatDateTime(now());
    }

    /**
     * 获取当前日期字符串（仅日期部分）
     * @return 格式化的当前日期
     */
    public static String todayString() {
        return format(now(), DATE_PATTERN);
    }

    /**
     * 获取今天的开始时间
     * @return 今天零点时间
     */
    public static Date todayStart() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 获取今天的结束时间
     * @return 今天23:59:59时间
     */
    public static Date todayEnd() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    /**
     * 获取指定日期的开始时间
     * @param date 日期对象
     * @return 指定日期的开始时间
     */
    public static Date getDayStart(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 获取指定日期的结束时间
     * @param date 日期对象
     * @return 指定日期的结束时间
     */
    public static Date getDayEnd(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    /**
     * 获取指定天数之前的日期
     * @param days 天数
     * @return 之前的日期
     */
    public static Date beforeDays(int days) {
        return DateUtils.addDays(now(), -days);
    }

    /**
     * 获取指定天数之后的日期
     * @param days 天数
     * @return 之后的日期
     */
    public static Date afterDays(int days) {
        return DateUtils.addDays(now(), days);
    }

    /**
     * 获取指定月数之前的日期
     * @param months 月数
     * @return 之前的日期
     */
    public static Date beforeMonths(int months) {
        return DateUtils.addMonths(now(), -months);
    }

    /**
     * 获取指定月数之后的日期
     * @param months 月数
     * @return 之后的日期
     */
    public static Date afterMonths(int months) {
        return DateUtils.addMonths(now(), months);
    }

    /**
     * 判断是否是同一天
     * @param date1 日期1
     * @param date2 日期2
     * @return 是否同一天
     */
    public static boolean isSameDay(Date date1, Date date2) {
        return DateUtils.isSameDay(date1, date2);
    }

    /**
     * 计算两个日期之间相差的天数
     * @param date1 日期1
     * @param date2 日期2
     * @return 相差天数
     */
    public static int daysBetween(Date date1, Date date2) {
        return (int) ((date2.getTime() - date1.getTime()) / (1000 * 3600 * 24));
    }

    /**
     * 计算两个日期之间相差的小时数
     * @param date1 日期1
     * @param date2 日期2
     * @return 相差小时数
     */
    public static int hoursBetween(Date date1, Date date2) {
        return (int) ((date2.getTime() - date1.getTime()) / (1000 * 3600));
    }

    /**
     * 计算两个日期之间相差的分钟数
     * @param date1 日期1
     * @param date2 日期2
     * @return 相差分钟数
     */
    public static int minutesBetween(Date date1, Date date2) {
        return (int) ((date2.getTime() - date1.getTime()) / (1000 * 60));
    }

    /**
     * Date转LocalDateTime
     * @param date 日期对象
     * @return LocalDateTime对象
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     * LocalDateTime转Date
     * @param localDateTime LocalDateTime对象
     * @return Date对象
     */
    public static Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Date转LocalDate
     * @param date 日期对象
     * @return LocalDate对象
     */
    public static LocalDate toLocalDate(Date date) {
        return toLocalDateTime(date).toLocalDate();
    }

    /**
     * LocalDate转Date
     * @param localDate LocalDate对象
     * @return Date对象
     */
    public static Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 获取日期是星期几
     * @param date 日期对象
     * @return 星期几
     */
    public static int getDayOfWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    /**
     * 获取日期是一年中的第几天
     * @param date 日期对象
     * @return 一年中的第几天
     */
    public static int getDayOfYear(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * 获取一天的开始时间的时间戳
     * @param date 日期对象
     * @return 一天的开始时间的时间戳
     */
    public static long getDayStartTimestamp(Date date) {
        return getDayStart(date).getTime();
    }

    /**
     * 获取一天的结束时间的时间戳
     * @param date 日期对象
     * @return 一天的结束时间的时间戳
     */
    public static long getDayEndTimestamp(Date date) {
        return getDayEnd(date).getTime();
    }

    /**
     * 判断日期是否在指定范围内
     * @param date 日期对象
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 是否在指定范围内
     */
    public static boolean isInRange(Date date, Date startDate, Date endDate) {
        return date.compareTo(startDate) >= 0 && date.compareTo(endDate) <= 0;
    }

    /**
     * 获取日期范围内的天数
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 日期范围内的天数
     */
    public static int getDaysInRange(Date startDate, Date endDate) {
        return (int) ((endDate.getTime() - startDate.getTime()) / (1000 * 3600 * 24)) + 1;
    }

    /**
     * 获取两个日期中的较小值
     * @param date1 日期1
     * @param date2 日期2
     * @return 较小值
     */
    public static Date min(Date date1, Date date2) {
        return date1.before(date2) ? date1 : date2;
    }

    /**
     * 获取两个日期中的较大值
     * @param date1 日期1
     * @param date2 日期2
     * @return 较大值
     */
    public static Date max(Date date1, Date date2) {
        return date1.after(date2) ? date1 : date2;
    }
}
