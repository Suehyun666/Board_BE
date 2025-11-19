# Board API - 게시판 CRUD 시스템

Spring Boot 기반 게시판 API with Kubernetes 배포

## 기술 스택

- **Backend**: Spring Boot 3.5.7, Java 21
- **Database**: PostgreSQL 17
- **ORM**: Spring Data JPA
- **API 문서**: SpringDoc OpenAPI 3 (Swagger UI + Redoc)
- **Container**: Docker, Docker Compose
- **Orchestration**: Kubernetes
- **Monitoring**: Prometheus + Grafana

## 주요 기능

- 게시글 CRUD (작성, 조회, 수정, 삭제)
- 댓글 및 대댓글
- 페이징 및 검색
- 파일 업로드 (최대 10개, 각 5MB)
- API 문서 자동 생성

## 로컬 개발 환경

### 1. Docker Compose로 실행 (권장)

```bash
# PostgreSQL + Backend 한번에 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f backend

# 중지
docker-compose down
```

### 2. 개별 실행

```bash
# PostgreSQL만 실행
docker-compose up -d postgres

# Backend 로컬 실행
./gradlew bootRun
```

## API 접속

서버 실행 후:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Redoc**: http://localhost:8080/redoc.html
- **Actuator**: http://localhost:8080/actuator/health
- **Prometheus**: http://localhost:8080/actuator/prometheus

## 초기 계정

| Username | Password | Role |
|----------|----------|------|
| admin | password123 | ADMIN |
| user1 | password123 | USER |
| user2 | password123 | USER |

## 프로젝트 구조

```
Board_BE/
├── src/
│   └── main/
│       ├── java/org/board/board_be/
│       │   ├── domain/         # 엔티티, 리포지토리
│       │   ├── service/        # 비즈니스 로직
│       │   ├── web/            # 컨트롤러, DTO
│       │   └── config/         # 설정
│       └── resources/
│           ├── application.yml
│           └── data.sql        # 초기 데이터
├── k8s/                        # Kubernetes 매니페스트
│   ├── secret.example.yml
│   ├── postgres.yml
│   ├── deployment.yml
│   └── README.md
├── init-db/                    # PostgreSQL 초기화
│   └── init.sql
├── Dockerfile
├── docker-compose.yml
└── docs/                       # 프로젝트 문서
```

## Kubernetes 배포

자세한 내용은 [k8s/README.md](k8s/README.md) 참고

### 빠른 시작

```bash
# 1. Secret 생성
cp k8s/secret.example.yml k8s/secret.yml
vi k8s/secret.yml  # 실제 비밀번호 입력

# 2. 배포
kubectl apply -f k8s/secret.yml
kubectl apply -f k8s/postgres.yml
kubectl apply -f k8s/deployment.yml

# 3. 확인
kubectl get pods
```

## 환경 변수

| 변수 | 설명 | 기본값 |
|------|------|--------|
| DB_URL | PostgreSQL URL | jdbc:postgresql://localhost:5432/board |
| DB_USERNAME | DB 사용자 | postgres |
| DB_PASSWORD | DB 비밀번호 | postgres |
| DB_POOL_SIZE | 커넥션 풀 크기 | 10 |

## 빌드

```bash
# 테스트 제외 빌드
./gradlew clean build -x test

# Docker 이미지 빌드
docker build -t board-backend:latest .
```

## 개발

### API 추가 시
1. Entity 작성 (`domain/`)
2. Repository 작성
3. Service 작성 (`service/`)
4. DTO 작성 (`web/dto/`)
5. Controller 작성 (`web/controller/`)
6. OpenAPI 어노테이션 추가

### 로그 레벨 변경
`application.yml`에서 설정:
```yaml
logging:
  level:
    org.board: DEBUG
```

## 문제 해결

### PostgreSQL 연결 실패
```bash
# 컨테이너 로그 확인
docker-compose logs postgres

# DB 직접 접속
docker exec -it board-postgres psql -U board_user -d board
```

### Backend 실행 실패
```bash
# 빌드 캐시 삭제
./gradlew clean

# 의존성 재다운로드
./gradlew build --refresh-dependencies
```

## 라이선스

MIT
