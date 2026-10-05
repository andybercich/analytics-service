package org.example.Model.DtoAndRecords;

import java.math.BigDecimal;
import java.time.LocalDate;

public record WeeklyRevenueDTO(
        LocalDate weekStart,
        BigDecimal revenue
) {}
