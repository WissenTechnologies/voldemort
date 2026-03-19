package com.example.order.scheduler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.order.client.PriceClient;
import com.example.order.entities.Order;
import com.example.order.service.OrderService;

@Component
public class OrderScheduler {

    @Autowired
    private OrderService orderService;

    @Autowired
    private PriceClient priceClient;

    @Scheduled(fixedRate = 500)
public void processOrders() {

    List<Order> orders = orderService.getPendingOrders();

    boolean marketOpen = orderService.isMarketOpen();

    for (Order order : orders) {

        // ❌ Market closed → expire order
        if (!marketOpen) {
            order.setStatus("FAILED"); // or "EXPIRED"
            orderService.save(order);
            continue;
        }

        Double price = priceClient.getPrice(order.getCompanyId());
        if (price == null) continue;

        if (orderService.shouldExecute(order, price)) {
            orderService.executeOrder(order, price);
        }
    }
}

    private boolean shouldExecute(Order order, Double price) {

        if ("LIMIT".equals(order.getOrderMode())) {
            if ("BUY".equals(order.getType())) {
                return price <= order.getTargetPrice();
            } else {
                return price >= order.getTargetPrice();
            }
        }

        if ("STOP_LOSS".equals(order.getOrderMode())) {
            return price <= order.getTargetPrice();
        }

        return false;
    }
}