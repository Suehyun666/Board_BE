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
import org.board.board_be.web.dto.ApiResult;
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
    @GetMapping("/boards/{postId}/comments")
    public ResponseEntity<ApiResult<List<CommentResponse>>> list(
            @Parameter(description = "게시글 ID", required = true) @PathVariable Long postId,
            @Parameter(description = "현재 사용자 ID (로그인한 경우)", required = false) @RequestParam(required = false) Long userId) {

        return ResponseEntity.ok(
                ApiResult.<List<CommentResponse>>builder()
                        .success(true)
                        .data(commentService.list(postId, userId))
                        .build()
        );
    }

    @Operation(summary = "댓글 작성", description = "댓글 또는 대댓글 작성")
    @ApiResponse(responseCode = "200", description = "작성 성공")
    @PostMapping("/boards/{postId}/comments")
    public ResponseEntity<ApiResult<Map<String, Long>>> create(
            @Parameter(description = "게시글 ID", required = true) @PathVariable Long postId,
            @Parameter(description = "작성자 ID (테스트용)", required = true) @RequestParam Long userId, // 수정됨
            @RequestBody @Valid CommentRequest request) {

        Long commentId = commentService.create(postId, userId, request);

        return ResponseEntity.ok(
                ApiResult.<Map<String, Long>>builder()
                        .success(true)
                        .data(Map.of("id", commentId))
                        .build()
        );
    }

    @Operation(summary = "댓글 수정", description = "댓글 내용 수정")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @PutMapping("/boards/{postId}/comments/{id}")
    public ResponseEntity<ApiResult<Void>> update(
            @Parameter(description = "게시글 ID", required = true) @PathVariable Long postId,
            @Parameter(description = "댓글 ID", required = true) @PathVariable Long id,
            @Parameter(description = "작성자 ID (본인 확인용)", required = true) @RequestParam Long userId,
            @RequestBody @Valid CommentRequest request) {

        commentService.update(postId, id, userId, request);

        return ResponseEntity.ok(
                ApiResult.<Void>builder()
                        .success(true)
                        .data(null)
                        .build()
        );
    }

    @Operation(summary = "댓글 삭제", description = "댓글 소프트 삭제")
    @ApiResponse(responseCode = "200", description = "삭제 성공")
    @DeleteMapping("/boards/{postId}/comments/{id}")
    public ResponseEntity<ApiResult<Void>> delete(
            @Parameter(description = "게시글 ID", required = true) @PathVariable Long postId,
            @Parameter(description = "댓글 ID", required = true) @PathVariable Long id,
            @Parameter(description = "작성자 ID (본인 확인용)", required = true) @RequestParam Long userId) {

        commentService.delete(postId, id, userId);

        return ResponseEntity.ok(
                ApiResult.<Void>builder()
                        .success(true)
                        .data(null)
                        .build()
        );
    }
}