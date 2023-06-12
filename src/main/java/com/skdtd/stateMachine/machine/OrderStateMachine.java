package com.skdtd.stateMachine.machine;

import com.skdtd.stateMachine.entity.Order;
import com.skdtd.stateMachine.enums.OrderEvent;
import com.skdtd.stateMachine.enums.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineModelConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.config.configurers.ConfigurationConfigurer;
import org.springframework.statemachine.config.configurers.StateConfigurer;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.EnumSet;
import java.util.UUID;

import static com.skdtd.stateMachine.enums.OrderEvent.*;
import static com.skdtd.stateMachine.enums.OrderEvent.CANCEL_ORDER;
import static com.skdtd.stateMachine.enums.OrderStatus.*;
import static com.skdtd.stateMachine.enums.OrderStatus.CANCELED;

@Configuration
@Slf4j
public class OrderStateMachine {

    public final static String ORDER_STATE_MACHINE_ID;


    private final OrderStatusAction action;

    private final StateMachineBuilder.Builder<OrderStatus, OrderEvent> builder;

    static {
        ORDER_STATE_MACHINE_ID = UUID.randomUUID().toString();
    }

    public OrderStateMachine() {
        try {
            this.action = new OrderStatusAction();
            this.builder = StateMachineBuilder.builder();
            configureStates(this.builder.configureStates().withStates());
            configureConfiguration(this.builder.configureConfiguration().withConfiguration());
            configureTransitions(this.builder.configureTransitions());
            configureModel(this.builder.configureModel());
        } catch (Exception e) {
            throw new IllegalArgumentException("状态机初始化失败: " + e.getMessage());
        }
    }

    private void configureStates(StateConfigurer<OrderStatus, OrderEvent> states) throws Exception {
        states.initial(TO_BE_ALLOCATED).states(EnumSet.allOf(OrderStatus.class));
    }

    private void configureModel(StateMachineModelConfigurer<OrderStatus, OrderEvent> stateMachineModel) throws Exception {

    }

    private void configureConfiguration(ConfigurationConfigurer<OrderStatus, OrderEvent> configuration) throws Exception {
        configuration.machineId(ORDER_STATE_MACHINE_ID) // 设置状态机唯一ID标识
                .beanFactory(null) // 指定一个BeanFactory
                .autoStartup(true) // 指定是否自启动是,默认为否
                .listener(new OrderStatusListener()); // 添加监听器
    }

    private void configureTransitions(StateMachineTransitionConfigurer<OrderStatus, OrderEvent> stateMachineTransition) throws Exception {
        stateMachineTransition
                // 派单
                .withExternal().source(TO_BE_ALLOCATED).target(PENDING_PROCESSING).event(DISPATCH_ORDER).action(action::dispatchOrder, action::getErrorHandle)
                // 开始处理
                .and().withExternal().source(PENDING_PROCESSING).target(PROCESSING).event(START_PROCESSING).action(action::startProcessing, action::getErrorHandle)
                // 上传处理记录
                .and().withExternal().source(PROCESSING).target(PROCESSING).event(UPLOAD_PROCESSING_RECORDS).action(action::uploadProcessingRecords, action::getErrorHandle)
                // 处理完成
                .and().withExternal().source(PROCESSING).target(PROCESSED).event(PROCESSING_COMPLETED).action(action::processingCompleted, action::getErrorHandle)
                // 订单完成
                .and().withExternal().source(PROCESSED).target(COMPLETED).event(COMPLETE_ORDER).action(action::completeOrder, action::getErrorHandle)
                // 订单取消
                .and().withExternal().source(TO_BE_ALLOCATED).target(CANCELED).event(CANCEL_ORDER).action(action::cancelOrder, action::getErrorHandle);
    }

    @Bean
    public StateMachine<OrderStatus, OrderEvent> build() {
        return this.builder.build();
    }

    @Bean
    public DefaultStateMachinePersister<OrderStatus, OrderEvent, Order> persist() {
        return new DefaultStateMachinePersister<>(new StateMachinePersist<>() {
            // 读入
            @Override
            public StateMachineContext<OrderStatus, OrderEvent> read(Order order) throws Exception {
                Assert.notNull(order, "获取订单信息失败");
                log.info("读入: " + order);
                return new DefaultStateMachineContext<>(order.getState(), null, null, null, null, ORDER_STATE_MACHINE_ID);
            }

            // 读入
            @Override
            public void write(StateMachineContext context, Order order) throws Exception {
                log.info("写出: " + order);
            }
        });
    }
}
