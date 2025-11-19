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
@Schema(description = "게시글 작성/수정 요청")
public class PostRequest {

    @Schema(description = "게시글 제목", example = "안녕하세요, 첫 게시글입니다!", required = true)
    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 150, message = "제목은 최대 150자입니다")
    private String title;

    @Schema(description = "게시글 내용", example = "이것은 게시글 내용입니다. 마크다운을 지원합니다.", required = true)
    @NotBlank(message = "내용은 필수입니다")
    @Size(max = 10000, message = "내용은 최대 10,000자입니다")
    private String content;
}
