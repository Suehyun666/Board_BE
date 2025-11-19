# 데이터베이스 스키마 설계

## 1. ERD (Entity Relationship Diagram)

```
┌─────────────────────┐
│       users         │
├─────────────────────┤
│ PK  id              │
│     username        │◄────┐
│     password        │     │
│     nickname        │     │
│     role            │     │
│     created_at      │     │
│     updated_at      │     │
└─────────────────────┘     │
         │                  │
         │ 1                │ 1
         │                  │
         │                  │
         │ N                │ N
         ▼                  │
┌─────────────────────┐     │
│       posts         │     │
├─────────────────────┤     │
│ PK  id              │     │
│     title           │     │
│     content         │     │
│ FK  author_id       │─────┘
│     view_count      │
│     like_count      │
│     is_deleted      │
│     created_at      │
│     updated_at      │
└─────────────────────┘
         │
         │ 1
         │
         ├──────────────────┐
         │                  │
         │ N                │ N
         ▼                  ▼
┌─────────────────────┐  ┌─────────────────────┐
│     comments        │  │    post_files       │
├─────────────────────┤  ├─────────────────────┤
│ PK  id              │  │ PK  id              │
│ FK  post_id         │  │ FK  post_id         │
│ FK  author_id       │  │     file_url        │
│     content         │  │     original_name   │
│ FK  parent_id (자기참조)│  │     file_size       │
│     is_deleted      │  │     mime_type       │
│     created_at      │  │     created_at      │
└─────────────────────┘  └─────────────────────┘

┌─────────────────────┐
│    post_likes       │  (선택 사항)
├─────────────────────┤
│ PK  post_id         │
│ PK  user_id         │
│     created_at      │
└─────────────────────┘
```

## 2. 테이블 스키마

### 2.1 users (사용자)

사용자 정보를 저장하는 테이블

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGSERIAL | PRIMARY KEY | 사용자 고유 ID |
| username | VARCHAR(50) | NOT NULL, UNIQUE | 로그인 아이디 |
| password | VARCHAR(255) | NOT NULL | 암호화된 비밀번호 (BCrypt) |
| nickname | VARCHAR(50) | NOT NULL | 사용자 닉네임 |
| role | VARCHAR(20) | NOT NULL, DEFAULT 'USER' | 권한 (USER, ADMIN) |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | 계정 생성일 |
| updated_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | 정보 수정일 |

**DDL:**
```sql
CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    nickname    VARCHAR(50)  NOT NULL,
    role        VARCHAR(20)  NOT NULL DEFAULT 'USER',
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- 인덱스
CREATE UNIQUE INDEX idx_users_username ON users(username);
```

### 2.2 posts (게시글)

게시글 정보를 저장하는 테이블

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGSERIAL | PRIMARY KEY | 게시글 고유 ID |
| title | VARCHAR(150) | NOT NULL | 게시글 제목 (최대 150자) |
| content | TEXT | NOT NULL | 게시글 본문 (최대 10,000자) |
| author_id | BIGINT | NOT NULL, FK → users(id) | 작성자 ID |
| view_count | BIGINT | NOT NULL, DEFAULT 0 | 조회수 |
| like_count | BIGINT | NOT NULL, DEFAULT 0 | 좋아요 수 |
| is_deleted | BOOLEAN | NOT NULL, DEFAULT FALSE | 삭제 여부 (소프트 삭제) |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | 작성일 |
| updated_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | 수정일 |

**DDL:**
```sql
CREATE TABLE posts (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(150) NOT NULL,
    content     TEXT         NOT NULL,
    author_id   BIGINT       NOT NULL REFERENCES users(id),
    view_count  BIGINT       NOT NULL DEFAULT 0,
    like_count  BIGINT       NOT NULL DEFAULT 0,
    is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- 인덱스
CREATE INDEX idx_posts_created_at ON posts(created_at DESC);
CREATE INDEX idx_posts_title ON posts(title);
CREATE INDEX idx_posts_author_id ON posts(author_id);
CREATE INDEX idx_posts_is_deleted ON posts(is_deleted) WHERE is_deleted = FALSE;
```

### 2.3 comments (댓글)

댓글 정보를 저장하는 테이블 (대댓글 지원)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGSERIAL | PRIMARY KEY | 댓글 고유 ID |
| post_id | BIGINT | NOT NULL, FK → posts(id) | 게시글 ID |
| author_id | BIGINT | NOT NULL, FK → users(id) | 작성자 ID |
| content | VARCHAR(1000) | NOT NULL | 댓글 내용 (최대 1,000자) |
| parent_id | BIGINT | NULL, FK → comments(id) | 부모 댓글 ID (대댓글인 경우) |
| is_deleted | BOOLEAN | NOT NULL, DEFAULT FALSE | 삭제 여부 |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | 작성일 |

**DDL:**
```sql
CREATE TABLE comments (
    id          BIGSERIAL PRIMARY KEY,
    post_id     BIGINT       NOT NULL REFERENCES posts(id),
    author_id   BIGINT       NOT NULL REFERENCES users(id),
    content     VARCHAR(1000) NOT NULL,
    parent_id   BIGINT       NULL REFERENCES comments(id),
    is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- 인덱스
CREATE INDEX idx_comments_post_id_created_at ON comments(post_id, created_at);
CREATE INDEX idx_comments_parent_id ON comments(parent_id);
```

### 2.4 post_files (첨부 파일)

게시글 첨부 파일 정보를 저장하는 테이블

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGSERIAL | PRIMARY KEY | 파일 고유 ID |
| post_id | BIGINT | NOT NULL, FK → posts(id) ON DELETE CASCADE | 게시글 ID |
| file_url | TEXT | NOT NULL | 파일 저장 경로/URL |
| original_name | VARCHAR(255) | NOT NULL | 원본 파일명 |
| file_size | BIGINT | NOT NULL | 파일 크기 (bytes) |
| mime_type | VARCHAR(50) | NOT NULL | MIME 타입 (image/jpeg 등) |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | 업로드일 |

**DDL:**
```sql
CREATE TABLE post_files (
    id           BIGSERIAL PRIMARY KEY,
    post_id      BIGINT       NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    file_url     TEXT         NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    file_size    BIGINT       NOT NULL,
    mime_type    VARCHAR(50)  NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- 인덱스
CREATE INDEX idx_post_files_post_id ON post_files(post_id);
```

### 2.5 post_likes (게시글 좋아요) - 선택 사항

사용자의 게시글 좋아요를 관리하는 테이블

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| post_id | BIGINT | PK, FK → posts(id) ON DELETE CASCADE | 게시글 ID |
| user_id | BIGINT | PK, FK → users(id) ON DELETE CASCADE | 사용자 ID |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | 좋아요 생성일 |

**DDL:**
```sql
CREATE TABLE post_likes (
    post_id BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (post_id, user_id)
);

-- 인덱스
CREATE INDEX idx_post_likes_user_id ON post_likes(user_id);
```

## 3. 제약조건 및 비즈니스 규칙

### 3.1 데이터 제약사항

| 항목 | 제약 | 검증 위치 |
|------|------|-----------|
| 게시글 제목 | 최대 150자 | DB + Application |
| 게시글 본문 | 최대 10,000자 | Application |
| 댓글 내용 | 최대 1,000자 | DB + Application |
| 첨부 파일 개수 | 게시글당 최대 10개 | Application |
| 첨부 파일 크기 | 파일당 최대 5MB | Application |
| 허용 파일 형식 | image/*, application/pdf | Application |

### 3.2 참조 무결성

- **posts.author_id** → users.id
  - 사용자 삭제 시 게시글 처리: 소프트 삭제 또는 CASCADE

- **comments.post_id** → posts.id
  - 게시글 삭제 시 댓글 처리: CASCADE 또는 소프트 삭제

- **comments.author_id** → users.id
  - 사용자 삭제 시 댓글 처리: 소프트 삭제

- **post_files.post_id** → posts.id
  - 게시글 삭제 시 파일 처리: ON DELETE CASCADE

### 3.3 소프트 삭제 정책

- **posts.is_deleted**: TRUE로 설정하여 논리적 삭제
- **comments.is_deleted**: TRUE로 설정하여 논리적 삭제
- 삭제된 항목은 조회 쿼리에서 필터링 (WHERE is_deleted = FALSE)

### 3.4 타임스탬프 정책

- **created_at**: INSERT 시점에 자동 설정 (DEFAULT now())
- **updated_at**: UPDATE 시점에 JPA @PreUpdate로 갱신

## 4. 인덱스 전략

### 4.1 Primary Key 인덱스
- 모든 테이블의 id 컬럼 (자동 생성)

### 4.2 Foreign Key 인덱스
- posts.author_id
- comments.post_id, comments.author_id
- post_files.post_id

### 4.3 검색 최적화 인덱스
- posts.created_at (DESC) - 최신순 정렬
- posts.title - 제목 검색
- posts.is_deleted - 삭제되지 않은 게시글 필터링

### 4.4 복합 인덱스
- comments (post_id, created_at) - 특정 게시글의 댓글 조회

## 5. 샘플 데이터

```sql
-- 사용자 샘플
INSERT INTO users (username, password, nickname, role) VALUES
('admin', '$2a$10$...', '관리자', 'ADMIN'),
('user01', '$2a$10$...', '사용자1', 'USER');

-- 게시글 샘플
INSERT INTO posts (title, content, author_id) VALUES
('첫 번째 게시글', '게시글 내용입니다.', 1),
('공지사항', '공지사항 내용입니다.', 1);

-- 댓글 샘플
INSERT INTO comments (post_id, author_id, content) VALUES
(1, 2, '좋은 게시글이네요!'),
(1, 1, '감사합니다!');
```

## 6. 마이그레이션 전략

### 6.1 초기 스키마 생성
- Flyway 또는 Liquibase 사용 권장
- Version: V1__init_schema.sql

### 6.2 스키마 변경 관리
- 마이그레이션 파일로 버전 관리
- 롤백 스크립트 함께 관리

### 6.3 운영 환경 배포
- Blue-Green 배포 시 스키마 호환성 유지
- 역방향 호환 가능한 변경 우선 적용

## 7. 성능 고려사항

### 7.1 Connection Pool 설정
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
```

### 7.2 쿼리 최적화
- N+1 문제 방지: Fetch Join 사용
- 페이징 쿼리 최적화: COUNT 쿼리 분리

### 7.3 파티셔닝 전략 (향후 확장)
- posts 테이블: created_at 기준 월별 파티셔닝
- 대용량 데이터 처리 시 적용
