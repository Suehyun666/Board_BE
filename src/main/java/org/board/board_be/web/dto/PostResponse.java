package org.board.board_be.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.board.board_be.domain.post.Post;
import org.board.board_be.domain.post.PostFile;

import java.time.Instant;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {

    private Long id;
    private String title;
    private String content;
    private String authorNickname;
    private Long viewCount;
    private Long likeCount;
    private Instant createdAt;
    private Instant updatedAt;
    private List<String> fileUrls;

    public static PostResponse from(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorNickname(post.getAuthor().getNickname())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .fileUrls(post.getFiles().stream()
                        .map(PostFile::getFileUrl)
                        .toList())
                .build();
    }
}
