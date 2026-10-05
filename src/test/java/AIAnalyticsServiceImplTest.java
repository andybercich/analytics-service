
import org.example.Model.DtoAndRecords.*;
import org.example.Model.Enum.AnalyticsProductType;
import org.example.Model.MonthlyProductAnalytics;

import org.example.Service.AIAnalyticsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.test.util.ReflectionTestUtils;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIAnalyticsServiceImplTest {
    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec responseSpec;

    @InjectMocks
    private AIAnalyticsServiceImpl aiAnalyticsService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(aiAnalyticsService, "model", "gpt-4o-mini");
    }

    @Test
    void shouldGenerateMonthlyInsightsSuccessfully() {
        // Arrange
        ReportDataDTO reportData = new ReportDataDTO(
            new BigDecimal("1000000"), new BigDecimal("300000"),
                new BigDecimal("30"), new BigDecimal("50000"),
            20L, 50L, 10L,
                LocalDate.of(2026, 8, 15),
                5L, LocalDate.of(2026, 8, 20),
                new BigDecimal("250000"),
            List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of()
        );

        MonthlyProductAnalytics product = new MonthlyProductAnalytics();
        product.setYear(2026);
        product.setMonth(8);
        product.setProductId(1L);
        product.setProductName("Notebook Lenovo IdeaPad");
        product.setQuantitySold(20L);
        product.setRevenue(new BigDecimal("500000"));
        product.setProfit(new BigDecimal("170000"));
        product.setRanking(1);
        product.setType(AnalyticsProductType.TOP_SELLER);

        List<MonthlyProductAnalytics> products = List.of(product);

        AIReportInsights expectedInsights = new AIReportInsights(
            "El negocio presenta un buen desempeño general.",
                "Las ventas muestran un comportamiento positivo.",
            "El margen de beneficio es adecuado.",
                "Los pedidos presentan concentración en determinados días.",
                "Las pérdidas requieren seguimiento.",
                "El producto más relevante presenta un buen desempeño.",
            List.of("Revisar los productos con mayores pérdidas.", "Analizar los días de mayor actividad.")
        );

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.options(any(OpenAiChatOptions.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.entity(AIReportInsights.class)).thenReturn(expectedInsights);

        AIReportInsights result = aiAnalyticsService.generateMonthlyInsights(reportData, products);

        assertNotNull(result);
        assertEquals(expectedInsights, result);
        verify(chatClient).prompt();
        verify(requestSpec).user(anyString());
        verify(requestSpec).options(any(OpenAiChatOptions.class));
        verify(requestSpec).call();
        verify(responseSpec).entity(AIReportInsights.class);
    }
}