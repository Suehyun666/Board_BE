package org.board.board_be.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.board.board_be.domain.post.Post;
import org.board.board_be.domain.post.PostFile;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {

    private Long id;
    private String title;
    private String content;
    private String authorNickname;
    private Boolean isAuthor;
    private Long viewCount;
    private Long likeCount;
    private Instant createdAt;
    private Instant updatedAt;
    private List<PostFileDto> files;
    private List<CommentResponse> comments;

    public static PostResponse from(Post post, List<CommentResponse> comments, Long currentUserId) {
        // 현재 사용자가 작성자인지 확인
        boolean isAuthor = currentUserId != null && post.getAuthor().getId().equals(currentUserId);

        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorNickname(post.getAuthor().getNickname())
                .isAuthor(isAuthor)
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .files(post.getFiles().stream()
                        .map(PostFileDto::from)
                        .collect(Collectors.toList()))
                .comments(comments)
                .build();
    }

    @Getter
    @Builder
    public static class PostFileDto {
        private Long id;
        private String originalName;
        private String fileUrl;
        private Long fileSize;
        private String mimeType;

        public static PostFileDto from(PostFile file) {
            return PostFileDto.builder()
                    .id(file.getId())
                    .originalName(file.getOriginalName())
                    .fileUrl(file.getFileUrl())
                    .fileSize(file.getFileSize())
                    .mimeType(file.getMimeType())
                    .build();
        }
    }
}