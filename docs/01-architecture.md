# 시스템 아키텍처 설계

## 1. 전체 시스템 구성도

```
┌─────────────────────────────────────────────────────────────────┐
│                          외부 사용자                              │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                       Kubernetes Cluster                         │
│                                                                   │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │                      Ingress Controller                     │ │
│  │                    (NGINX Ingress)                          │ │
│  └─────────────┬──────────────────────────┬───────────────────┘ │
│                │                          │                      │
│                ▼                          ▼                      │
│  ┌──────────────────────┐   ┌──────────────────────────┐       │
│  │   Frontend Service   │   │    Backend Service       │       │
│  │   (React/Vue)        │   │    (Spring Boot)         │       │
│  │                      │   │                          │       │
│  │  ┌────────────────┐ │   │  ┌────────────────────┐ │       │
│  │  │   Deployment   │ │   │  │    Deployment      │ │       │
│  │  │   Replicas: 2  │ │   │  │    Replicas: 3     │ │       │
│  │  │   Port: 80     │ │   │  │    Port: 8080      │ │       │
│  │  └────────────────┘ │   │  └────────────────────┘ │       │
│  └──────────────────────┘   │           │              │       │
│                              │           ▼              │       │
│                              │  ┌────────────────────┐ │       │
│                              │  │  ConfigMap/Secret  │ │       │
│                              │  └────────────────────┘ │       │
│                              └──────────────────────────┘       │
│                                         │                        │
│                                         ▼                        │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              PostgreSQL StatefulSet                       │  │
│  │              (Persistent Volume Claim)                    │  │
│  │              Port: 5432                                   │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                  Monitoring Stack                         │  │
│  │                                                            │  │
│  │  ┌──────────────┐        ┌──────────────┐               │  │
│  │  │  Prometheus  │───────▶│   Grafana    │               │  │
│  │  │  (Metrics)   │        │  (Dashboard) │               │  │
│  │  └──────────────┘        └──────────────┘               │  │
│  └──────────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                         CI/CD Pipeline                           │
│                                                                   │
│  GitHub ──▶ GitHub Actions ──▶ Docker Build ──▶ Push to Registry│
│                     │                                             │
│                     └──────────▶ ArgoCD ──▶ K8s Deploy          │
└───────────────────────────────────────────────────────────────────┘
```

## 2. 기술 스택

### 2.1 Backend
| 기술 | 버전 | 선정 이유 |
|------|------|-----------|
| Java | 17 | LTS 버전, 안정성 및 성능 |
| Spring Boot | 3.2.x | 엔터프라이즈급 웹 애플리케이션 프레임워크 |
| Spring Data JPA | 3.2.x | ORM 기반 데이터 접근 계층 추상화 |
| PostgreSQL | 15 | ACID 지원, 확장성 우수 |
| Gradle | 8.x | 빌드 도구, Kotlin DSL 지원 |

### 2.2 Frontend
| 기술 | 버전 | 선정 이유 |
|------|------|-----------|
| React / Vue.js | 18.x / 3.x | 컴포넌트 기반 UI 라이브러리 |
| TypeScript | 5.x | 타입 안정성 |
| Axios | latest | HTTP 클라이언트 |
| Vite | latest | 빠른 개발 서버 및 빌드 |

### 2.3 Infrastructure
| 기술 | 버전 | 선정 이유 |
|------|------|-----------|
| Docker | 24.x | 컨테이너화 |
| Kubernetes | 1.28+ | 컨테이너 오케스트레이션 |
| NGINX Ingress | latest | 로드 밸런싱 및 라우팅 |

### 2.4 CI/CD
| 기술 | 버전 | 선정 이유 |
|------|------|-----------|
| GitHub Actions | - | 소스 코드와 통합된 CI/CD |
| ArgoCD | 2.9+ | GitOps 기반 CD, K8s 네이티브 |
| Docker Hub | - | 컨테이너 이미지 레지스트리 |

### 2.5 Monitoring
| 기술 | 버전 | 선정 이유 |
|------|------|-----------|
| Prometheus | 2.48+ | 메트릭 수집 및 저장 |
| Grafana | 10.x | 시각화 대시보드 |
| Spring Actuator | 3.2.x | 애플리케이션 메트릭 노출 |

## 3. Backend 계층 구조

```
src/main/java/com/example/board/
├── BoardApplication.java          # Spring Boot 진입점
├── domain/                        # 도메인 계층
│   ├── user/
│   │   ├── User.java             # 사용자 엔티티
│   │   └── UserRepository.java
│   ├── post/
│   │   ├── Post.java             # 게시글 엔티티
│   │   ├── PostFile.java         # 첨부 파일 엔티티
│   │   └── PostRepository.java
│   └── comment/
│       ├── Comment.java          # 댓글 엔티티
│       └── CommentRepository.java
├── service/                       # 서비스 계층
│   ├── PostService.java          # 게시글 비즈니스 로직
│   ├── CommentService.java
│   └── FileStorageService.java
└── web/                          # 웹 계층
    ├── controller/
    │   ├── PostController.java   # REST API 컨트롤러
    │   └── CommentController.java
    ├── dto/
    │   ├── PostRequest.java
    │   ├── PostResponse.java
    │   └── CommentRequest.java
    └── exception/
        └── GlobalExceptionHandler.java
```

### 3.1 계층별 역할

#### Domain 계층
- JPA 엔티티 정의
- Repository 인터페이스 (Spring Data JPA)
- 도메인 비즈니스 규칙 포함

#### Service 계층
- 비즈니스 로직 구현
- 트랜잭션 경계 설정
- 도메인 객체 조작

#### Web 계층
- HTTP 요청/응답 처리
- DTO 변환
- 입력 검증 (Validation)
- 예외 처리

## 4. Frontend 구조

```
frontend/
├── src/
│   ├── components/          # 재사용 가능한 컴포넌트
│   │   ├── PostList.tsx
│   │   ├── PostForm.tsx
│   │   └── CommentList.tsx
│   ├── pages/              # 페이지 컴포넌트
│   │   ├── HomePage.tsx
│   │   ├── PostDetailPage.tsx
│   │   └── PostEditPage.tsx
│   ├── api/                # API 호출 함수
│   │   └── postApi.ts
│   ├── store/              # 상태 관리 (Zustand/Redux)
│   │   └── postStore.ts
│   └── App.tsx
├── public/
└── package.json
```

## 5. 데이터 흐름

### 5.1 게시글 조회 흐름
```
User → Frontend → Ingress → Backend Service → PostController
                                                      ↓
                                                PostService
                                                      ↓
                                              PostRepository
                                                      ↓
                                                 PostgreSQL
```

### 5.2 게시글 작성 흐름
```
User → Frontend (파일 업로드) → Backend API
                                    ↓
                           FileStorageService (파일 저장)
                                    ↓
                           PostService (DB 저장)
                                    ↓
                           PostRepository → PostgreSQL
```

## 6. 보안 고려사항

### 6.1 인증/인가
- JWT 기반 토큰 인증 (향후 확장)
- Spring Security 적용
- CORS 설정

### 6.2 데이터 보호
- 비밀번호 암호화 (BCrypt)
- SQL Injection 방지 (JPA Prepared Statement)
- XSS 방지 (입력값 검증)

### 6.3 네트워크 보안
- HTTPS 적용 (Ingress TLS)
- Kubernetes Secret을 통한 민감 정보 관리

## 7. 확장성 고려사항

### 7.1 수평 확장
- Stateless 애플리케이션 설계
- Kubernetes HPA (Horizontal Pod Autoscaler) 적용 가능

### 7.2 데이터베이스 확장
- Read Replica 구성 가능
- Connection Pool 설정 (HikariCP)

### 7.3 캐싱 전략 (향후 확장)
- Redis 도입 가능
- Spring Cache Abstraction 활용

## 8. 설계 원칙

### 8.1 SOLID 원칙 준수
- 단일 책임 원칙: 계층별 명확한 역할 분리
- 의존성 역전 원칙: 인터페이스 기반 설계

### 8.2 RESTful API 설계
- 리소스 기반 URL 설계
- HTTP 메서드 적절한 사용
- 적절한 HTTP 상태 코드 반환

### 8.3 12-Factor App
- 설정은 환경 변수로 관리
- 로그는 표준 출력으로
- 무상태 프로세스
