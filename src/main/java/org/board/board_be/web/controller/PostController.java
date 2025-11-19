package org.board.board_be.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.board.board_be.domain.post.PostFile;
import org.board.board_be.service.FileStorageService;
import org.board.board_be.service.PostService;
import org.board.board_be.web.dto.ApiResult;
import org.board.board_be.web.dto.PostListResponse;
import org.board.board_be.web.dto.PostRequest;
import org.board.board_be.web.dto.PostResponse;
import org.board.board_be.web.exception.ErrorResponse;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Tag(name = "게시판", description = "게시판 CRUD API")
@RestController
@RequestMapping("/boards")
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
    public ResponseEntity<ApiResult<Page<PostListResponse>>> list(
            @Parameter(description = "검색 키워드 (제목, 내용)") @RequestParam(required = false) String keyword,
            @Parameter(description = "페이징 정보") @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(
                ApiResult.<Page<PostListResponse>>builder()
                        .success(true)
                        .data(postService.list(keyword, pageable))
                        .build()
        );
    }

    @Operation(summary = "게시글 상세 조회", description = "게시글 ID로 상세 정보 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{id}")
    public ResponseEntity<ApiResult<PostResponse>> get(
            @Parameter(description = "게시글 ID", required = true) @PathVariable Long id,
            @Parameter(description = "현재 사용자 ID (로그인한 경우)", required = false) @RequestParam(required = false) Long userId) {

        return ResponseEntity.ok(
                ApiResult.<PostResponse>builder()
                        .success(true)
                        .data(postService.get(id, userId))
                        .build()
        );
    }

    @Operation(summary = "게시글 작성", description = "게시글 작성 (파일 업로드 지원)")
    @ApiResponse(responseCode = "200", description = "작성 성공")
    @ApiResponse(responseCode = "400", description = "입력값 검증 실패", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResult<Map<String, Long>>> create(
            @RequestParam Long userId,
            @Parameter(description = "게시글 정보 (JSON)", required = true)
            @RequestPart("post") @Valid PostRequest request,
            @Parameter(description = "첨부 파일 (최대 10개, 각 5MB)")
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {

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
                fileEntities.add(PostFile.builder()
                        .fileUrl(url)
                        .originalName(file.getOriginalFilename())
                        .fileSize(file.getSize())
                        .mimeType(file.getContentType())
                        .build());
            }
        }

        Long postId = postService.create(userId, request, fileEntities);

        return ResponseEntity.ok(
                ApiResult.<Map<String, Long>>builder()
                        .success(true)
                        .data(Map.of("id", postId))
                        .build()
        );
    }

    @Operation(summary = "게시글 수정", description = "게시글 제목 및 내용 수정")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @ApiResponse(responseCode = "403", description = "수정 권한 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PutMapping("/{id}")
    public ResponseEntity<ApiResult<Void>> update(
            @PathVariable Long id,
            @RequestParam Long userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "수정할 게시글 정보")
            @RequestBody @Valid PostRequest request) {

        postService.update(id, userId, request);

        return ResponseEntity.ok(
                ApiResult.<Void>builder()
                        .success(true)
                        .data(null)
                        .build()
        );
    }

    @Operation(summary = "게시글 삭제", description = "게시글 소프트 삭제 (is_deleted = true)")
    @ApiResponse(responseCode = "200", description = "삭제 성공")
    @ApiResponse(responseCode = "403", description = "삭제 권한 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResult<Void>> delete(
            @PathVariable Long id,
            @RequestParam Long userId) {

        postService.delete(id, userId);

        return ResponseEntity.ok(
                ApiResult.<Void>builder()
                        .success(true)
                        .data(null)
                        .build()
        );
    }

    @Operation(summary = "파일 다운로드", description = "업로드된 파일을 다운로드합니다")
    @ApiResponse(responseCode = "200", description = "다운로드 성공")
    @ApiResponse(responseCode = "404", description = "파일을 찾을 수 없음")
    @GetMapping("/files/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "파일명", required = true) @PathVariable String fileName,
            HttpServletRequest request) {

        Resource resource = fileStorageService.loadFileAsResource(fileName);

        // 파일의 Content-Type 결정
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            // Content-Type을 결정할 수 없으면 기본값 사용
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                // 브라우저 캐싱: 1년간 캐시 (이미지/파일은 변경되지 않으므로)
                .cacheControl(org.springframework.http.CacheControl.maxAge(365, java.util.concurrent.TimeUnit.DAYS))
                // 이미지는 인라인 표시, 다운로드 아님
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}