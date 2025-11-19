package org.board.board_be.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.board.board_be.service.UserService;
import org.board.board_be.web.dto.ApiResult;
import org.board.board_be.web.dto.UserRequest;
import org.board.board_be.web.dto.UserResponse;
import org.board.board_be.web.exception.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "사용자", description = "회원가입 / 로그인 / 사용자 정보 API")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponse(responseCode = "200", description = "회원가입 성공")
    @ApiResponse(responseCode = "400", description = "입력값 검증 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/register")
    public ResponseEntity<ApiResult<Map<String, Long>>> register(
            @RequestBody @Valid UserRequest request) {

        Long userId = userService.register(request);

        return ResponseEntity.ok(
                ApiResult.<Map<String, Long>>builder()
                        .success(true)
                        .data(Map.of("id", userId))
                        .build()
        );
    }

    @Operation(summary = "로그인", description = "사용자 로그인 / 회원 정보 반환")
    @ApiResponse(responseCode = "200", description = "로그인 성공")
    @ApiResponse(responseCode = "400", description = "로그인 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/login")
    public ResponseEntity<ApiResult<UserResponse>> login(
            @RequestBody @Valid UserRequest.LoginRequest request) {

        UserResponse response = userService.login(request);

        return ResponseEntity.ok(
                ApiResult.<UserResponse>builder()
                        .success(true)
                        .data(response)
                        .build()
        );
    }

    @Operation(summary = "내 정보 조회", description = "사용자 ID로 내 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{id}")
    public ResponseEntity<ApiResult<UserResponse>> getMyInfo(
            @PathVariable Long id) {

        UserResponse response = userService.getMyInfo(id);

        return ResponseEntity.ok(
                ApiResult.<UserResponse>builder()
                        .success(true)
                        .data(response)
                        .build()
        );
    }

    @Operation(summary = "회원 탈퇴", description = "사용자 계정을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "삭제 성공")
    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResult<Void>> delete(
            @PathVariable Long id) {

        userService.delete(id);

        return ResponseEntity.ok(
                ApiResult.<Void>builder()
                        .success(true)
                        .data(null)
                        .build()
        );
    }
}
