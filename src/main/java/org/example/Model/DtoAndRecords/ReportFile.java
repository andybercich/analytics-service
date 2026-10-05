package org.example.Model.DtoAndRecords;

import org.springframework.core.io.Resource;

public record ReportFile(
        String fileName, Resource resource
) {}
