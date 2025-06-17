package elec.shop.strategy.email;

import elec.shop.strategy.inter.OrderStatusMessageHandler;
import org.springframework.stereotype.Component;

// 已取消状态处理器
@Component
public class CancelledHandler implements OrderStatusMessageHandler {
    @Override
    public String getMessage(String orderNo) {
        return "订单 " + orderNo + " 已取消";
    }

    @Override
    public int getStatus() {
        return 5;
    }
}
