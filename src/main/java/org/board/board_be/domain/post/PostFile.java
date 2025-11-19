package org.board.board_be.domain.post;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "post_files")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String fileUrl;

    @Column(nullable = false, length = 255)
    private String originalName;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false, length = 50)
    private String mimeType;

    @Column(nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    public void setPost(Post post) {
        this.post = post;
    }
}
