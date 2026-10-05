package org.example.Handler;

import org.example.Exception.*;
import org.example.Model.DtoAndRecords.ApiError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidReportRequestException.class)
    public ResponseEntity<ApiError> handleInvalidReportRequest(
            InvalidReportRequestException exception) {

        log.warn("Invalid report request. message={}", exception.getMessage());

        return ResponseEntity
                .badRequest()
                .body(new ApiError(
                        "INVALID_REPORT_REQUEST",
                        exception.getMessage()
                ));
    }

    @ExceptionHandler(ReportNotFoundException.class)
    public ResponseEntity<ApiError> handleReportNotFound(
            ReportNotFoundException exception) {

        log.warn("Report not found. message={}", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiError(
                        "REPORT_NOT_FOUND",
                        exception.getMessage()
                ));
    }

    @ExceptionHandler(ReportGenerationException.class)
    public ResponseEntity<ApiError> handleReportGeneration(
            ReportGenerationException exception) {

        log.error("Error generating report", exception);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError(
                        "REPORT_GENERATION_ERROR",
                        "No se pudo generar el reporte."
                ));
    }

    @ExceptionHandler(GraphicGenerationException.class)
    public ResponseEntity<ApiError> handleReportGeneration(
            GraphicGenerationException exception) {

        log.error("Error generating report", exception);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError(
                        "GRAPHIC_GENERATION_ERROR",
                            exception.getMessage()
                ));
    }


    @ExceptionHandler(KafkaListenerException.class)
    public ResponseEntity<ApiError> handleReportGeneration(
            KafkaListenerException exception) {

        log.error("Error generating report", exception);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError(
                        "KAFKA_LISTENER_ERROR",
                        exception.getMessage()
                ));
    }

    @ExceptionHandler(AIServiceException.class)
    public ResponseEntity<ApiError> handleAIService(
            AIServiceException exception) {

        log.error("Error communicating with AI service", exception);

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ApiError(
                        "AI_SERVICE_UNAVAILABLE",
                        "El servicio de inteligencia artificial no está disponible."
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(
            IllegalArgumentException exception) {

        log.warn("Invalid argument. message={}", exception.getMessage());

        return ResponseEntity
                .badRequest()
                .body(new ApiError(
                        "INVALID_REQUEST",
                        exception.getMessage()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleException(
            Exception exception) {

        log.error("Unexpected error in analytics-service", exception);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError(
                        "INTERNAL_SERVER_ERROR",
                        "Ocurrió un error interno al procesar la solicitud."
                ));
    }
}