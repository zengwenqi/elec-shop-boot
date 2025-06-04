package elec.shop.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期工具类
 */
public class DateUtil {

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final SimpleDateFormat DEFAULT_FORMAT = new SimpleDateFormat(DEFAULT_PATTERN);

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
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            return DEFAULT_FORMAT.parse(dateStr);
        } catch (Exception e) {
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
        try {
            SimpleDateFormat format = new SimpleDateFormat(pattern);
            return format.format(date);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解析日期字符串为Date对象
     * @param dateStr 日期字符串
     * @param pattern 格式模式
     * @return Date对象
     */
    public static Date parse(String dateStr, String pattern) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat format = new SimpleDateFormat(pattern);
            return format.parse(dateStr);
        } catch (Exception e) {
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
     * 获取指定天数之前的日期
     * @param days 天数
     * @return 之前的日期
     */
    public static Date beforeDays(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -days);
        return calendar.getTime();
    }

    /**
     * 获取指定天数之后的日期
     * @param days 天数
     * @return 之后的日期
     */
    public static Date afterDays(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTime();
    }

    /**
     * 判断是否是同一天
     * @param date1 日期1
     * @param date2 日期2
     * @return 是否同一天
     */
    public static boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * 计算两个日期之间相差的天数
     * @param date1 日期1
     * @param date2 日期2
     * @return 相差天数
     */
    public static int daysBetween(Date date1, Date date2) {
        long diff = Math.abs(date1.getTime() - date2.getTime());
        return (int) (diff / (24 * 60 * 60 * 1000));
    }
}
