package com.skdtd.stateMachine.machine;

import com.skdtd.stateMachine.enums.OrderStatus;
import com.skdtd.stateMachine.enums.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import static com.skdtd.stateMachine.entity.Order.ORDER_KEY;

@Slf4j
public class OrderStatusListener extends StateMachineListenerAdapter<OrderStatus, OrderEvent> {
    @Override
    public void stateChanged(State<OrderStatus, OrderEvent> from, State<OrderStatus, OrderEvent> to) {
        log.info(from != null ? "传入状态: " + from.getStates() : "无传入状态");
        log.info(to != null ? "传出状态: " + to.getStates() : "无传出状态");
    }

    @Override
    public void eventNotAccepted(Message<OrderEvent> message) {
        // 事件未被接受时，打印日志
        log.info("事件未被接受: " + message + ", 订单: " + message.getHeaders().get(ORDER_KEY));
    }

    @Override
    public void stateMachineError(StateMachine<OrderStatus, OrderEvent> stateMachine, Exception exception) {
        // 状态机出错时，打印日志
        log.info("状态切换异常, stateMachine: " + stateMachine + ", exception: " + exception.getMessage());
    }
}
