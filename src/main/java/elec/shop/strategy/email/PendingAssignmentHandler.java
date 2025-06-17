package elec.shop.strategy.email;

import elec.shop.strategy.inter.OrderStatusMessageHandler;
import org.springframework.stereotype.Component;

// 待分配状态处理器
@Component
public class PendingAssignmentHandler implements OrderStatusMessageHandler {
    @Override
    public String getMessage(String orderNo) {
        return "订单 " + orderNo + " 已创建，等待分配处理人员";
    }

    @Override
    public int getStatus() {
        return 0;
    }
}
