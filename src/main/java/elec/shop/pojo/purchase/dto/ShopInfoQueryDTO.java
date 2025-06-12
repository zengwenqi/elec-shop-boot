package elec.shop.pojo.purchase.dto;

import lombok.Data;

@Data
public class ShopInfoQueryDTO {
    private String shopName;
    private String shopCode;
    private Integer shopType;
    private Integer pageNum;
    private Integer pageSize;
}
