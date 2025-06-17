package elec.shop.strategy.email;

import elec.shop.strategy.inter.OrderStatusMessageHandler;
import org.springframework.stereotype.Component;

// 采购中状态处理器
@Component
public class PurchasingHandler implements OrderStatusMessageHandler {
    @Override
    public String getMessage(String orderNo) {
        return "订单 " + orderNo + " 正在采购商品";
    }

    @Override
    public int getStatus() {
        return 3;
    }
}
