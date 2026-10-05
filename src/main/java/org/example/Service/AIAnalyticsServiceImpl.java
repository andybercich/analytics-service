package org.example.Service;

import lombok.RequiredArgsConstructor;
import org.example.Events.StockMovementType;
import org.example.Exception.AIServiceException;
import org.example.Model.DtoAndRecords.*;
import org.example.Model.HourlyOrder;
import org.example.Model.MonthlyAnalytics;
import org.example.Model.MonthlyProductAnalytics;
import org.example.Service.Interfaces.AIAnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AIAnalyticsServiceImpl implements AIAnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AIAnalyticsServiceImpl.class);

    private final ChatClient chatClient;

    @Value("${spring.ai.openai.chat.model}")
    private String model;

    @Override
    public AIReportInsights generateMonthlyInsights(
            ReportDataDTO reportDataDTO,
            List<MonthlyProductAnalytics> products) {

        log.info("Initializing generation of monthly insights with AI. model={}", model);
        try {
            String prompt = buildPrompt(reportDataDTO, products);

            AIReportInsights insights = chatClient
                    .prompt()
                    .user(prompt)
                    .options(
                            OpenAiChatOptions.builder()
                                    .model(model)
                                    .temperature(0.3)
                                    .build()
                    )
                    .call()
                    .entity(AIReportInsights.class);

            log.info("Monthly insights generate. model={}", model);
            return insights;

        } catch (Exception e) {
            log.error("Error generating monthly insights with AI. model={}", model, e);
            throw new AIServiceException("Error generando insights con ia", e);
        }
    }

    public AIAnnualReportInsights generateAnnualInsights(
            AnnualAIData annualAIData) {

        log.info("Iniciando generación de insights anuales con AI. year={}, model={}", annualAIData.year(), model);

        try {
            String prompt = """
                    Analiza el rendimiento anual del negocio correspondiente al año %d.

                    DATOS GENERALES MENSUALES:
                    %s

                    RESUMEN ANUAL POR PRODUCTO:
                    %s

                    Realiza un análisis empresarial basado únicamente en los datos proporcionados.

                    Identifica:

                    1. Evolución general de ingresos y ganancias durante el año.
                    2. Meses con mejor y peor rendimiento.
                    3. Productos con mejor rendimiento comercial.
                    4. Productos que generaron mayor rentabilidad.
                    5. Productos con mayores pérdidas.
                    6. Productos que aparecen repetidamente entre los destacados.
                    7. Productos con alta cantidad de ventas pero baja rentabilidad.
                    8. Productos con baja cantidad de ventas pero alta rentabilidad.
                    9. Tendencias importantes observadas durante el año.
                    10. Recomendaciones concretas para mejorar ventas, rentabilidad y gestión del inventario.

                    No inventes datos que no estén presentes.
                    Diferencia claramente entre datos observados y recomendaciones.
                    """.formatted(
                    annualAIData.year(),
                    annualAIData.monthly(),
                    annualAIData.products()
            );

            AIAnnualReportInsights insights = chatClient
                    .prompt()
                    .user(prompt)
                    .options(
                            OpenAiChatOptions.builder()
                                    .model(model)
                                    .temperature(0.3)
                                    .build()
                    )
                    .call()
                    .entity(AIAnnualReportInsights.class);

            log.info(
                    "Annual insights generate correctly. year={}, model={}",
                    annualAIData.year(),
                    model
            );

            return insights;

        } catch (Exception e) {
            log.error(
                    "Error generate annual insights with AI. year={}, model={}",
                    annualAIData.year(),
                    model,
                    e
            );
            throw new AIServiceException("Error generando insights con ia", e);
        }
    }

    private String buildPrompt(
            ReportDataDTO reportDataDTO,
            List<MonthlyProductAnalytics> products) {

        return """
                Analiza los siguientes datos de un reporte empresarial mensual.

                Tu objetivo es actuar como un analista senior de ventas,
                rentabilidad, inventario y comportamiento de pedidos. Debes
                detectar tendencias, comportamientos relevantes, posibles
                problemas y oportunidades de mejora.

                IMPORTANTE:
                - No te limites a repetir los valores proporcionados.
                - Busca relaciones entre las diferentes métricas.
                - Calcula porcentajes y proporciones cuando puedan obtenerse
                  correctamente a partir de los datos proporcionados.
                - Compara volumen de ventas contra rentabilidad.
                - Identifica concentración de ventas en pocos productos.
                - Identifica productos con alta rotación.
                - Identifica productos con alta rentabilidad aunque tengan
                  un volumen de ventas bajo.
                - Identifica productos con pérdidas relevantes.
                - Compara las unidades perdidas con las unidades vendidas
                  cuando sea posible.
                - Identifica diferencias relevantes entre productos más
                  vendidos y productos más rentables.
                - Analiza el comportamiento temporal de los pedidos.
                - Busca anomalías o comportamientos que merezcan atención.
                - Las recomendaciones deben ser específicas y accionables.
                - Cuando sea posible, menciona los productos, fechas y valores
                  que justifican cada recomendación.

                NO HAGAS:
                - No inventes datos.
                - No inventes causas.
                - No afirmes causalidades que los datos no permitan demostrar.
                - No realices recomendaciones genéricas sin justificar.
                - No repitas simplemente las métricas del reporte.
                - No supongas información que no esté presente.

                Si una conclusión no puede obtenerse de los datos,
                indícalo explícitamente.


                EJEMPLO DE ANÁLISIS ESPERADO:

                En lugar de decir:
                "El producto X fue el más vendido."

                Debes intentar aportar contexto, por ejemplo:
                "El producto X representó aproximadamente el 25%% de las
                unidades vendidas del período, lo que indica una elevada
                concentración de las ventas en este producto."

                En lugar de decir:
                "Mejorar la gestión del inventario."

                Debes relacionar la recomendación con los datos:
                "Las pérdidas representan aproximadamente el 40%% de las
                unidades vendidas. Conviene revisar qué productos concentran
                las pérdidas y analizar sus movimientos de stock."


                DATOS GENERALES DEL MES:

                Revenue total: %s
                Profit total: %s
                Margen de beneficio: %s

                Total de pedidos: %s
                Productos vendidos: %s
                Productos distintos: %s

                Ticket promedio: %s


                COMPORTAMIENTO TEMPORAL DE LOS PEDIDOS:

                Mejor día por cantidad de pedidos: %s
                Pedidos del mejor día: %s

                Mejor día por revenue: %s
                Revenue del mejor día: %s


                DATOS DE INVENTARIO:

                Unidades perdidas: %s


                DATOS DE PRODUCTOS:

                %s
                        
                DATOS DE ACTIVIDAD POR HORARIO:
                        
                %s


                INSTRUCCIONES DE ANÁLISIS:


                Para el análisis de ventas:

                - Determina qué productos concentran el volumen de ventas.
                - Calcula su participación sobre el total cuando sea posible.
                - Compara los productos más vendidos entre sí.
                - Analiza si las ventas están concentradas en pocos productos.
                - Relaciona el volumen vendido con la rentabilidad.


                Para el análisis de rentabilidad:

                - Identifica los productos que más contribuyen al profit.
                - Diferencia productos de alta rotación de productos de alta
                  rentabilidad.
                - Analiza la relación entre cantidad vendida, revenue y profit.
                - Detecta productos que vendan mucho pero tengan una contribución
                  relativamente baja al profit.


                Para el análisis de comportamiento de pedidos:

                - Identifica el día con mayor cantidad de pedidos.
                - Identifica el día con mayor facturación.
                - Determina si ambos días coinciden o son diferentes.
                - Si son diferentes, explica qué puede concluirse únicamente
                  a partir de los valores disponibles.
                - Compara el volumen de pedidos del mejor día contra la
                  facturación del mejor día cuando sea posible.
                - Identifica si existe una diferencia relevante entre el día
                  de mayor actividad y el día de mayor facturación.
                - No inventes causas para explicar por qué determinados días
                  tuvieron mayor actividad.
                - No afirmes que existe una tendencia semanal, horaria o
                  estacional si los datos proporcionados no permiten demostrarlo.


                Para el análisis de stock:

                - Analiza las pérdidas.
                - Compara unidades perdidas contra unidades vendidas cuando
                  sea posible.
                - Identifica productos que aparezcan asociados a pérdidas.
                - No afirmes que existe un problema de inventario si los datos
                  no son suficientes para demostrarlo.


                Para el análisis de productos:

                - Compara TOP_SELLER, TOP_PROFIT y TOP_LOSS.
                - Detecta productos que aparezcan en más de una categoría.
                - Identifica productos que tengan comportamientos especialmente
                  relevantes.
                - Explica por qué son relevantes utilizando los datos.
                        
                        Para el análisis de patrones horarios:
                                
                        - Identifica la hora o franja horaria con mayor cantidad de pedidos.
                        - Indica cuántos pedidos se registraron en esa franja.
                        - Identifica períodos de alta y baja actividad cuando los datos lo permitan.
                        - Compara las franjas horarias cuando exista una diferencia relevante.
                        - No inventes causas para los patrones observados.
                        - No afirmes que una franja es "pico" si la diferencia respecto de las demás
                          no es significativa.


                Para las recomendaciones:

                - Genera entre 3 y 5 recomendaciones.
                - Cada recomendación debe estar respaldada por datos.
                - Prioriza acciones concretas.
                - Evita recomendaciones genéricas.
                - No recomiendes aumentar stock únicamente porque un producto
                  vende mucho; utiliza también la información disponible sobre
                  rentabilidad y pérdidas.
                - Si detectas una concentración importante de pedidos o revenue
                  en determinados días, puedes mencionarlo como consideración
                  operativa, pero no inventes las causas.


                GENERA LA RESPUESTA SIGUIENDO EXACTAMENTE ESTA ESTRUCTURA:


                executiveSummary:
                Resumen ejecutivo del comportamiento general del negocio.
                Debe incluir las tendencias más importantes y no limitarse
                a repetir los KPIs.


                salesInsight:
                Análisis de ventas, volumen, concentración y productos vendidos.


                profitabilityInsight:
                Análisis de revenue, profit, margen y diferencias entre
                volumen de ventas y rentabilidad.


                orderPatternInsight:
                Análisis del comportamiento temporal de los pedidos.
                Debe comparar el día con mayor cantidad de pedidos contra
                el día con mayor facturación y explicar las diferencias
                relevantes que puedan deducirse de los datos.


                stockInsight:
                Análisis de pérdidas y comportamiento del inventario.


                productInsight:
                Análisis de los productos más relevantes y sus relaciones
                entre ventas, rentabilidad y pérdidas.


                recommendations:
                Lista de 3 a 5 recomendaciones concretas, específicas y
                justificadas por los datos.
                        
                        
                """.formatted(

                reportDataDTO.totalRevenue(),
                reportDataDTO.totalProfit(),
                reportDataDTO.profitMargin(),

                reportDataDTO.totalOrders(),
                reportDataDTO.totalProductsSold(),
                reportDataDTO.distinctProducts(),

                reportDataDTO.averageOrderValue(),

                reportDataDTO.bestSalesDay(),
                reportDataDTO.bestSalesDayOrders(),
                reportDataDTO.bestRevenueDay(),
                reportDataDTO.bestRevenueDayAmount(),

                reportDataDTO.stockMovements().stream()
                        .filter(m -> m.movementType() == StockMovementType.LOSS)
                        .mapToLong(StockMovementSummaryDTO::quantity)
                        .sum(),

                formatProducts(products),

                formatHourlyOrders(reportDataDTO.ordersByHour())
        );
    }
    private String formatProducts(
            List<MonthlyProductAnalytics> products) {

        return products.stream()
                .map(product -> """
                        Producto:
                        ID: %d
                        Nombre: %s
                        Tipo de análisis: %s
                        Ranking: %d
                        Cantidad vendida: %d
                        Cantidad perdida: %s
                        Revenue: %s
                        Profit: %s
                        """.formatted(
                        product.getProductId(),
                        product.getProductName(),
                        product.getType(),
                        product.getRanking(),
                        product.getQuantitySold(),
                        product.getQuantityLost(),
                        product.getRevenue(),
                        product.getProfit()
                ))
                .collect(Collectors.joining("\n"));
    }

    private String formatHourlyOrders(List<HourlyOrder> ordersByHour) {

        if (ordersByHour == null || ordersByHour.isEmpty()) {
            return "No hay datos de pedidos por horario disponibles.";
        }

        return ordersByHour.stream()
                .sorted(Comparator.comparing(HourlyOrder::getHour))
                .map(dto -> String.format(
                        "%02d:00 - %d pedidos",
                        dto.getHour(),
                        dto.getOrderCount()
                ))
                .collect(Collectors.joining("\n"));
    }
}
