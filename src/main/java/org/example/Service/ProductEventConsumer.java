package org.example.Service;

import lombok.RequiredArgsConstructor;
import org.example.Events.StockMovementEvent;
import org.example.Exception.KafkaListenerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductEventConsumer {

        private static final Logger log = LoggerFactory.getLogger(ProductEventConsumer.class);
            private final SaveDateService analyticsService;
        @KafkaListener(
                topics = "stock-movement-events",
                groupId = "analytics-group"
        )
        public void handleStockMovement(StockMovementEvent event) {

            log.info(
                    "Stock movement event received. eventId={}, productId={}, movementType={}, quantity={}",
                    event.getEventId(),
                    event.getProductId(),
                    event.getMovementType(),
                    event.getQuantityChanged()
            );

            try {

                analyticsService.saveProductAnalytics(event);

                log.info(
                        "Stock movement event processed successfully. eventId={}, productId={}",
                        event.getEventId(),
                        event.getProductId()
                );

            } catch (Exception ex) {

                log.error(
                        "Error processing stock movement event. eventId={}, productId={}",
                        event.getEventId(),
                        event.getProductId(),
                        ex
                );

                throw new KafkaListenerException(
                        "Error procesando movimientos de stock. eventId=" +
                                event.getEventId() +
                                ", productId=" +
                                event.getProductId(),
                        ex
                );
            }
        }
}