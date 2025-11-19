# 게시판 시스템 문서

## 문서 개요

이 문서는 Kubernetes 기반 게시판 CRUD 시스템의 설계 및 구현을 위한 기술 문서입니다.

## 문서 구조

### 01. [아키텍처 설계](./01-architecture.md)
- 시스템 전체 구성도
- 기술 스택 및 선정 이유
- 계층별 역할 정의

### 02. [데이터베이스 스키마](./02-database-schema.md)
- ERD (Entity Relationship Diagram)
- 테이블 스키마 정의
- 제약조건 및 인덱스 설계

### 03. [API 명세](./03-api-specification.md)
- RESTful API 엔드포인트
- 요청/응답 형식
- 에러 코드 정의

### 04. [CI/CD 및 배포](./04-cicd-deployment.md)
- GitHub Actions 워크플로우
- ArgoCD GitOps 배포 전략
- PostgreSQL 배포 옵션 (StatefulSet vs 관리형 DB)
- init-db 스크립트 실행 방법
- 배포 프로세스 및 트러블슈팅

### 05. [모니터링 구성](./05-monitoring.md) (예정)
- Prometheus 메트릭 수집
- Grafana 대시보드 설계
- 알림 설정

## 프로젝트 정보

- **프로젝트명**: Board CRUD System
- **개발 환경**: Spring Boot (Backend) + React/Vue (Frontend)
- **배포 환경**: Kubernetes
- **데이터베이스**: PostgreSQL
- **버전 관리**: Git
- **CI/CD**: GitHub Actions + ArgoCD
- **모니터링**: Prometheus + Grafana

## 빠른 시작

```bash
# Backend 실행
cd backend
./gradlew bootRun

# Frontend 실행
cd frontend
npm install
npm run dev

# Docker Compose로 전체 실행
docker-compose up -d

# Kubernetes 배포
kubectl apply -f k8s/
```

## 작성일

2025-11-19
