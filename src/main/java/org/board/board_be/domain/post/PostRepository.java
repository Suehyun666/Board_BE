package org.board.board_be.domain.post;

import org.board.board_be.web.dto.PostListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * 메인페이지용 게시글 목록 조회 (쿼리 최적화)
     * - Fetch Join으로 N+1 문제 해결
     * - DTO Projection으로 필요한 필드만 조회
     * - 댓글 수 COUNT로 함께 조회
     */
    @Query("""
        SELECT new org.board.board_be.web.dto.PostListResponse(
            p.id,
            p.title,
            u.nickname,
            p.viewCount,
            p.likeCount,
            COUNT(c.id),
            p.createdAt
        )
        FROM Post p
        JOIN p.author u
        LEFT JOIN Comment c ON c.post.id = p.id AND c.isDeleted = false
        WHERE p.isDeleted = false
          AND (:keyword IS NULL
               OR p.title LIKE CONCAT('%', :keyword, '%')
               OR p.content LIKE CONCAT('%', :keyword, '%'))
        GROUP BY p.id, p.title, u.nickname, p.viewCount, p.likeCount, p.createdAt
        ORDER BY p.createdAt DESC
    """)
    Page<PostListResponse> searchList(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 상세 조회용 (기존)
     * - Fetch Join으로 연관 엔티티 함께 조회
     */
    @Query("""
        SELECT p FROM Post p
        JOIN FETCH p.author
        LEFT JOIN FETCH p.files
        WHERE p.id = :id AND p.isDeleted = false
    """)
    Post findByIdWithDetails(@Param("id") Long id);
}
