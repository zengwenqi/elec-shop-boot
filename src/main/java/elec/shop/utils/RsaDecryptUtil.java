package elec.shop.utils;

import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

@Component
public class RsaDecryptUtil {

    // 填入你数据库里 jwt.access-token-private-key 对应的私钥内容
    private static final String PRIVATE_KEY = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCSuDyfLPkt6tud6Lt3QQRj4M08o/gsMqeTjpWgCoINluVElz6B6kbMXdI+IrdnxuVydkjYqo9TrQNRRAy+nVrQUuPhb5tLcNnTRZECkVhjjBw/0W38KOaBpSTrWgVeS4RYS9dwr1ou232i+EH54Zji+QeRVQX3aUq3ccYIS2mTirMnsXmNJThTWu8zyzuOrZOzs4nveqhbhgdsdLxTk8+8+gIwGP+AhNXlc5fBYhLQ112X05IfX8gDaBBalRFTvYa15GoFCIMK/3l6YYnyBTu6H5hwkaktnl6QAfU3ASUMkhMshPRd9m8Jf6XurHUB77kzlNb+PzFMNltUshWbbaVTAgMBAAECggEAA+BxsZ8Fl9dXJY1VuWlIDWo44hD5vhPkkPs5LDkQMKhmYcaWnzt/0ryT5pJnmDVi21W8HQV8s1r59cwU2Msy8Bg7RI9SDM9BjNnkuABRKT2K2DYsxXdAh/fDdE3foHCMYaziAhc6tyGTk0UjW8/rM0FzQr16t+EvWTjctlwPwRc53fdkCrWvP1YkApmrVRjv/J+nHCMwKqx02QLVel069Qv9slIqORStswgOYfGh63jiKPrV7765bkVntDRRjH6yu1i84bi8TZtUw9lD3aWHNrgNtnv1R+dDawrXLZDlln7Ci0GbnOzmjv5lFrRmWZd1LweEvGg4faAfARHvYWvreQKBgQDK5YoQhknJvR8LcjypEkqq6dhdL+UbgiBufYY1p4sHLP4foDzzXpp9LcgySS098XKEazbcDnAkuIoHESH1/S6Hz4R2UeYH3gmccM6eXAaaoGF4j9T7gVORbNTAxL4ZVefoQGyZrJchVliSPS9P4YPmb4TJ7SD+MA7heNdSMncJJwKBgQC5Hrvu8y+0NrmMB62HYi4Fe+bxQFprtJeve55gc8CkWe72eTZLOLJM7v7pMKb62cGLrhEFHvsxNtXcG3hEqSlZwx7rmXfTxsMmiKhmAYK3JACWEOGSvQP9iFsM3XrniOIXs4z3OFGIWz9PQYGFz8gBtIjqb3FY1JxFTM8dyF7l9QKBgCj/xy1dpW1xnONYOy/C9Xmh9UmnUvmDCwCotsUCzRIvIk7Lt2VKOZeP98NJjC3XK1fcvmIrPLf+3k1dReqKNavV4CN0mzH9yR1moinX2LHINMoC7nu//14Eit1FJFDETi9dTVSkTom7A/zRBk1y+Y9H+6I+G4uhtHAVVhVTA0DDAoGAPiAsXfLLLaW3+2CQQgEXBMwiIxC/x6mhxkisTB5MpltwMNNY/0GVxO/oKIYlY8Y0iBXwrFFLZ6bJesbVQ+WpdPBgrhyg9DeeQo5WRXV4UtIISFR9/2rjrfBW7iheGNn9VorxYf5KjoJg6s5HE4s5yii2Ukb4OSGRGEjAsXQPNQkCgYAHK+X3UPcPHgvuN3FtvWLQDek96AMLzqKMxNJ9qcwykWKIhmfQvUiJAeMbFl4MSbWd7h0eqW62dll8SUs9RmEpxIbHB2j/ldomwacAzsmJM4cUmXkJ7Mxc69un4ZFUng91RzYkNvBGf/F+o2JYGj3Fm8SkDC80MGbNnCQQOOu5ig==";
    public static String decrypt(String encryptedData) throws Exception {
        byte[] keyBytes = Base64.decodeBase64(PRIVATE_KEY);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);

        // 这里根据实际情况选择合适的加密模式和填充方式，和前端加密时保持一致
        // 示例用的是 RSA/ECB/PKCS1Padding（jsencrypt 默认的填充方式）
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(javax.crypto.Cipher.DECRYPT_MODE, privateKey);

        byte[] encryptedBytes = Base64.decodeBase64(encryptedData);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes);
    }
}
