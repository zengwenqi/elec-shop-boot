package elec.shop.strategy.email;

import elec.shop.strategy.inter.OrderStatusMessageHandler;
import org.springframework.stereotype.Component;

// 已确认状态处理器
@Component
public class ConfirmedHandler implements OrderStatusMessageHandler {
    @Override
    public String getMessage(String orderNo) {
        return "订单 " + orderNo + " 已确认，开始处理";
    }

    @Override
    public int getStatus() {
        return 2;
    }
}
