package it.baldilorenzo.hibernate_envers_101.dto;

import java.time.Instant;

public record RevisionDto<T>(
        long revisionNumber,
        Instant revisionDate,
        String username,
        String revisionType,
        T entity
) {}
