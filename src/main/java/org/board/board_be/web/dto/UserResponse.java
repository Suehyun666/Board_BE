package org.board.board_be.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.board.board_be.domain.user.User;

import java.time.Instant;

/**
 * 사용자 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String nickname;
    private String role;
    private Instant createdAt;

    /**
     * User 엔티티를 DTO로 변환
     */
    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .role(user.getRole() != null ? user.getRole() : "USER")
                .createdAt(user.getCreatedAt())
                .build();
    }
}
