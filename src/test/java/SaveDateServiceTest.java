import org.example.Events.OrderCreatedEvent;
import org.example.Events.OrderDeletedEvent;
import org.example.Events.OrderItemEvent;
import org.example.Model.OrderAnalytics;
import org.example.Model.ProcessedEvent;
import org.example.Repository.OrderAnalyticsRepository;
import org.example.Repository.ProcessedEventRepository;
import org.example.Repository.ProductAnalyticsRepository;
import org.example.Service.SaveDateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaveDateServiceTest {

    @Mock
    private OrderAnalyticsRepository repository;

    @Mock
    private ProductAnalyticsRepository productAnalyticsRepository;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @InjectMocks
    private SaveDateService saveDateService;

    @Test
    void shouldNotProcessOrderCreatedEventTwice() {

        UUID eventId = UUID.randomUUID();

        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setEventId(eventId);
        event.setOrderId(100L);
        event.setTotal(new BigDecimal("2000"));
        event.setCreatedAt(LocalDateTime.now());

        OrderItemEvent item = new OrderItemEvent();
        item.setProductId(1L);
        item.setProductName("Producto Test");
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("1000"));
        item.setUnitCost(new BigDecimal("600"));
        item.setProfitLine(new BigDecimal("800"));

        event.setItems(List.of(item));

        when(processedEventRepository.existsByEventId(eventId))
                .thenReturn(false)
                .thenReturn(true);

        saveDateService.saveOrder(event);
        saveDateService.saveOrder(event);

        verify(repository, times(1))
                .save(any(OrderAnalytics.class));

        verify(processedEventRepository, times(1))
                .save(any(ProcessedEvent.class));
    }

    @Test
    void shouldUpdateOrderAnalyticsWhenEventIsNew() {

        UUID eventId = UUID.randomUUID();

        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setEventId(eventId);
        event.setOrderId(100L);
        event.setTotal(new BigDecimal("3000"));
        event.setCreatedAt(LocalDateTime.now());

        OrderItemEvent item = new OrderItemEvent();
        item.setProductId(1L);
        item.setProductName("Producto Actualizado");
        item.setQuantity(3);
        item.setUnitPrice(new BigDecimal("1000"));
        item.setUnitCost(new BigDecimal("600"));
        item.setProfitLine(new BigDecimal("1200"));

        event.setItems(List.of(item));

        when(processedEventRepository.existsByEventId(eventId))
                .thenReturn(false);

        saveDateService.updateOrder(event);

        verify(repository)
                .deleteByOrderId(100L);

        verify(repository)
                .save(any(OrderAnalytics.class));

        verify(processedEventRepository)
                .save(argThat(processedEvent ->
                        processedEvent.getEventId().equals(eventId)
                                && processedEvent.getEventType().equals("ORDER_UPDATED")
                ));
    }

    @Test
    void shouldNotUpdateOrderAnalyticsWhenEventWasAlreadyProcessed() {

        UUID eventId = UUID.randomUUID();

        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setEventId(eventId);
        event.setOrderId(100L);

        when(processedEventRepository.existsByEventId(eventId))
                .thenReturn(true);

        saveDateService.updateOrder(event);

        verify(repository, never())
                .deleteByOrderId(anyLong());

        verify(repository, never())
                .save(any(OrderAnalytics.class));

        verify(processedEventRepository, never())
                .save(any(ProcessedEvent.class));
    }

    @Test
    void shouldDeleteOrderAnalyticsWhenDeleteEventIsNew() {

        UUID eventId = UUID.randomUUID();

        OrderDeletedEvent event = new OrderDeletedEvent();
        event.setEventId(eventId);
        event.setOrderId(100L);
        event.setDeletedAt(LocalDateTime.now());

        when(processedEventRepository.existsByEventId(eventId))
                .thenReturn(false);

        saveDateService.deleteOrder(event);

        verify(repository)
                .deleteByOrderId(100L);

        verify(processedEventRepository)
                .save(argThat(processedEvent ->
                        processedEvent.getEventId().equals(eventId)
                                && processedEvent.getEventType().equals("ORDER_DELETED")
                ));
    }

    @Test
    void shouldNotDeleteOrderAnalyticsWhenDeleteEventWasAlreadyProcessed() {

        UUID eventId = UUID.randomUUID();

        OrderDeletedEvent event = new OrderDeletedEvent();
        event.setEventId(eventId);
        event.setOrderId(100L);
        event.setDeletedAt(LocalDateTime.now());

        when(processedEventRepository.existsByEventId(eventId))
                .thenReturn(true);

        saveDateService.deleteOrder(event);

        verify(repository, never())
                .deleteByOrderId(anyLong());

        verify(processedEventRepository, never())
                .save(any(ProcessedEvent.class));
    }
}
