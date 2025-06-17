package elec.shop.strategy.factory;

import elec.shop.strategy.inter.OrderStatusMessageHandler;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OrderStatusMessageHandlerFactory {
    private final Map<Integer, OrderStatusMessageHandler> handlerMap = new HashMap<>();
    private final List<OrderStatusMessageHandler> handlers;

    public OrderStatusMessageHandlerFactory(List<OrderStatusMessageHandler> handlers) {
        this.handlers = handlers;
    }

    @PostConstruct
    public void init() {
        for (OrderStatusMessageHandler handler : handlers) {
            handlerMap.put(handler.getStatus(), handler);
        }
    }

    public OrderStatusMessageHandler getHandler(int status) {
        return handlerMap.get(status);
    }
}
