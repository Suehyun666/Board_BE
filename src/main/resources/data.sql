-- 초기 사용자 데이터 (비밀번호: password123)
-- BCrypt로 암호화된 비밀번호: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

INSERT INTO users (username, password, nickname, role, created_at, updated_at)
VALUES
    ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '관리자', 'ADMIN', NOW(), NOW()),
    ('user1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '사용자1', 'USER', NOW(), NOW()),
    ('user2', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '사용자2', 'USER', NOW(), NOW())
ON CONFLICT (username) DO NOTHING;

-- 샘플 게시글
INSERT INTO posts (title, content, author_id, view_count, like_count, is_deleted, created_at, updated_at)
VALUES
    ('첫 번째 게시글', '게시글 내용입니다. 환영합니다!', 1, 0, 0, FALSE, NOW(), NOW()),
    ('[공지] 게시판 이용 안내', '게시판 이용 규칙입니다.', 1, 0, 0, FALSE, NOW(), NOW())
ON CONFLICT DO NOTHING;
