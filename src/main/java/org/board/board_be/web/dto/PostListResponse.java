package org.board.board_be.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 메인페이지용 게시글 목록 응답 (경량화)
 * - 본문(content) 제외
 * - 파일 URL 제외
 * - 댓글 수 포함
 */
@Schema(description = "게시글 목록 응답 (간단)")
@Getter
@NoArgsConstructor
@Builder
public class PostListResponse {

    @Schema(description = "게시글 ID", example = "1")
    private Long id;

    @Schema(description = "제목", example = "첫 번째 게시글")
    private String title;

    @Schema(description = "작성자 닉네임", example = "사용자1")
    private String authorNickname;

    @Schema(description = "조회수", example = "120")
    private Long viewCount;

    @Schema(description = "좋아요 수", example = "15")
    private Long likeCount;

    @Schema(description = "댓글 수", example = "5")
    private Long commentCount;

    @Schema(description = "작성일", example = "2025-11-19T13:51:02.744Z")
    private Instant createdAt;

    // Projection을 위한 생성자 (JPQL에서 직접 사용)
    public PostListResponse(Long id, String title, String authorNickname,
                           Long viewCount, Long likeCount, Long commentCount, Instant createdAt) {
        this.id = id;
        this.title = title;
        this.authorNickname = authorNickname;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.createdAt = createdAt;
    }
}
