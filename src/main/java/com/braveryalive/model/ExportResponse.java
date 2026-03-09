package com.braveryalive.model;

/**
 * DTO for export format response.
 */
public record ExportResponse(
        String format,
        String content
) {
}
