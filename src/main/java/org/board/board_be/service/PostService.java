package org.board.board_be.service;

import lombok.RequiredArgsConstructor;
import org.board.board_be.domain.post.Post;
import org.board.board_be.domain.post.PostFile;
import org.board.board_be.domain.post.PostRepository;
import org.board.board_be.domain.user.User;
import org.board.board_be.domain.user.UserRepository;
import org.board.board_be.web.dto.PostRequest;
import org.board.board_be.web.dto.PostResponse;
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

    @Transactional(readOnly = true)
    public Page<PostResponse> list(String keyword, Pageable pageable) {
        Page<Post> page = postRepository.search(keyword, pageable);
        return page.map(PostResponse::from);
    }

    @Transactional(readOnly = true)
    public PostResponse get(Long id) {
        Post post = postRepository.findById(id)
                .filter(p -> !p.getIsDeleted())
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));
        return PostResponse.from(post);
    }

    public Long create(Long userId, PostRequest request, List<PostFile> files) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

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
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));

        if (!post.getAuthor().getId().equals(userId)) {
            throw new SecurityException("수정 권한이 없습니다");
        }

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
    }

    public void delete(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .filter(p -> !p.getIsDeleted())
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));

        if (!post.getAuthor().getId().equals(userId)) {
            throw new SecurityException("삭제 권한이 없습니다");
        }

        post.setDeleted(true);
    }
}
