package com.skdtd.stateMachine.machine;

import com.skdtd.stateMachine.entity.Order;
import com.skdtd.stateMachine.enums.OrderEvent;
import com.skdtd.stateMachine.enums.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;
import org.springframework.util.Assert;

import static com.skdtd.stateMachine.entity.Order.ORDER_KEY;
import static com.skdtd.stateMachine.enums.OrderStatus.*;

@Slf4j
public class OrderStatusAction {
    private Order get(StateContext<OrderStatus, OrderEvent> context) {
        Order order = context.getMessage().getHeaders().get(ORDER_KEY, Order.class);
        Assert.notNull(order, "订单信息获取失败");
        return order;
    }

    public void getErrorHandle(StateContext<OrderStatus, OrderEvent> context) {
        Order order = get(context);
        log.warn("状态切换失败, 订单id: " + order.getId());
    }

    public void dispatchOrder(StateContext<OrderStatus, OrderEvent> context) {
        Order order = get(context);
        order.setState(PENDING_PROCESSING);
        log.info("派单, 订单id: " + order.getId());
    }

    public void startProcessing(StateContext<OrderStatus, OrderEvent> context) {
        Order order = get(context);
        order.setState(PROCESSING);
        log.info("开始处理, 订单id: " + order.getId());
    }

    public void uploadProcessingRecords(StateContext<OrderStatus, OrderEvent> context) {
        Order order = get(context);
        order.setState(PROCESSING);
        log.info("上传处理记录, 订单id: " + order.getId());
    }

    public void processingCompleted(StateContext<OrderStatus, OrderEvent> context) {
        Order order = get(context);
        order.setState(PROCESSED);
        log.info("处理完成, 订单id: " + order.getId());
    }

    public void completeOrder(StateContext<OrderStatus, OrderEvent> context) {
        Order order = get(context);
        order.setState(COMPLETED);
        log.info("完成订单, 订单id: " + order.getId());
    }

    public void cancelOrder(StateContext<OrderStatus, OrderEvent> context) {
        Order order = get(context);
        order.setState(CANCELED);
        log.info("取消订单, 订单id: " + order.getId());
    }
}
