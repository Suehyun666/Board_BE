-- PostgreSQL 초기화 스크립트
-- 이 파일은 PostgreSQL 컨테이너 최초 실행 시 자동으로 실행됩니다

-- 데이터베이스는 환경변수(POSTGRES_DB)로 자동 생성되므로 별도 생성 불필요
-- 유저 생성
CREATE USER board_user WITH PASSWORD 'board_pass';

-- DB 생성 (OWNER 지정)
CREATE DATABASE board OWNER board_user;

-- 권한 설정 (이미 POSTGRES_USER로 생성된 사용자에게 모든 권한 부여)
GRANT ALL PRIVILEGES ON DATABASE board TO board_user;

-- 확장 기능 활성화 (필요 시)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";  -- 텍스트 검색 성능 향상

-- 샘플 데이터는 Spring Boot의 data.sql에서 처리하므로 여기서는 생략
