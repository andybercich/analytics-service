package org.example;

import org.example.Events.StockMovementEvent;
import org.example.Events.StockMovementType;
import org.example.Exception.KafkaListenerException;
import org.example.Model.ProcessedEvent;
import org.example.Model.ProductAnalytics;
import org.example.Repository.ProcessedEventRepository;
import org.example.Repository.ProductAnalyticsRepository;
import org.example.Service.AnalyticsServiceImpl;
import org.example.Service.ProductEventConsumer;
import org.example.Service.SaveDateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@SpringBootTest(
        properties = {
                // Internal
                "internal.secret=test-internal-secret",

                // JWT
                "jwt.secret=test-jwt-secret-daaweda454f87845",
                "jwt.expiration=3600000",

                // MySQL
                "spring.datasource.url=jdbc:mysql://localhost:3306/analytics",
                "spring.datasource.username=root",
                "spring.datasource.password=20042023",

                // Eureka
                "eureka.client.service-url.defaultZone=http://localhost:8761/eureka",
                "eureka.client.register-with-eureka=false",
                "eureka.client.fetch-registry=false",

                // Kafka
                "spring.kafka.bootstrap-servers=localhost:9092",

                // Spring IA
                "spring.ai.openai.api-key=${API_IA}",
                "spring.ai.openai.base-url=https://api.groq.com/openai",
                "spring.ai.openai.chat.model=openai/gpt-oss-120b",
                "spring.ai.openai.chat.temperature=0.3"
        }
    )
    @Transactional
class ProductAnalyticsPersistenceTest {
        @Autowired
        private SaveDateService saveDateService;

        @Autowired
        private ProductAnalyticsRepository productAnalyticsRepository;

        @Autowired
        private ProcessedEventRepository processedEventRepository;

        @Autowired
        private ProductEventConsumer productEventConsumer;


        @Test
        void saveProductAnalytics_shouldSaveAnalyticsAndProcessedEvent() {

            UUID eventId = UUID.randomUUID();

            long initialAnalyticsCount = productAnalyticsRepository.count();
            long initialProcessedEventsCount = processedEventRepository.count();

            StockMovementEvent event = new StockMovementEvent(
                    eventId,
                    1L,
                    "Coca Cola",
                    StockMovementType.RESTOCK,
                    10,
                    15,
                    5,
                    "Bebidas",
                    LocalDateTime.now()
            );

            saveDateService.saveProductAnalytics(event);

            List<ProductAnalytics> analytics =
                    productAnalyticsRepository.findAll();

            assertEquals(initialAnalyticsCount + 1, analytics.size());

            ProductAnalytics productAnalytics = analytics.stream()
                    .filter(a -> a.getProductId().equals(1L))
                    .filter(a -> a.getProductName().equals("Coca Cola"))
                    .filter(a -> a.getMovementType().equals(StockMovementType.RESTOCK))
                    .findFirst()
                    .orElseThrow();

            assertEquals(1L, productAnalytics.getProductId());
            assertEquals("Coca Cola", productAnalytics.getProductName());
            assertEquals("Bebidas", productAnalytics.getCategory());
            assertEquals(5, productAnalytics.getQuantityChanged());
            assertEquals(
                    StockMovementType.RESTOCK,
                    productAnalytics.getMovementType()
            );
            assertEquals(10, productAnalytics.getStockBefore());
            assertEquals(15, productAnalytics.getStockAfter());
            assertNotNull(productAnalytics.getTimestamp());

            List<ProcessedEvent> processedEvents =
                    processedEventRepository.findAll();

            assertEquals(initialProcessedEventsCount + 1, processedEvents.size());

            ProcessedEvent processedEvent = processedEvents.stream()
                    .filter(p -> p.getEventId().equals(eventId))
                    .findFirst()
                    .orElseThrow();

            assertEquals(eventId, processedEvent.getEventId());
            assertEquals("STOCK_MOVEMENT", processedEvent.getEventType());
            assertNotNull(processedEvent.getProcessedAt());
        }

    @Test
    void saveProductAnalytics_shouldIgnoreDuplicatedEvent() {

        UUID eventId = UUID.randomUUID();

        long initialAnalyticsCount = productAnalyticsRepository.count();
        long initialProcessedEventsCount = processedEventRepository.count();

        StockMovementEvent event = new StockMovementEvent(
                eventId,
                1L,
                "Coca Cola",
                StockMovementType.RESTOCK,
                10,
                15,
                5,
                "Bebidas",
                LocalDateTime.now()
        );

        saveDateService.saveProductAnalytics(event);

        long afterFirstAnalyticsCount = productAnalyticsRepository.count();
        long afterFirstProcessedEventsCount = processedEventRepository.count();

        saveDateService.saveProductAnalytics(event);

        long afterSecondAnalyticsCount = productAnalyticsRepository.count();
        long afterSecondProcessedEventsCount = processedEventRepository.count();

        assertEquals(
                initialAnalyticsCount + 1,
                afterFirstAnalyticsCount
        );

        assertEquals(
                initialProcessedEventsCount + 1,
                afterFirstProcessedEventsCount
        );

        assertEquals(
                afterFirstAnalyticsCount,
                afterSecondAnalyticsCount
        );

        assertEquals(
                afterFirstProcessedEventsCount,
                afterSecondProcessedEventsCount
        );
    }

      @ExtendWith(MockitoExtension.class)
      class ProductEventConsumerTest {

            @Mock
            private SaveDateService analyticsService;

            @InjectMocks
            private ProductEventConsumer productEventConsumer;

            @Test
            void handleStockMovement_shouldProcessEventSuccessfully() {

                  UUID eventId = UUID.randomUUID();

                  StockMovementEvent event = new StockMovementEvent(
                          eventId,
                          1L,
                          "Coca Cola",
                          StockMovementType.RESTOCK,
                          10,
                          15,
                          5,
                          "Bebidas",
                          LocalDateTime.now()
                  );

                  productEventConsumer.handleStockMovement(event);

                  verify(analyticsService)
                          .saveProductAnalytics(event);
            }
      }

      @Test
      void saveProductAnalytics_shouldIgnoreDuplicateEvent() {

            UUID eventId = UUID.randomUUID();

            long initialAnalyticsCount = productAnalyticsRepository.count();
            long initialProcessedEventsCount = processedEventRepository.count();

            StockMovementEvent event = new StockMovementEvent(
                    eventId,
                    1L,
                    "Coca Cola",
                    StockMovementType.RESTOCK,
                    10,
                    15,
                    5,
                    "Bebidas",
                    LocalDateTime.now()
            );

            saveDateService.saveProductAnalytics(event);

            saveDateService.saveProductAnalytics(event);

            assertEquals(
                    initialAnalyticsCount + 1,
                    productAnalyticsRepository.count()
            );

            assertEquals(
                    initialProcessedEventsCount + 1,
                    processedEventRepository.count()
            );
            ProcessedEvent processedEvent = processedEventRepository.findAll()
                    .stream()
                    .filter(p -> p.getEventId().equals(eventId))
                    .findFirst()
                    .orElseThrow();

            assertEquals(eventId, processedEvent.getEventId());
            assertEquals("STOCK_MOVEMENT", processedEvent.getEventType());
            assertNotNull(processedEvent.getProcessedAt());
      }


}
