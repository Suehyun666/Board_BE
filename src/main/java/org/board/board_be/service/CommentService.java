package org.board.board_be.service;

import lombok.RequiredArgsConstructor;
import org.board.board_be.domain.comment.Comment;
import org.board.board_be.domain.comment.CommentRepository;
import org.board.board_be.domain.post.Post;
import org.board.board_be.domain.post.PostRepository;
import org.board.board_be.domain.user.User;
import org.board.board_be.domain.user.UserRepository;
import org.board.board_be.web.dto.CommentRequest;
import org.board.board_be.web.dto.CommentResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CommentResponse> list(Long postId) {
        List<Comment> comments = commentRepository.findByPostIdAndIsDeletedFalseOrderByCreatedAtAsc(postId);
        return comments.stream()
                .map(CommentResponse::from)
                .toList();
    }

    public Long create(Long postId, Long userId, CommentRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        Comment.CommentBuilder builder = Comment.builder()
                .post(post)
                .author(author)
                .content(request.getContent());

        if (request.getParentId() != null) {
            Comment parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다"));
            builder.parent(parent);
        }

        Comment comment = builder.build();
        Comment saved = commentRepository.save(comment);
        return saved.getId();
    }

    public void update(Long postId, Long commentId, Long userId, CommentRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .filter(c -> !c.getIsDeleted())
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다"));

        // postId 검증: 댓글이 해당 게시글에 속하는지 확인
        if (!comment.getPost().getId().equals(postId)) {
            throw new IllegalArgumentException("해당 게시글의 댓글이 아닙니다");
        }

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new SecurityException("수정 권한이 없습니다");
        }

        comment.setContent(request.getContent());
    }

    public void delete(Long postId, Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .filter(c -> !c.getIsDeleted())
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다"));

        // postId 검증: 댓글이 해당 게시글에 속하는지 확인
        if (!comment.getPost().getId().equals(postId)) {
            throw new IllegalArgumentException("해당 게시글의 댓글이 아닙니다");
        }

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new SecurityException("삭제 권한이 없습니다");
        }

        comment.setDeleted(true);
    }
}
