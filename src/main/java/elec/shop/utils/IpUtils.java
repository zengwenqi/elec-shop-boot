package elec.shop.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * IP地址工具类
 */
@Slf4j
public class IpUtils {

    private static final String UNKNOWN = "unknown";
    private static final String LOCAL_IP = "127.0.0.1";
    private static final String LOCAL_ADDR = "本地";
    private static final String IP_URL = "https://whois.pconline.com.cn/ipJson.jsp?ip=%s&json=true";

    /**
     * 获取客户端真实IP地址
     */
    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (isUnknown(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (isUnknown(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (isUnknown(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (isUnknown(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (isUnknown(ip)) {
            ip = request.getRemoteAddr();
        }
        // 对于通过多个代理的情况，第一个IP为客户端真实IP，多个IP按照','分割
        if (ip != null && ip.indexOf(",") > 0) {
            ip = ip.substring(0, ip.indexOf(","));
        }
        return ip;
    }

    /**
     * 获取IP地址所属地理位置
     */
    public static String getLocationByIP(String ip) {
        // 如果是本地IP，直接返回
        if (LOCAL_IP.equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            return LOCAL_ADDR;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = String.format(IP_URL, ip);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // 解析响应数据，格式类似：{"ip":"x.x.x.x","pro":"浙江省","proCode":"330000","city":"杭州市","cityCode":"330100","region":"滨江区","regionCode":"330108","addr":"浙江省杭州市滨江区 电信"}
                String body = response.getBody();
                // 简单处理，实际项目中建议使用JSON工具类解析
                String pro = body.substring(body.indexOf("\"pro\":\"") + 7);
                pro = pro.substring(0, pro.indexOf("\""));
                String city = body.substring(body.indexOf("\"city\":\"") + 8);
                city = city.substring(0, city.indexOf("\""));
                return pro + " " + city;
            }
        } catch (Exception e) {
            log.error("获取IP地址所属地理位置失败：", e);
        }
        return "未知位置";
    }

    /**
     * 检查IP地址是否未知
     */
    private static boolean isUnknown(String ip) {
        return ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip);
    }
}
