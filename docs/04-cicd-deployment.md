# CI/CD 및 배포 가이드

## 목차
1. [GitHub Actions CI/CD 파이프라인](#github-actions-cicd-파이프라인)
2. [ArgoCD 설정 및 배포](#argocd-설정-및-배포)
3. [PostgreSQL 배포 전략](#postgresql-배포-전략)
4. [배포 프로세스](#배포-프로세스)

---

## GitHub Actions CI/CD 파이프라인

### 워크플로우 구조

`.github/workflows/ci-cd.yml` 파이프라인은 3개의 Job으로 구성됩니다:

#### 1. build-and-test
- **트리거**: 모든 push 및 PR
- **작업**:
  - 코드 체크아웃
  - JDK 21 설정
  - Gradle 빌드 및 테스트 실행
  - 빌드 아티팩트 업로드

#### 2. docker-build-push
- **트리거**: main 또는 develop 브랜치에 push 시
- **작업**:
  - Docker 이미지 빌드
  - GitHub Container Registry(GHCR)에 푸시
  - 태그 전략:
    - `main` → `latest`, `main-{short-sha}`
    - `develop` → `develop-{short-sha}`

#### 3. update-manifests
- **트리거**: main 브랜치에 push 시
- **작업**:
  - `k8s/deployment.yml`의 이미지 태그 자동 업데이트
  - 변경사항 커밋 및 푸시
  - ArgoCD가 감지하여 자동 배포 트리거

### 왜 PostgreSQL은 GitHub Actions에 포함하지 않나요?

**PostgreSQL은 애플리케이션 코드가 아닌 인프라 컴포넌트**이기 때문입니다:

- **애플리케이션 (Board_BE)**:
  - 소스 코드가 변경될 때마다 새로 빌드
  - CI/CD 파이프라인으로 자동 배포
  - 무상태(stateless) 컨테이너로 쉽게 교체 가능

- **PostgreSQL**:
  - 소스 코드 변경과 무관
  - 공식 이미지 사용 (직접 빌드 불필요)
  - 상태(stateful) 데이터를 저장하므로 별도 관리 필요
  - 배포 환경에서 한 번 설정 후 지속적으로 실행

---

## ArgoCD 설정 및 배포

### ArgoCD 설치 (클러스터에 한 번만 실행)

```bash
# ArgoCD 설치
kubectl create namespace argocd
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

# ArgoCD CLI 설치 (선택사항)
# macOS
brew install argocd

# Linux
curl -sSL -o /usr/local/bin/argocd https://github.com/argoproj/argo-cd/releases/latest/download/argocd-linux-amd64
chmod +x /usr/local/bin/argocd

# ArgoCD UI 접속을 위한 포트포워딩
kubectl port-forward svc/argocd-server -n argocd 8080:443

# 초기 비밀번호 확인
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d
```

### ArgoCD에 애플리케이션 추가

#### 방법 1: kubectl 사용 (권장)

```bash
# 1. GitHub 레포지토리 URL을 실제 레포지토리로 변경
# argocd/application.yml 파일 수정:
#   repoURL: https://github.com/YOUR_USERNAME/Board_BE.git

# 2. ArgoCD Application 리소스 생성
kubectl apply -f argocd/application.yml
```

#### 방법 2: ArgoCD CLI 사용

```bash
# ArgoCD 로그인
argocd login localhost:8080

# 애플리케이션 생성
argocd app create board-backend \
  --repo https://github.com/YOUR_USERNAME/Board_BE.git \
  --path k8s \
  --dest-server https://kubernetes.default.svc \
  --dest-namespace board-app \
  --sync-policy automated \
  --auto-prune \
  --self-heal
```

#### 방법 3: ArgoCD UI 사용

1. ArgoCD UI 접속 (http://localhost:8080)
2. `+ NEW APP` 클릭
3. 설정:
   - **Application Name**: `board-backend`
   - **Project**: `default`
   - **Sync Policy**: `Automatic`
   - **Repository URL**: `https://github.com/YOUR_USERNAME/Board_BE.git`
   - **Path**: `k8s`
   - **Cluster URL**: `https://kubernetes.default.svc`
   - **Namespace**: `board-app`
4. `CREATE` 클릭

### ArgoCD 동작 방식 (GitOps)

```
┌─────────────────────────────────────────────────────────────┐
│                    GitOps Workflow                          │
└─────────────────────────────────────────────────────────────┘

1. 개발자 코드 푸시
   └─> GitHub Repository (main branch)

2. GitHub Actions 트리거
   ├─> Gradle 빌드 및 테스트
   ├─> Docker 이미지 빌드 및 GHCR 푸시
   └─> k8s/deployment.yml 이미지 태그 업데이트 및 커밋

3. ArgoCD가 Git 변경 감지 (매 3분마다 또는 webhook)
   └─> Git 매니페스트와 클러스터 상태 비교

4. ArgoCD 자동 동기화 (syncPolicy.automated)
   ├─> 새 이미지로 Deployment 업데이트
   ├─> Rolling Update로 Pod 교체
   └─> 동기화 상태 모니터링

5. 배포 완료
   └─> ArgoCD UI에서 상태 확인
```

---

## PostgreSQL 배포 전략

### 배포 환경에서 PostgreSQL 실행 방법

#### 옵션 1: Kubernetes StatefulSet (권장 - 개발/테스트 환경)

**이미 준비된 매니페스트**: `k8s/postgres.yml`

```bash
# 1. PostgreSQL 배포 (ArgoCD로 자동 배포되지 않음)
kubectl apply -f k8s/postgres.yml

# 2. Secret 생성 (ArgoCD로 자동 배포됨)
kubectl apply -f k8s/secret.yml
```

**특징**:
- StatefulSet으로 안정적인 Pod 이름 및 스토리지
- PersistentVolumeClaim으로 데이터 영속성 보장
- init-db 스크립트 자동 실행 (initdb.d volume mount)
- 클러스터 내부에서만 접근 가능 (Service: ClusterIP)

**장점**:
- 클러스터 내에서 완전히 자급자족
- 간단한 설정 및 관리
- 개발/테스트 환경에 적합

**단점**:
- 프로덕션 수준의 백업/복구 기능 부족
- 고가용성(HA) 구성 복잡
- 리소스 제한에 따른 성능 이슈 가능

#### 옵션 2: 관리형 데이터베이스 (권장 - 프로덕션 환경)

클라우드 제공자의 관리형 PostgreSQL 서비스 사용:
- **AWS RDS for PostgreSQL**
- **Google Cloud SQL for PostgreSQL**
- **Azure Database for PostgreSQL**

```yaml
# k8s/deployment.yml에서 환경변수만 변경
env:
  - name: DB_URL
    value: "jdbc:postgresql://your-rds-endpoint.rds.amazonaws.com:5432/board"
  - name: DB_USERNAME
    valueFrom:
      secretKeyRef:
        name: board-secret
        key: db-username
  - name: DB_PASSWORD
    valueFrom:
      secretKeyRef:
        name: board-secret
        key: db-password
```

**장점**:
- 자동 백업 및 복구
- 고가용성(Multi-AZ) 기본 제공
- 자동 패치 및 유지보수
- 프로덕션 워크로드에 최적화

**단점**:
- 추가 비용 발생
- 클라우드 벤더 종속성

### init-db 스크립트 실행 방법

#### Kubernetes StatefulSet 사용 시

`k8s/postgres.yml`에 이미 설정되어 있음:

```yaml
volumeMounts:
  - name: init-script
    mountPath: /docker-entrypoint-initdb.d
volumes:
  - name: init-script
    configMap:
      name: postgres-init-script
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: postgres-init-script
data:
  init.sql: |
    CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
    CREATE EXTENSION IF NOT EXISTS pg_trgm;
    # ... (나머지 초기화 스크립트)
```

**동작 방식**:
- PostgreSQL 공식 이미지는 `/docker-entrypoint-initdb.d/` 디렉터리의 `.sql` 파일을 **최초 실행 시 자동으로 실행**
- PVC에 데이터가 이미 존재하면 실행하지 않음 (멱등성)

#### 관리형 데이터베이스 사용 시

초기화 스크립트를 **수동으로 한 번 실행**:

```bash
# 1. init-db/init.sql을 관리형 DB에 직접 실행
psql -h your-rds-endpoint.rds.amazonaws.com \
     -U your-admin-user \
     -d postgres \
     -f init-db/init.sql

# 2. 또는 DBeaver, pgAdmin 같은 GUI 도구 사용
```

---

## 배포 프로세스

### 초기 환경 설정 (한 번만 실행)

```bash
# 1. GitHub Container Registry 권한 설정
# GitHub Settings > Developer settings > Personal access tokens
# 'write:packages' 권한으로 토큰 생성

# 2. ArgoCD 설치
kubectl create namespace argocd
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

# 3. PostgreSQL 배포 (StatefulSet 사용 시)
kubectl create configmap postgres-init-script --from-file=init-db/init.sql
kubectl apply -f k8s/postgres.yml

# 4. Secret 생성
# k8s/secret.yml 파일에 실제 값 입력 후:
kubectl apply -f k8s/secret.yml

# 5. ArgoCD에 애플리케이션 추가
# argocd/application.yml에서 repoURL 수정 후:
kubectl apply -f argocd/application.yml
```

### 일반 배포 프로세스 (자동화)

```bash
# 1. 코드 변경 및 커밋
git add .
git commit -m "feat: 새로운 기능 추가"
git push origin main

# 2. GitHub Actions 자동 실행
# - 빌드 및 테스트
# - Docker 이미지 빌드 및 푸시
# - k8s/deployment.yml 이미지 태그 업데이트

# 3. ArgoCD 자동 동기화
# - Git 변경 감지 (최대 3분 소요)
# - 클러스터에 자동 배포

# 4. 배포 확인
kubectl get pods -n board-app
kubectl logs -f deployment/board-backend -n board-app
```

### 수동 동기화 (필요 시)

```bash
# ArgoCD CLI
argocd app sync board-backend

# 또는 UI에서 'SYNC' 버튼 클릭
```

---

## 트러블슈팅

### ArgoCD 동기화 실패

```bash
# 애플리케이션 상태 확인
kubectl get application -n argocd board-backend -o yaml

# ArgoCD 서버 로그 확인
kubectl logs -n argocd deployment/argocd-server
```

### PostgreSQL 연결 실패

```bash
# PostgreSQL Pod 상태 확인
kubectl get pods -l app=postgres

# PostgreSQL 로그 확인
kubectl logs -f statefulset/postgres

# 연결 테스트
kubectl run psql-test --rm -it --image=postgres:16 -- \
  psql -h postgres.default.svc.cluster.local -U board_user -d board
```

### 이미지 Pull 실패

```bash
# GHCR 이미지가 public인지 확인
# GitHub Repository > Settings > Packages > 패키지 선택 > Change visibility

# 또는 ImagePullSecret 생성
kubectl create secret docker-registry ghcr-secret \
  --docker-server=ghcr.io \
  --docker-username=YOUR_GITHUB_USERNAME \
  --docker-password=YOUR_GITHUB_TOKEN \
  --docker-email=YOUR_EMAIL

# deployment.yml에 추가:
# imagePullSecrets:
#   - name: ghcr-secret
```

---

## 모니터링 (다음 단계)

배포 완료 후 Prometheus 및 Grafana를 설정하여 모니터링을 추가할 예정입니다.
자세한 내용은 `05-monitoring.md` 문서를 참조하세요.
