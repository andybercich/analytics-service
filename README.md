# Analytics Service

The `analytics-service` is the analytical and reporting microservice of the system.

It maintains its own MySQL database and receives data from other microservices through Kafka events. Instead of querying the operational databases directly, it processes events and builds its own analytical data model.

The service uses this data to generate monthly and annual reports, including charts and AI-generated insights through Groq.

Its main purpose is to keep analytics decoupled from operational services and provide a practical way to transform business data into reports and relevant insights.

## Responsibilities

The main responsibilities of `analytics-service` are:

* Consume order and stock-related events from Kafka.
* Store analytical information in its own database.
* Prevent duplicate event processing.
* Maintain analytical data independently from operational databases.
* Generate monthly and annual reports.
* Generate business charts using JFreeChart.
* Generate PDF reports using iText.
* Generate AI-based insights using Groq.
* Store generated reports locally or in AWS S3, depending on the configured storage implementation.
* Expose authenticated endpoints for report generation and retrieval.
* Provide health and application information through Spring Boot Actuator.

## Architecture

`analytics-service` follows an event-driven architecture.

Operational microservices publish events to Kafka. Analytics consumers receive those events and delegate their processing to `SaveDataService`, which validates the event and persists the corresponding analytical information.

```text
                 ┌─────────────────────┐
                 │    Order Service     │
                 └──────────┬──────────┘
                            │
                         Events
                            │
                 ┌──────────▼──────────┐
                 │        Kafka         │
                 └──────────┬──────────┘
                            │
             ┌──────────────┴──────────────┐
             │                             │
     OrderEventConsumer          ProductEventConsumer
             │                             │
             └──────────────┬──────────────┘
                            ▼
                   ┌─────────────────┐
                   │ SaveDataService │
                   │                 │
                   │ Event filtering │
                   │ UUID validation │
                   │ Idempotency     │
                   └────────┬────────┘
                            │
             ┌──────────────┼───────────────┐
             ▼              ▼               ▼
      OrderAnalytics  ProductAnalytics  ProcessedEvent
             │              │
             └──────────────┴──────────────┐
                                           ▼
                                  Analytics Database
                                           │
                                           ▼
                                    AnalyticsService
                                           │
                         ┌─────────────────┼─────────────────┐
                         ▼                 ▼                 ▼
                   ChartService      AIAnalyticsService   Queries
                         │                 │
                    JFreeChart           Groq
                         │                 │
                         └────────┬────────┘
                                  ▼
                              PDFService
                                  │
                                  ▼
                             ReportStorage
                                  │
                         ┌────────┴────────┐
                         ▼                 ▼
                    Local reports        AWS S3
```

## Technology Stack

* Java 17
* Spring Boot
* Spring Data JPA
* MySQL
* Apache Kafka
* JFreeChart
* iText
* Spring Boot Actuator
* Redis
* Groq API
* Docker
* AWS S3

## Database

`analytics-service` has its own MySQL database.

It does not share the operational database tables used by `pedido-service` or `product-service`.

Instead, analytical information is built from events received through Kafka.

This separation allows the reporting process to work with its own data model without requiring direct access to the operational databases.

### Main Tables

The service currently contains the following main analytical tables:

* `OrderAnalytics`
* `ProductAnalytics`
* `MonthlyAnalytics`
* `MonthlyProductAnalytics`
* `ProcessedEvent`
* `GeneratedReport`

`ProcessedEvent` is used as part of the event idempotency mechanism.

The analytical tables also use unique identifiers and constraints to prevent duplicate analytical records.

## Event-Driven Data Processing

The service consumes the following event types:

* `ORDER_CREATED`
* `ORDER_UPDATED`
* `ORDER_DELETED`
* `STOCK_MOVEMENT`

### Order Events

`OrderEventConsumer` receives order-related events and delegates their processing to `SaveDataService`.

For a new order, the event UUID generated by the producing microservice is checked against previously processed events.

If the UUID has already been processed, the event is ignored.

If the UUID is new, the event is stored in the corresponding analytical data.

For `ORDER_UPDATED`, the existing analytical information associated with the order is removed and the updated state is stored.

For `ORDER_DELETED`, the corresponding order analytical data is removed.

### Stock Movement Events

`ProductEventConsumer` processes `STOCK_MOVEMENT` events.

The event UUID is checked before saving the information.

If the event has already been processed, it is ignored. Otherwise, the stock movement is stored in `ProductAnalytics`.

## Event Idempotency

Event idempotency is an important part of the service.

Kafka-based systems can deliver the same event more than once. Processing the same event multiple times could result in duplicated analytical information.

To prevent this, `SaveDataService` checks the event UUID against the `ProcessedEvent` table before processing it.

```text
Kafka Event
     │
     ▼
SaveDataService
     │
     ▼
Is UUID already processed?
     │
 ┌───┴────┐
 │        │
Yes       No
 │        │
 ▼        ▼
Ignore   Process
          │
          ├── Save analytical data
          │
          └── Save event as processed
```

The analytical tables also use unique identifiers/constraints as an additional protection against duplicated data.

This means that duplicate events do not result in duplicate analytical processing.

## SaveDataService

`SaveDataService` is responsible for the central processing of events received by the Kafka consumers.

Its responsibilities include:

* Receiving event data from consumers.
* Identifying the event type.
* Filtering events according to their type.
* Checking whether the event UUID was already processed.
* Persisting order analytical information.
* Updating analytical information when an order changes.
* Removing analytical information when an order is deleted.
* Persisting stock movement information.
* Registering processed events in `ProcessedEvent`.

This keeps the event consumers focused on consuming and delegating events while the persistence and idempotency logic remains centralized.

## Analytics and Report Generation

Once analytical data has been stored, the reporting layer queries the analytics database and prepares the information required to generate a report.

The report generation process can use:

* Sales information
* Revenue
* Profit
* Product performance
* Stock movements
* Daily order patterns
* Hourly order patterns
* Weekly revenue
* Weekly profit

The analytical information is transformed into the datasets required by the chart and reporting services.

## Report Types

The service currently supports:

### Monthly Reports

Monthly reports can be generated:

* Without AI insights.
* With AI-generated insights.

### Annual Reports

Annual reports can be generated with AI-generated insights.

The generated report contains the analytical information and visualizations produced from the stored data.

## Chart Generation

`ChartService` uses JFreeChart to generate visual representations of the analytical data.

The service currently generates charts for:

### Top Products

Displays the products with the highest relevant sales performance.

```java
chartService.createTopProductsChart(
    data.topProducts()
);
```

### Revenue and Profit by Week

Compares weekly revenue and profit.

```java
chartService.createRevenueProfitChart(
    data.revenueByWeek(),
    data.profitByWeek()
);
```

### Stock Movements

Displays stock movement information.

```java
chartService.createStockMovementChart(
    data.stockMovements()
);
```

### Top Profit Products

Displays products with the highest generated profit.

```java
chartService.createTopProfitProductsChart(
    data.topProfitProducts()
);
```

### Orders by Day

Displays the distribution of orders by day.

```java
chartService.createOrdersByDayChart(
    data.dailyOrders()
);
```

### Orders by Hour

Displays the distribution of orders throughout the day.

```java
chartService.createOrdersByHourChart(
    data.ordersByHour()
);
```

## PDF Generation

`PDFService` is responsible for assembling the final report.

It uses iText to generate PDF documents and incorporates the generated analytical information and charts.

The resulting report can include:

* Business metrics
* Analytical summaries
* Generated charts
* AI insights when enabled
* Recommendations generated by the AI analysis

## AI Integration

The service integrates with Groq through `AIAnalyticsService`.

The AI does not directly query the database.

Instead, `analytics-service` first obtains and simplifies the relevant analytical information. This information is then included as context in a prompt sent to Groq.

```text
Analytics Database
       │
       ▼
Analytics Queries
       │
       ▼
Simplified Analytical Context
       │
       ▼
AIAnalyticsService
       │
       ▼
Groq
       │
       ▼
AI Insights
       │
       ▼
PDF Report
```

The AI can use information such as:

* Revenue
* Profit
* Product performance
* Stock movements
* Order behavior
* Other relevant business metrics calculated by the analytics service

The purpose is to transform the raw analytical results into a more practical interpretation of the information that matters to the business.

## AI Failure Handling

AI generation is not required for the report generation process to succeed.

Both monthly and annual report generation handle failures from the AI service.

If Groq is unavailable or an error occurs while generating the insights:

1. The error is logged.
2. A fallback AI response is generated.
3. Report generation continues.
4. The PDF can still be generated without depending on a successful AI response.

This prevents an external AI dependency from becoming a single point of failure for report generation.

## Report Storage

Generated reports are stored locally in a `reports` directory by default.

The storage implementation can also be configured to use Amazon S3.

This allows the same reporting functionality to work in a local/Docker environment while supporting cloud storage when deployed to AWS.

```text
ReportService
     │
     ▼
PDFService
     │
     ▼
ReportStorage
     │
     ├── Local storage
     │
     └── AWS S3
```

## Security

`analytics-service` is not intended to be directly accessible from outside the internal network.

Its application port is:

```text
8083
```

During deployment, this port is not exposed directly to external clients.

Requests must go through the API Gateway.

All report endpoints require authentication.

Report permissions are role-based:

* `ADMIN`: can generate reports and access generated reports.
* `USER`: can access/download generated reports but cannot generate them.

This keeps report generation restricted while allowing authorized users to retrieve existing reports.

## Rate Limiting

Rate limiting is implemented through Redis and the API Gateway.

This helps control the number of requests reaching protected operations such as report generation.

The rate limiting mechanism is applied at the Gateway layer rather than exposing the analytics service directly to external traffic.

## API Gateway

The API Gateway acts as the public HTTP entry point for the system.

The analytics service is accessed internally through the Gateway.

```text
Client
  │
  ▼
API Gateway
  │
  ├── Authentication
  ├── Authorization
  ├── Rate Limiting
  └── Routing
          │
          ▼
   analytics-service
```

The service itself does not expose its application port directly to external clients.

## Monitoring and Observability

Spring Boot Actuator is enabled for application monitoring.

The available Actuator endpoints include:

* `health`
* `info`

The service also uses structured logging and trace information to make requests and event processing easier to follow across the microservice architecture.

`X-Trace-Id` / correlation information is used to correlate logs between services.

## Error Handling

The service uses a global exception handling mechanism through `GlobalExceptionHandler`.

This centralizes application error handling and provides consistent responses when errors occur.

Combined with logging and trace IDs, this makes failures easier to identify and investigate.

## Testing

The service includes tests covering important parts of the analytical workflow.

Current test coverage includes:

* PDF generation
* Event processing and persistence
* Chart generation

These tests help validate the main functionality responsible for transforming event-driven data into analytical reports.

## Docker

`analytics-service` is containerized and designed to run as part of the complete microservices environment.

The service communicates with other internal components through the Docker/internal network, including:

* Kafka
* MySQL
* Redis-related infrastructure through the API Gateway
* Other microservices where required

The application port remains internal when deployed as part of the complete system.

## Reliability

The service is designed to avoid making report generation dependent on the availability of every external component.

Kafka provides the event-driven communication layer between operational services and analytics.

The analytics database provides an independent copy of the information required for reporting.

Event UUID validation prevents duplicate processing.

Groq failures do not prevent report generation because the AI layer has a fallback mechanism.

The API Gateway provides authentication, authorization and rate limiting before requests reach the service.

## Data Flow

The complete analytical flow can be summarized as:

```text
Operational Microservices
          │
          │ Kafka Events
          ▼
        Kafka
          │
          ▼
   Analytics Consumers
          │
          ▼
    SaveDataService
          │
          ├── Check UUID
          │
          ├── Check ProcessedEvent
          │
          └── Store analytical data
                    │
                    ▼
             Analytics Database
                    │
                    ▼
             AnalyticsService
                    │
          ┌─────────┴─────────┐
          ▼                   ▼
    ChartService       AIAnalyticsService
          │                   │
     JFreeChart              Groq
          │                   │
          └─────────┬─────────┘
                    ▼
                PDFService
                    │
                    ▼
              ReportStorage
                    │
             ┌──────┴──────┐
             ▼             ▼
          Local           S3
```

## Failure Scenarios

### Kafka Unavailable

If Kafka becomes unavailable, events produced by the operational services remain handled by their respective reliability mechanisms, including the transactional outbox.

When Kafka becomes available again, pending events can be published and consumed by `analytics-service`.

### Analytics Service Unavailable

If `analytics-service` is temporarily unavailable while Kafka remains available, the events remain in Kafka until the analytics consumers are able to process them.

### Duplicate Kafka Event

If the same event is delivered more than once, `SaveDataService` checks its UUID against `ProcessedEvent`.

Already processed events are ignored.

### Groq Unavailable

If Groq cannot generate AI insights, the report generation process uses the configured fallback and continues generating the report.

The availability of the AI provider therefore does not determine whether the report itself can be generated.

## Project Role

`analytics-service` provides the analytical layer of the microservices architecture.

Instead of coupling reporting directly to operational databases, it builds its own analytical model from events.

This provides a clear separation between:

```text
Operational Data
      │
      ▼
Event-Driven Communication
      │
      ▼
Analytical Data
      │
      ├── Charts
      ├── Reports
      └── AI Insights
```

The result is a reporting service that can process business information independently from the operational services and turn that information into practical reports and AI-assisted insights.
