package org.board.board_be.web.exception;

import lombok.Builder;

import java.time.Instant;
import java.util.Map;

@Builder
public record ErrorResponse(
        String code,
        String message,
        Map<String, String> details,
        Instant timestamp
) {
}
