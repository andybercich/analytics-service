package org.example.Model.DtoAndRecords;

import lombok.Data;

@Data
public class ApiError {

    private final String code;
    private final String message;

}
