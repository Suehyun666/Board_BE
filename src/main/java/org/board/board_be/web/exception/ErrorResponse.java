package org.board.board_be.web.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

@Schema(description = "에러 응답")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        @Schema(description = "에러 코드", example = "BAD_REQUEST")
        String code,

        @Schema(description = "에러 메시지", example = "입력값이 올바르지 않습니다")
        String message,

        @Schema(description = "상세 에러 정보 (필드별)")
        Map<String, String> details,

        @Schema(description = "발생 시각", example = "2025-11-19T13:51:02.744Z")
        Instant timestamp
) {
    // Lombok Builder 제거, 일반 정적 팩토리 메서드 사용
    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, null, Instant.now());
    }

    public static ErrorResponse of(String code, String message, Map<String, String> details) {
        return new ErrorResponse(code, message, details, Instant.now());
    }
}
