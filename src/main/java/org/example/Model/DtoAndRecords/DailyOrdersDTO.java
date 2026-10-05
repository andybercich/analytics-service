package org.example.Model.DtoAndRecords;

import java.time.LocalDate;

public record DailyOrdersDTO(
        LocalDate date,
        Long orderCount
) {}