package org.example.Service;

import org.example.Events.OrderCreatedEvent;
import org.example.Events.OrderDeletedEvent;
import org.example.Events.OrderItemEvent;
import org.example.Events.StockMovementEvent;
import org.example.Model.OrderAnalytics;
import org.example.Model.ProcessedEvent;
import org.example.Model.ProductAnalytics;
import org.example.Repository.OrderAnalyticsRepository;
import org.example.Repository.ProcessedEventRepository;
import org.example.Repository.ProductAnalyticsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;




@Service
public class SaveDateService {

    private static final Logger log = LoggerFactory.getLogger(SaveDateService.class);

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @Autowired
    private OrderAnalyticsRepository repository;

    @Autowired
    private ProductAnalyticsRepository productAnalyticsRepository;

    private void saveOrderProcessed(OrderCreatedEvent event, String eventType) {
        if (processedEventRepository.existsByEventId(event.getEventId())) {
            log.info(
                    "Order event already processed. orderId={}, eventId={}, eventType={}",
                    event.getOrderId(),
                    event.getEventId(),
                    eventType
            );

            return;
        }

        for (OrderItemEvent item : event.getItems()) {

            OrderAnalytics entity = new OrderAnalytics();
            entity.setOrderId(event.getOrderId());
            entity.setProductId(item.getProductId());
            entity.setProductName(item.getProductName());
            entity.setItemType(item.getItemType());
            entity.setQuantity(item.getQuantity());
            entity.setUnitPrice(item.getUnitPrice());
            entity.setUnitCost(item.getUnitCost());
            entity.setProfit(item.getProfitLine());
            entity.setTotal(event.getTotal());
            entity.setCreatedAt(event.getCreatedAt());

            repository.save(entity);
        }

        ProcessedEvent processedEvent = new ProcessedEvent();

        processedEvent.setEventId(event.getEventId());
        processedEvent.setEventType(eventType);
        processedEvent.setProcessedAt(LocalDateTime.now());

        processedEventRepository.save(processedEvent);

        log.info(
                "Order analytics saved successfully. orderId={}, eventId={}, eventType={}",
                event.getOrderId(),
                event.getEventId(),
                eventType
        );
    }

    @Transactional
    public void saveOrder(OrderCreatedEvent event) {

        saveOrderProcessed(event, "ORDER_CREATED");
    }

    @Transactional
    public void updateOrder(OrderCreatedEvent event) {

        log.info(
                "Updating order analytics. orderId={}, eventId={}",
                event.getOrderId(),
                event.getEventId()
        );

        if (processedEventRepository.existsByEventId(event.getEventId())) {
            log.info(
                    "Order update event already processed. orderId={}, eventId={}",
                    event.getOrderId(),
                    event.getEventId()
            );
            return;
        }

        repository.deleteByOrderId(event.getOrderId());

        log.info(
                "Deleted analytics for orderId={}",
                event.getOrderId()
        );


        saveOrderProcessed(event, "ORDER_UPDATED");
    }

    @Transactional
    public void deleteOrder(OrderDeletedEvent event) {

        log.info(
                "Deleting order analytics. orderId={}, eventId={}",
                event.getOrderId(),
                event.getEventId()
        );

        if (processedEventRepository.existsByEventId(event.getEventId())) {

            log.info(
                    "Order delete event already processed. orderId={}, eventId={}",
                    event.getOrderId(),
                    event.getEventId()
            );

            return;
        }

        repository.deleteByOrderId(event.getOrderId());

        ProcessedEvent processedEvent = new ProcessedEvent();

        processedEvent.setEventId(event.getEventId());
        processedEvent.setEventType("ORDER_DELETED");
        processedEvent.setProcessedAt(LocalDateTime.now());

        processedEventRepository.save(processedEvent);

        log.info(
                "Order analytics deleted successfully. orderId={}, eventId={}",
                event.getOrderId(),
                event.getEventId()
        );
    }


    @Transactional
    public void saveProductAnalytics(StockMovementEvent event) {

        log.info(
                "Saving product analytics. eventId={}, productId={}, movementType={}, quantity={}",
                event.getEventId(),
                event.getProductId(),
                event.getMovementType(),
                event.getQuantityChanged()
        );

        if (processedEventRepository.existsByEventId(event.getEventId())) {

            log.warn(
                    "Stock movement event already processed. eventId={}, productId={}",
                    event.getEventId(),
                    event.getProductId()
            );

            return;
        }

        ProductAnalytics productAnalytics = new ProductAnalytics();

        productAnalytics.setProductId(event.getProductId());
        productAnalytics.setProductName(event.getProductName());
        productAnalytics.setCategory(event.getCategory());
        productAnalytics.setQuantityChanged(event.getQuantityChanged());
        productAnalytics.setMovementType(event.getMovementType());
        productAnalytics.setStockBefore(event.getStockBefore());
        productAnalytics.setStockAfter(event.getStockAfter());
        productAnalytics.setTimestamp(event.getTimestamp());

        productAnalyticsRepository.save(productAnalytics);


        ProcessedEvent processedEvent = new ProcessedEvent();

        processedEvent.setEventId(event.getEventId());
        processedEvent.setEventType("STOCK_MOVEMENT");
        processedEvent.setProcessedAt(LocalDateTime.now());

        processedEventRepository.save(processedEvent);

        log.info(
                "Product analytics saved and event marked as processed. eventId={}, productId={}",
                event.getEventId(),
                event.getProductId()
        );
    }
}
