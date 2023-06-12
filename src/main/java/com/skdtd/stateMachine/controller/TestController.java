package com.skdtd.stateMachine.controller;

import com.skdtd.stateMachine.entity.Order;
import com.skdtd.stateMachine.enums.OrderEvent;
import com.skdtd.stateMachine.enums.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.skdtd.stateMachine.enums.OrderEvent.*;

@RestController
@Slf4j
public class TestController {
    @Resource
    private DefaultStateMachinePersister<OrderStatus, OrderEvent, Object> persist;
    @Resource
    private StateMachine<OrderStatus, OrderEvent> stateMachine;
    public static Map<String, Order> map = new HashMap<>();

    @GetMapping("add")
    public String createOrder() {
        String uuid = UUID.randomUUID().toString();
        Order order = new Order();
        order.setId(uuid);
        order.setState(OrderStatus.TO_BE_ALLOCATED);
        map.put(uuid, order);
        return uuid;
    }

    @GetMapping("dispatch/{id}")
    public String dispatchOrder(@PathVariable String id) {
        return (sendEvent(DISPATCH_ORDER, id) ? "派单成功: " : "派单失败: ") + id;
    }

    @GetMapping("startProcessing/{id}")
    public String startProcessing(@PathVariable String id) {
        return (sendEvent(START_PROCESSING, id) ? "启动订单成功: " : "启动订单失败: ") + id;
    }

    @GetMapping("uploadProcessingRecords/{id}")
    public String uploadProcessingRecords(@PathVariable String id) {
        return (sendEvent(UPLOAD_PROCESSING_RECORDS, id) ? "上传处理记录成功: " : "上传处理记录失败: ") + id;
    }

    @GetMapping("processingCompleted/{id}")
    public String processingCompleted(@PathVariable String id) {
        return (sendEvent(PROCESSING_COMPLETED, id) ? "完成订单成功: " : "完成订单失败: ") + id;
    }

    @GetMapping("cancelOrder/{id}")
    public String cancelOrder(@PathVariable String id) {
        return (sendEvent(CANCEL_ORDER, id) ? "取消订单成功: " : "取消订单失败: ") + id;
    }

    @GetMapping("completeOrder/{id}")
    public String completeOrder(@PathVariable String id) {
        return (sendEvent(COMPLETE_ORDER, id) ? "结束订单成功: " : "结束订单失败: ") + id;
    }

    @GetMapping("show")
    public String show() {
        return map.toString();
    }

    private synchronized boolean sendEvent(OrderEvent event, final String id) {
        // TODO 去锁
        log.info("准备发送消息, id: " + id);
        if (id == null) return false;
        Order order = map.get(id);
        Message<OrderEvent> message = MessageBuilder.withPayload(event).setHeader(Order.class.getName(), order).build();
        log.info("创建消息: " + message);
        boolean result;
        try {
            persist.restore(stateMachine, order);
            result = stateMachine.sendEvent(message);
            if (!result) {
                log.warn("切换失败: " + event);
            }
            persist.persist(stateMachine, order);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
