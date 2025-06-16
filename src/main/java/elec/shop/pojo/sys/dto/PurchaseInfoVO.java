package elec.shop.pojo.sys.dto;

import lombok.Data;

@Data
public class PurchaseInfoVO {
    private Long purchaserId;
    private String purchaserName;
    private String realName;
    private String purchaserCode;
    private String email;
    private String mobile;
    private String gender;
    private String status;
}
