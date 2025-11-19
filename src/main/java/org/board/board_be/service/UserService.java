package org.board.board_be.service;

import lombok.RequiredArgsConstructor;
import org.board.board_be.domain.user.User;
import org.board.board_be.domain.user.UserRepository;
import org.board.board_be.web.dto.UserRequest;
import org.board.board_be.web.dto.UserResponse;
import org.board.board_be.web.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 서비스
 */
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 회원가입
     */
    public Long register(UserRequest request) {
        userRepository.findByUsername(request.getUsername())
                .ifPresent(u -> {
                    throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
                });

        String role = "USER";

        // 비밀번호 평문 저장(임시)
        User user = User.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .nickname(request.getNickname())
                .role(role)
                .build();

        User saved = userRepository.save(user);
        return saved.getId();
    }

    /**
     * 로그인
     */
    @Transactional(readOnly = true)
    public UserResponse login(UserRequest.LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다"));

        // TODO: 비밀번호 암호화 비교 (BCrypt)
        if (!user.getPassword().equals(request.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다");
        }

        return UserResponse.from(user);
    }

    /**
     * 내 정보 조회
     */
    @Transactional(readOnly = true)
    public UserResponse getMyInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자", userId));
        return UserResponse.from(user);
    }

    /**
     * 회원 탈퇴
     */
    public void delete(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자", userId));
        userRepository.delete(user);
    }
}
