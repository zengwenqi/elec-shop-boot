package elec.shop.strategy.email;

import elec.shop.strategy.inter.OrderStatusMessageHandler;
import org.springframework.stereotype.Component;

// 已完成状态处理器
@Component
public class CompletedHandler implements OrderStatusMessageHandler {
    @Override
    public String getMessage(String orderNo) {
        return "订单 " + orderNo + " 已完成，感谢您的购买";
    }

    @Override
    public int getStatus() {
        return 4;
    }
}
