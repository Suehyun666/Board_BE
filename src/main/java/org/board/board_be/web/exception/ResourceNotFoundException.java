package org.board.board_be.web.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s을(를) 찾을 수 없습니다 (ID: %d)", resourceName, id));
    }
}
