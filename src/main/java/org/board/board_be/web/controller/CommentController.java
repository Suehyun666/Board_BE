package org.board.board_be.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.board.board_be.service.CommentService;
import org.board.board_be.web.dto.CommentRequest;
import org.board.board_be.web.dto.CommentResponse;
import org.board.board_be.web.exception.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "댓글", description = "댓글 CRUD API")
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글 목록 조회", description = "특정 게시글의 댓글 목록 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/api/posts/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> list(
            @Parameter(description = "게시글 ID", required = true) @PathVariable Long postId) {
        return ResponseEntity.ok(commentService.list(postId));
    }

    @Operation(summary = "댓글 작성", description = "댓글 또는 대댓글 작성")
    @ApiResponse(responseCode = "200", description = "작성 성공")
    @ApiResponse(responseCode = "400", description = "입력값 검증 실패", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/api/posts/{postId}/comments")
    public ResponseEntity<Map<String, Long>> create(
            @Parameter(description = "게시글 ID", required = true) @PathVariable Long postId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "댓글 정보") @RequestBody @Valid CommentRequest request) {
        Long currentUserId = 1L; // TODO: Security
        Long commentId = commentService.create(postId, currentUserId, request);
        return ResponseEntity.ok(Map.of("id", commentId));
    }

    @Operation(summary = "댓글 수정", description = "댓글 내용 수정")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @ApiResponse(responseCode = "403", description = "수정 권한 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PutMapping("/api/comments/{id}")
    public ResponseEntity<Void> update(
            @Parameter(description = "댓글 ID", required = true) @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "수정할 댓글 정보") @RequestBody @Valid CommentRequest request) {
        Long currentUserId = 1L; // TODO: Security
        commentService.update(id, currentUserId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "댓글 삭제", description = "댓글 소프트 삭제")
    @ApiResponse(responseCode = "200", description = "삭제 성공")
    @ApiResponse(responseCode = "403", description = "삭제 권한 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @DeleteMapping("/api/comments/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "댓글 ID", required = true) @PathVariable Long id) {
        Long currentUserId = 1L; // TODO: Security
        commentService.delete(id, currentUserId);
        return ResponseEntity.ok().build();
    }
}
