package elec.shop.strategy.inter;

// 状态消息处理器接口
public interface OrderStatusMessageHandler {
    String getMessage(String orderNo);
    int getStatus();
}
