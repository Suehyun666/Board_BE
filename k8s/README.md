# Kubernetes 배포 가이드

## 사전 준비

### 1. Secret 파일 준비
```bash
# secret.example.yml을 복사하여 실제 값 입력
cp k8s/secret.example.yml k8s/secret.yml

# secret.yml 파일 수정 (실제 비밀번호 입력)
vi k8s/secret.yml
```

### 2. Docker 이미지 빌드 및 푸시
```bash
# 이미지 빌드
docker build -t your-registry/board-backend:latest .

# 이미지 푸시
docker push your-registry/board-backend:latest
```

## 배포 순서

### 1. Secret 적용
```bash
kubectl apply -f k8s/secret.yml
```

### 2. PostgreSQL 배포
```bash

```

### 3. Backend 배포
```bash
# deployment.yml 파일에서 이미지 레지스트리 주소 수정 후
kubectl apply -f k8s/deployment.yml

# 배포 상태 확인
kubectl get pods -w
```

## 배포 확인

### Pod 상태 확인
```bash
kubectl get pods
kubectl describe pod <pod-name>
kubectl logs <pod-name>
```

### Service 확인
```bash
kubectl get svc
```

### Ingress 확인
```bash
kubectl get ingress
```

## 로컬 테스트

### Port-Forward로 로컬 접속
```bash
# Backend
kubectl port-forward svc/board-backend-service 8080:8080

# PostgreSQL
kubectl port-forward svc/postgres-service 5432:5432
```

그 후 브라우저에서:
- Swagger UI: http://localhost:8080/swagger-ui.html
- Redoc: http://localhost:8080/redoc.html
- Actuator: http://localhost:8080/actuator/health

## 스케일링

### Pod 개수 조정
```bash
kubectl scale deployment board-backend --replicas=5
```

### HPA (Horizontal Pod Autoscaler) 설정
```bash
kubectl autoscale deployment board-backend \
  --cpu-percent=70 \
  --min=3 \
  --max=10
```

## 업데이트

### Rolling Update
```bash
# 새 이미지 빌드 및 푸시
docker build -t your-registry/board-backend:v2 .
docker push your-registry/board-backend:v2

# 이미지 업데이트
kubectl set image deployment/board-backend \
  board-backend=your-registry/board-backend:v2

# 롤아웃 상태 확인
kubectl rollout status deployment/board-backend

# 롤백 (필요시)
kubectl rollout undo deployment/board-backend
```

## 삭제

```bash
# 전체 삭제
kubectl delete -f k8s/deployment.yml
kubectl delete -f k8s/postgres.yml
kubectl delete -f k8s/secret.yml
```

## 환경 변수 관리

Secret에 저장된 환경 변수:
- `DB_URL`: PostgreSQL 연결 URL
- `DB_USERNAME`: PostgreSQL 사용자명
- `DB_PASSWORD`: PostgreSQL 비밀번호
- `DB_POOL_SIZE`: 커넥션 풀 크기

## 모니터링

### Prometheus + Grafana 접속
```bash
# Prometheus
kubectl port-forward svc/prometheus-server 9090:80

# Grafana
kubectl port-forward svc/grafana 3000:80
```

Spring Boot Actuator 메트릭:
- http://localhost:8080/actuator/prometheus
- http://localhost:8080/actuator/metrics

## 문제 해결

### Pod이 시작되지 않는 경우
```bash
# 이벤트 확인
kubectl describe pod <pod-name>

# 로그 확인
kubectl logs <pod-name>

# 이전 Pod 로그 확인
kubectl logs <pod-name> --previous
```

### DB 연결 실패
```bash
# PostgreSQL 상태 확인
kubectl exec -it postgres-0 -- psql -U postgres -d board

# Secret 확인
kubectl get secret board-secret -o yaml
```

### 이미지 Pull 실패
```bash
# ImagePullSecret 생성 (Private Registry 사용 시)
kubectl create secret docker-registry regcred \
  --docker-server=<your-registry> \
  --docker-username=<username> \
  --docker-password=<password> \
  --docker-email=<email>

# deployment.yml에 추가
# spec:
#   template:
#     spec:
#       imagePullSecrets:
#         - name: regcred
```
