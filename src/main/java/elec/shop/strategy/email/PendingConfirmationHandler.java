package elec.shop.strategy.email;

import elec.shop.strategy.inter.OrderStatusMessageHandler;
import org.springframework.stereotype.Component;

// 待确认状态处理器
@Component
public class PendingConfirmationHandler implements OrderStatusMessageHandler {
    @Override
    public String getMessage(String orderNo) {
        return "订单 " + orderNo + " 已分配，等待客户确认";
    }

    @Override
    public int getStatus() {
        return 1;
    }
}
