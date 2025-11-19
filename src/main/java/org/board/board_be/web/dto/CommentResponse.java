package org.board.board_be.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.board.board_be.domain.comment.Comment;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {

    private Long id;
    private String content;
    private String authorNickname;
    private Boolean isAuthor;
    private Long parentId;
    private Instant createdAt;

    @Builder.Default
    private List<CommentResponse> replies = new ArrayList<>();

    public static CommentResponse from(Comment comment, Long currentUserId) {
        // 현재 사용자가 작성자인지 확인
        boolean isAuthor = currentUserId != null && comment.getAuthor().getId().equals(currentUserId);

        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorNickname(comment.getAuthor().getNickname())
                .isAuthor(isAuthor)
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
