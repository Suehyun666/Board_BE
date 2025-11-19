# 백엔드 API 구조

## 📁 컨트롤러 구조

```
web/controller/
 ├─ PostController.java      # 게시판 API (/boards)
 ├─ CommentController.java   # 댓글 API (/boards/{postId}/comments)
 └─ GlobalExceptionHandler.java  # 전역 예외 처리
```

---

## 🌐 API 엔드포인트

### 게시판 API (`/boards`)

| 메서드 | URL | 설명 | 파라미터 |
|--------|-----|------|----------|
| GET | `/boards` | 목록 조회 | `page`, `size`, `keyword` |
| GET | `/boards/{id}` | 상세 조회 | - |
| POST | `/boards` | 작성 | `multipart/form-data` |
| PUT | `/boards/{id}` | 수정 | JSON |
| DELETE | `/boards/{id}` | 삭제 | - |

#### GET /boards (목록 조회)
```
GET /boards?page=0&size=20&keyword=검색어
```

**응답 예시**:
```json
{
  "content": [
    {
      "id": 1,
      "title": "제목",
      "authorNickname": "작성자",
      "viewCount": 10,
      "likeCount": 5,
      "commentCount": 3,
      "createdAt": "2025-11-19T12:00:00Z"
    }
  ],
  "pageable": {...},
  "totalPages": 5,
  "totalElements": 100,
  "number": 0,
  "size": 20
}
```

#### POST /boards (작성)
```
POST /boards
Content-Type: multipart/form-data

post: {
  "title": "제목",
  "content": "내용"
}
files: [파일1, 파일2, ...]
```

**응답 예시**:
```json
{
  "id": 123
}
```

---

### 댓글 API (`/boards/{postId}/comments`)

| 메서드 | URL | 설명 |
|--------|-----|------|
| GET | `/boards/{postId}/comments` | 댓글 목록 |
| POST | `/boards/{postId}/comments` | 댓글 작성 |
| PUT | `/boards/{postId}/comments/{id}` | 댓글 수정 |
| DELETE | `/boards/{postId}/comments/{id}` | 댓글 삭제 |

#### GET /boards/{postId}/comments
```
GET /boards/1/comments
```

**응답 예시**:
```json
[
  {
    "id": 1,
    "content": "댓글 내용",
    "authorNickname": "작성자",
    "createdAt": "2025-11-19T12:00:00Z",
    "isDeleted": false
  }
]
```

#### POST /boards/{postId}/comments
```
POST /boards/1/comments
Content-Type: application/json

{
  "content": "댓글 내용",
  "parentId": null  // 대댓글인 경우 부모 댓글 ID
}
```

**응답 예시**:
```json
{
  "id": 456
}
```

---

## 🔧 주요 변경 사항

### URL 변경
- ❌ 이전: `/posts`, `/posts/{postId}/comments`
- ✅ 현재: `/boards`, `/boards/{postId}/comments`

### 이유
1. **일관성**: 리소스 이름을 복수형으로 통일
2. **RESTful**: 계층 구조 명확화
3. **가독성**: `boards`가 더 직관적

---

## 🎯 REST 설계 원칙

### ✅ 올바른 설계
```
GET    /boards              # 목록
GET    /boards/{id}         # 상세
POST   /boards              # 작성
PUT    /boards/{id}         # 수정
DELETE /boards/{id}         # 삭제

GET    /boards/{postId}/comments           # 댓글 목록
POST   /boards/{postId}/comments           # 댓글 작성
PUT    /boards/{postId}/comments/{id}      # 댓글 수정
DELETE /boards/{postId}/comments/{id}      # 댓글 삭제
```

### ❌ 잘못된 설계
```
GET /getBoards              # 동사 사용 X
POST /board                 # 단수형 X
PUT /comments/{id}          # 계층 구조 X
DELETE /deleteComment/{id}  # 동사 + 단수형 X
```

---

## 📝 에러 응답

모든 에러는 `GlobalExceptionHandler`에서 처리:

```json
{
  "code": "NOT_FOUND",
  "message": "게시글을 찾을 수 없습니다",
  "timestamp": "2025-11-19T12:00:00Z"
}
```

### 주요 에러 코드
- `NOT_FOUND` (404): 리소스를 찾을 수 없음
- `METHOD_NOT_ALLOWED` (405): 지원하지 않는 HTTP 메서드
- `BAD_REQUEST` (400): 입력값 검증 실패
- `FORBIDDEN` (403): 권한 없음
- `INTERNAL_SERVER_ERROR` (500): 서버 내부 오류

---

## 🔍 Swagger/OpenAPI

API 문서는 Swagger UI에서 확인:
```
http://localhost:8080/swagger-ui.html
```

또는 Redoc:
```
http://localhost:8080/redoc.html
```

---

## 🚀 다음 단계

### 추가 예정
- [ ] 사용자 인증/인가 (JWT)
- [ ] 좋아요 기능
- [ ] 파일 다운로드 API
- [ ] 알림 기능

### 개선 예정
- [ ] 페이지네이션 응답 표준화 (PagedModel)
- [ ] API 버저닝 (v1, v2)
- [ ] Rate Limiting
