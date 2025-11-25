package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 自定义定时任务：实现订单状态的定时处理
 */
@Slf4j
@Component
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理支付超时订单
     */
    @Scheduled(cron = "0 * * * * ? ")
    public void processTimeoutOrder() {
        log.info("处理支付超时订单",new Date());

        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        List<Orders> list = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, time);
        if (list != null && list.size() > 0) {
            for (Orders orders : list) {
                orderMapper.update(Orders.builder()
                        .id(orders.getId())
                        .status(Orders.CANCELLED)
                        .cancelReason("订单支付超时，已取消")
                        .cancelTime(LocalDateTime.now())
                        .build());
            }
        }
    }

    /**
     * 处理"派送中"状态的订单
     */
    @Scheduled(cron = "0 0 1 * * ? ")
    public void processDeliveryOrder() {
        log.info("处理派送中状态的订单",new Date());

        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        List<Orders> list = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, time);
        if (list != null && list.size() > 0) {
            for (Orders orders : list) {
                orderMapper.update(Orders.builder()
                        .id(orders.getId())
                        .status(Orders.COMPLETED)
                        .deliveryTime(LocalDateTime.now())
                        .build());
            }
        }
    }
}
