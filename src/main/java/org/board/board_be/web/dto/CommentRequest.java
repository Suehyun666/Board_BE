package org.board.board_be.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "댓글 작성/수정 요청")
public class CommentRequest {

    @Schema(description = "댓글 내용", example = "좋은 글 감사합니다!", required = true)
    @NotBlank(message = "댓글 내용은 필수입니다")
    @Size(max = 1000, message = "댓글은 최대 1,000자입니다")
    private String content;

    @Schema(description = "부모 댓글 ID (대댓글인 경우)", example = "1", required = false)
    private Long parentId;
}
