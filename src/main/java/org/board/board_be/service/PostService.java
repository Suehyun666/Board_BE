package org.board.board_be.service;

import lombok.RequiredArgsConstructor;
import org.board.board_be.domain.post.Post;
import org.board.board_be.domain.post.PostFile;
import org.board.board_be.domain.post.PostRepository;
import org.board.board_be.domain.user.User;
import org.board.board_be.domain.user.UserRepository;
import org.board.board_be.web.dto.PostListResponse;
import org.board.board_be.web.dto.PostRequest;
import org.board.board_be.web.dto.PostResponse;
import org.board.board_be.web.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /**
     * 메인페이지용 게시글 목록 조회 (경량화, 쿼리 최적화)
     */
    @Transactional(readOnly = true)
    public Page<PostListResponse> list(String keyword, Pageable pageable) {
        return postRepository.searchList(keyword, pageable);
    }

    /**
     * 게시글 상세 조회 (전체 정보)
     */
    @Transactional(readOnly = true)
    public PostResponse get(Long id) {
        Post post = postRepository.findByIdWithDetails(id);
        if (post == null) {
            throw new ResourceNotFoundException("게시글", id);
        }
        return PostResponse.from(post);
    }

    public Long create(Long userId, PostRequest request, List<PostFile> files) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자", userId));

        Post post = Post.builder()
                .author(author)
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        if (files != null && !files.isEmpty()) {
            for (PostFile file : files) {
                post.addFile(file);
            }
        }

        Post saved = postRepository.save(post);
        return saved.getId();
    }

    public void update(Long postId, Long userId, PostRequest request) {
        Post post = postRepository.findById(postId)
                .filter(p -> !p.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("게시글", postId));

        if (!post.getAuthor().getId().equals(userId)) {
            throw new SecurityException("수정 권한이 없습니다");
        }

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
    }

    public void delete(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .filter(p -> !p.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("게시글", postId));

        if (!post.getAuthor().getId().equals(userId)) {
            throw new SecurityException("삭제 권한이 없습니다");
        }

        post.setDeleted(true);
    }
}
