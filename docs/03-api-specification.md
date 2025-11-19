# API 명세서

## 1. API 설계 원칙

### 1.1 RESTful 설계 규칙
- 리소스 기반 URL 설계
- HTTP 메서드의 의미에 맞는 사용
- 적절한 HTTP 상태 코드 반환
- 일관된 응답 형식

### 1.2 Base URL
```
개발: http://localhost:8080/api
운영: https://api.yourdomain.com/api
```

### 1.3 공통 헤더
```
Content-Type: application/json
Accept: application/json
Authorization: Bearer {token}  (인증 필요 시)
```

## 2. 응답 형식

### 2.1 성공 응답
```json
{
  "data": { ... },
  "timestamp": "2025-11-19T10:30:00Z"
}
```

### 2.2 에러 응답
```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "에러 메시지",
    "details": [ ... ]
  },
  "timestamp": "2025-11-19T10:30:00Z"
}
```

### 2.3 페이징 응답
```json
{
  "data": {
    "content": [ ... ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20,
      "sort": { "sorted": true, "unsorted": false }
    },
    "totalElements": 100,
    "totalPages": 5,
    "last": false,
    "first": true
  },
  "timestamp": "2025-11-19T10:30:00Z"
}
```

## 3. 게시글 API

### 3.1 게시글 목록 조회

**Endpoint:** `GET /api/posts`

**Query Parameters:**
| 파라미터 | 타입 | 필수 | 설명 | 기본값 |
|----------|------|------|------|--------|
| page | integer | N | 페이지 번호 (0부터 시작) | 0 |
| size | integer | N | 페이지 크기 | 20 |
| sort | string | N | 정렬 기준 (createdAt,desc) | createdAt,desc |
| keyword | string | N | 검색 키워드 (제목, 내용) | - |

**Request Example:**
```http
GET /api/posts?page=0&size=20&keyword=공지
```

**Response: 200 OK**
```json
{
  "data": {
    "content": [
      {
        "id": 1,
        "title": "공지사항",
        "content": "게시글 내용...",
        "authorNickname": "관리자",
        "viewCount": 120,
        "likeCount": 15,
        "createdAt": "2025-11-19T10:00:00Z",
        "updatedAt": "2025-11-19T10:00:00Z",
        "fileUrls": [
          "https://storage.example.com/files/abc123.jpg"
        ]
      }
    ],
    "totalElements": 50,
    "totalPages": 3,
    "pageNumber": 0,
    "pageSize": 20
  }
}
```

---

### 3.2 게시글 상세 조회

**Endpoint:** `GET /api/posts/{id}`

**Path Parameters:**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| id | long | 게시글 ID |

**Request Example:**
```http
GET /api/posts/1
```

**Response: 200 OK**
```json
{
  "data": {
    "id": 1,
    "title": "게시글 제목",
    "content": "게시글 본문 내용...",
    "authorNickname": "작성자",
    "viewCount": 120,
    "likeCount": 15,
    "createdAt": "2025-11-19T10:00:00Z",
    "updatedAt": "2025-11-19T10:00:00Z",
    "files": [
      {
        "id": 1,
        "fileUrl": "https://storage.example.com/files/abc123.jpg",
        "originalName": "image.jpg",
        "fileSize": 1024000,
        "mimeType": "image/jpeg"
      }
    ]
  }
}
```

**Error Responses:**
- `404 Not Found`: 게시글을 찾을 수 없음

---

### 3.3 게시글 작성

**Endpoint:** `POST /api/posts`

**Content-Type:** `multipart/form-data`

**Request Parts:**
| 파트명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| post | JSON | Y | 게시글 정보 |
| files | File[] | N | 첨부 파일 (최대 10개, 각 5MB) |

**post JSON Schema:**
```json
{
  "title": "string (max 150)",
  "content": "string (max 10000)"
}
```

**Request Example:**
```http
POST /api/posts
Content-Type: multipart/form-data

--boundary
Content-Disposition: form-data; name="post"
Content-Type: application/json

{
  "title": "새 게시글",
  "content": "게시글 내용..."
}

--boundary
Content-Disposition: form-data; name="files"; filename="image.jpg"
Content-Type: image/jpeg

[binary data]
--boundary--
```

**Response: 200 OK**
```json
{
  "data": {
    "id": 123
  }
}
```

**Error Responses:**
- `400 Bad Request`: 유효성 검증 실패
  ```json
  {
    "error": {
      "code": "VALIDATION_ERROR",
      "message": "입력값 검증 실패",
      "details": [
        {
          "field": "title",
          "message": "제목은 필수입니다."
        }
      ]
    }
  }
  ```
- `413 Payload Too Large`: 파일 크기 초과

---

### 3.4 게시글 수정

**Endpoint:** `PUT /api/posts/{id}`

**Path Parameters:**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| id | long | 게시글 ID |

**Request Body:**
```json
{
  "title": "수정된 제목",
  "content": "수정된 내용"
}
```

**Response: 200 OK**
```json
{
  "data": {
    "message": "게시글이 수정되었습니다."
  }
}
```

**Error Responses:**
- `403 Forbidden`: 수정 권한 없음
- `404 Not Found`: 게시글을 찾을 수 없음

---

### 3.5 게시글 삭제

**Endpoint:** `DELETE /api/posts/{id}`

**Path Parameters:**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| id | long | 게시글 ID |

**Response: 200 OK**
```json
{
  "data": {
    "message": "게시글이 삭제되었습니다."
  }
}
```

**Error Responses:**
- `403 Forbidden`: 삭제 권한 없음
- `404 Not Found`: 게시글을 찾을 수 없음

---

## 4. 댓글 API

### 4.1 댓글 목록 조회

**Endpoint:** `GET /api/posts/{postId}/comments`

**Path Parameters:**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| postId | long | 게시글 ID |

**Response: 200 OK**
```json
{
  "data": [
    {
      "id": 1,
      "content": "댓글 내용",
      "authorNickname": "작성자",
      "parentId": null,
      "createdAt": "2025-11-19T11:00:00Z",
      "replies": [
        {
          "id": 2,
          "content": "대댓글 내용",
          "authorNickname": "다른 사용자",
          "parentId": 1,
          "createdAt": "2025-11-19T11:05:00Z"
        }
      ]
    }
  ]
}
```

---

### 4.2 댓글 작성

**Endpoint:** `POST /api/posts/{postId}/comments`

**Path Parameters:**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| postId | long | 게시글 ID |

**Request Body:**
```json
{
  "content": "댓글 내용",
  "parentId": null  // 대댓글인 경우 부모 댓글 ID
}
```

**Response: 200 OK**
```json
{
  "data": {
    "id": 10
  }
}
```

**Error Responses:**
- `400 Bad Request`: 유효성 검증 실패
- `404 Not Found`: 게시글을 찾을 수 없음

---

### 4.3 댓글 수정

**Endpoint:** `PUT /api/comments/{id}`

**Path Parameters:**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| id | long | 댓글 ID |

**Request Body:**
```json
{
  "content": "수정된 댓글 내용"
}
```

**Response: 200 OK**

---

### 4.4 댓글 삭제

**Endpoint:** `DELETE /api/comments/{id}`

**Path Parameters:**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| id | long | 댓글 ID |

**Response: 200 OK**

---

## 5. 사용자 API (선택 사항)

### 5.1 회원가입

**Endpoint:** `POST /api/users/register`

**Request Body:**
```json
{
  "username": "user01",
  "password": "password123!",
  "nickname": "사용자1"
}
```

**Response: 200 OK**
```json
{
  "data": {
    "id": 1,
    "username": "user01",
    "nickname": "사용자1"
  }
}
```

---

### 5.2 로그인

**Endpoint:** `POST /api/users/login`

**Request Body:**
```json
{
  "username": "user01",
  "password": "password123!"
}
```

**Response: 200 OK**
```json
{
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600
  }
}
```

---

## 6. HTTP 상태 코드

| 코드 | 설명 | 사용 시나리오 |
|------|------|---------------|
| 200 | OK | 성공적인 GET, PUT, DELETE |
| 201 | Created | 성공적인 POST (리소스 생성) |
| 400 | Bad Request | 유효성 검증 실패 |
| 401 | Unauthorized | 인증 실패 |
| 403 | Forbidden | 권한 없음 |
| 404 | Not Found | 리소스를 찾을 수 없음 |
| 409 | Conflict | 중복된 리소스 (username 등) |
| 413 | Payload Too Large | 파일 크기 초과 |
| 500 | Internal Server Error | 서버 내부 오류 |

## 7. 에러 코드

| 코드 | 메시지 | 설명 |
|------|--------|------|
| VALIDATION_ERROR | 입력값 검증 실패 | 요청 파라미터 유효성 검증 실패 |
| POST_NOT_FOUND | 게시글을 찾을 수 없음 | 존재하지 않는 게시글 |
| UNAUTHORIZED | 인증 필요 | 로그인이 필요한 요청 |
| FORBIDDEN | 권한 없음 | 작성자가 아닌 사용자의 수정/삭제 시도 |
| FILE_TOO_LARGE | 파일 크기 초과 | 5MB 초과 파일 업로드 |
| TOO_MANY_FILES | 파일 개수 초과 | 10개 초과 파일 업로드 |
| INVALID_FILE_TYPE | 지원하지 않는 파일 형식 | 허용되지 않은 MIME 타입 |

## 8. API 테스트 예제

### 8.1 cURL 예제

**게시글 목록 조회:**
```bash
curl -X GET "http://localhost:8080/api/posts?page=0&size=10" \
  -H "Accept: application/json"
```

**게시글 작성:**
```bash
curl -X POST "http://localhost:8080/api/posts" \
  -H "Authorization: Bearer {token}" \
  -F "post={\"title\":\"제목\",\"content\":\"내용\"};type=application/json" \
  -F "files=@/path/to/file.jpg"
```

**게시글 수정:**
```bash
curl -X PUT "http://localhost:8080/api/posts/1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{"title":"수정된 제목","content":"수정된 내용"}'
```

**게시글 삭제:**
```bash
curl -X DELETE "http://localhost:8080/api/posts/1" \
  -H "Authorization: Bearer {token}"
```

## 9. API 버전 관리

### 9.1 URL 버전 관리 (향후 확장)
```
/api/v1/posts
/api/v2/posts
```

### 9.2 헤더 버전 관리 (대안)
```
Accept: application/vnd.board.v1+json
```

## 10. Rate Limiting (향후 적용)

```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1700400000
```

- 기본: 100 requests/hour
- 인증된 사용자: 1000 requests/hour
