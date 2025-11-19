package org.board.board_be.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.board.board_be.domain.post.PostFile;
import org.board.board_be.service.FileStorageService;
import org.board.board_be.service.PostService;
import org.board.board_be.web.dto.PostListResponse;
import org.board.board_be.web.dto.PostRequest;
import org.board.board_be.web.dto.PostResponse;
import org.board.board_be.web.exception.ErrorResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Tag(name = "게시글", description = "게시글 CRUD API")
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final FileStorageService fileStorageService;

    @Operation(
        summary = "게시글 목록 조회 (메인페이지용)",
        description = "쿼리 최적화된 게시글 목록 조회 - 본문 제외, 댓글 수 포함, N+1 해결"
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseEntity<Page<PostListResponse>> list(
            @Parameter(description = "검색 키워드 (제목, 내용)") @RequestParam(required = false) String keyword,
            @Parameter(description = "페이징 정보") @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(postService.list(keyword, pageable));
    }

    @Operation(summary = "게시글 상세 조회", description = "게시글 ID로 상세 정보 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> get(
            @Parameter(description = "게시글 ID", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(postService.get(id));
    }

    @Operation(summary = "게시글 작성", description = "게시글 작성 (파일 업로드 지원)")
    @ApiResponse(responseCode = "200", description = "작성 성공")
    @ApiResponse(responseCode = "400", description = "입력값 검증 실패", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Map<String, Long>> create(
            @Parameter(description = "게시글 정보 (JSON)", required = true) @RequestPart("post") @Valid PostRequest request,
            @Parameter(description = "첨부 파일 (최대 10개, 각 5MB)") @RequestPart(value = "files", required = false) List<MultipartFile> files) {

        Long currentUserId = 1L; // TODO: Security에서 가져오기

        List<PostFile> fileEntities = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            if (files.size() > 10) {
                throw new IllegalArgumentException("파일은 최대 10개까지 업로드 가능합니다");
            }
            for (MultipartFile file : files) {
                if (file.getSize() > 5 * 1024 * 1024L) {
                    throw new IllegalArgumentException("파일 크기는 5MB를 초과할 수 없습니다");
                }
                String url = fileStorageService.store(file);
                PostFile pf = PostFile.builder()
                        .fileUrl(url)
                        .originalName(file.getOriginalFilename())
                        .fileSize(file.getSize())
                        .mimeType(file.getContentType())
                        .build();
                fileEntities.add(pf);
            }
        }

        Long postId = postService.create(currentUserId, request, fileEntities);
        return ResponseEntity.ok(Map.of("id", postId));
    }

    @Operation(summary = "게시글 수정", description = "게시글 제목 및 내용 수정")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @ApiResponse(responseCode = "403", description = "수정 권한 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @Parameter(description = "게시글 ID", required = true) @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "수정할 게시글 정보") @RequestBody @Valid PostRequest request) {
        Long currentUserId = 1L; // TODO: Security
        postService.update(id, currentUserId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "게시글 삭제", description = "게시글 소프트 삭제 (is_deleted = true)")
    @ApiResponse(responseCode = "200", description = "삭제 성공")
    @ApiResponse(responseCode = "403", description = "삭제 권한 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "게시글 ID", required = true) @PathVariable Long id) {
        Long currentUserId = 1L; // TODO: Security
        postService.delete(id, currentUserId);
        return ResponseEntity.ok().build();
    }
}
