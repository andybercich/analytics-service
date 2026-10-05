package org.example.Service;

import org.example.Events.OrderCreatedEvent;
import org.example.Events.OrderDeletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    @Autowired
    private SaveDateService analyticsService;

    @KafkaListener(
            topics = "order-created-events",
            groupId = "analytics-group"
    )
    public void handleOrderCreated(OrderCreatedEvent event) {

        log.info("Order created event received. orderId={}", event.getOrderId());

        try {
            analyticsService.saveOrder(event);

            log.info("Order created event processed successfully. orderId={}", event.getOrderId());

        } catch (Exception ex) {
            log.error("Error processing order created event. orderId={}", event.getOrderId(), ex);
            throw new KafkaException("Error procesando creacion de orden. orderId=" + event.getOrderId(), ex);
        }
    }

    @KafkaListener(
            topics = "order-delete-events",
            groupId = "analytics-group"
    )
    public void handleOrderDeleted(OrderDeletedEvent event) {

        log.info("Order deleted event received. orderId={}", event.getOrderId());

        try {
            analyticsService.deleteOrder(event);
            log.info("Order deleted event processed successfully. orderId={}", event.getOrderId());

        } catch (Exception ex) {

            log.error("Error processing order deleted event. orderId={}", event.getOrderId(), ex);
            throw new KafkaException("Error procesando eliminacion de orden. orderId=" + event.getOrderId(), ex);
        }
    }

    @KafkaListener(
            topics = "order-updated-events",
            groupId = "analytics-group"
    )
    public void handleOrderUpdated(OrderCreatedEvent event) {

        log.info("Order updated event received. orderId={}", event.getOrderId());

        try {

            analyticsService.updateOrder(event);
            log.info("Order updated event processed successfully. orderId={}", event.getOrderId());

        } catch (Exception ex) {

            log.error("Error processing order updated event. orderId={}", event.getOrderId(), ex);
            throw new KafkaException("Error procesando modificacion de orden. orderId=" + event.getOrderId(), ex);
        }
    }
}