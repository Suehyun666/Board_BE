package org.board.board_be.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiResult<T> {
    private boolean success;
    private T data;
}
