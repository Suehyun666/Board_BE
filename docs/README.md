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

### 04. [Docker 구성](./04-docker-configuration.md)
- Dockerfile 설계
- Multi-stage build 전략
- Docker Compose 구성

### 05. [Kubernetes 배포](./05-kubernetes-deployment.md)
- Deployment 매니페스트
- Service 구성
- ConfigMap/Secret 관리

### 06. [CI/CD 파이프라인](./06-cicd-pipeline.md)
- GitHub Actions 워크플로
- ArgoCD 연동 전략
- 배포 자동화 프로세스

### 07. [모니터링 구성](./07-monitoring.md)
- Prometheus 메트릭 수집
- Grafana 대시보드 설계
- 알림 설정

### 08. [요구사항 및 제한사항](./08-requirements.md)
- 프로젝트 요구사항 (70+15+15점 구성)
- 기능 제한사항
- 성능 목표

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
