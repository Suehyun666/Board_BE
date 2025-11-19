package org.board.board_be.web.exception;

import lombok.extern.slf4j.Slf4j;
import org.board.board_be.web.dto.ApiResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 404 Not Found (핸들러 없음)
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResult<ErrorResponse>> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        String message = String.format("요청한 URL을 찾을 수 없습니다: %s %s", ex.getHttpMethod(), ex.getRequestURL());
        ErrorResponse error = ErrorResponse.of("NOT_FOUND", message);
        return createErrorResponse(HttpStatus.NOT_FOUND, error);
    }

    /**
     * 404 Not Found (리소스 없음 - 커스텀 예외)
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResult<ErrorResponse>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ErrorResponse error = ErrorResponse.of("NOT_FOUND", ex.getMessage());
        return createErrorResponse(HttpStatus.NOT_FOUND, error);
    }

    /**
     * 405 Method Not Allowed
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResult<ErrorResponse>> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        String message = String.format("지원하지 않는 HTTP 메서드입니다: %s (허용된 메서드: %s)",
                ex.getMethod(),
                String.join(", ", ex.getSupportedHttpMethods().stream().map(Object::toString).toList()));
        ErrorResponse error = ErrorResponse.of("METHOD_NOT_ALLOWED", message);
        return createErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, error);
    }

    /**
     * 400 Bad Request (잘못된 인자)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResult<ErrorResponse>> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponse error = ErrorResponse.of("BAD_REQUEST", ex.getMessage());
        return createErrorResponse(HttpStatus.BAD_REQUEST, error);
    }

    /**
     * 400 Bad Request (필수 파라미터 누락) - 추가됨
     * 예: userId가 없을 때 발생
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResult<ErrorResponse>> handleMissingParams(MissingServletRequestParameterException ex) {
        String message = String.format("필수 파라미터가 누락되었습니다: %s", ex.getParameterName());
        ErrorResponse error = ErrorResponse.of("MISSING_PARAMETER", message);
        return createErrorResponse(HttpStatus.BAD_REQUEST, error);
    }

    /**
     * 403 Forbidden (권한 없음)
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiResult<ErrorResponse>> handleSecurityException(SecurityException ex) {
        ErrorResponse error = ErrorResponse.of("FORBIDDEN", ex.getMessage());
        return createErrorResponse(HttpStatus.FORBIDDEN, error);
    }

    /**
     * 400 Bad Request (Validation 실패)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<ErrorResponse>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse error = ErrorResponse.of("VALIDATION_ERROR", "입력값 검증 실패", errors);
        return createErrorResponse(HttpStatus.BAD_REQUEST, error);
    }

    /**
     * 400 Bad Request (타입 불일치)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResult<ErrorResponse>> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        ErrorResponse error = ErrorResponse.of("METHOD_MISMATCH", "유효하지 않은 데이터 타입입니다");
        return createErrorResponse(HttpStatus.BAD_REQUEST, error);
    }

    /**
     * 500 Internal Server Error (그 외 모든 예외)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<ErrorResponse>> handleException(Exception ex) {
        log.error("Unhandled exception occurred", ex);
        ErrorResponse error = ErrorResponse.of("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다");
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, error);
    }

    /**
     * 공통 응답 생성 헬퍼 메서드
     */
    private ResponseEntity<ApiResult<ErrorResponse>> createErrorResponse(HttpStatus status, ErrorResponse error) {
        return ResponseEntity
                .status(status)
                .body(ApiResult.<ErrorResponse>builder()
                        .success(false)
                        .data(error)
                        .build()
                );
    }
}