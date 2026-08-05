# 동네모아 Dongnae-Moa

주민이 제보한 동네 문제를 AI가 5분짜리 **마이크로 퀘스트**로 자동 가공하여, 주민 스스로 동네를 가꾸고 해결하도록 돕는 주민 참여형 지역 사회 플랫폼, 주민 주도적 공동체 커뮤니티입니다.

## 어떤 서비스인가요

1. 주민이 동네에서 발견한 문제(쓰레기 무단투기, 불법 주차, 통행을 방해하는 공유 킥보드 등)를 사진과 함께 제보합니다.
2. AI가 제보 내용을 분석해 소요 시간, 보상 포인트, 난이도, 인증 체크리스트가 포함된 퀘스트로 자동 변환합니다.
3. 같은 동네 주민이 퀘스트에 참여하고, 처리 후 인증 사진을 올립니다.
4. 퀘스트 등록자가 인증을 검토해 완료를 승인하면 참여자에게 포인트가 지급됩니다.

신고만 하고 끝나는 것이 아니라, 주민들이 직접 동네 문제를 해결하는 선순환 구조를 만드는 것이 목표입니다.

### 예시

- 공원에 버려진 쓰레기 → AI가 "OO 지역 쓰레기 치우고 인증하기" 퀘스트 생성 → 다른 주민이 치우고 인증
- 인도를 막은 공유 킥보드 → 등록자는 신고로 포인트, 수행자는 처리로 포인트, 검수자는 진위 확인으로 포인트
- 불법 주차 차량 → 안전신문고 신고 후 답변 캡처를 업로드하면 퀘스트 완료

## 기술 스택

- **Java 21** / **Spring Boot 3.4**
- Spring Data JPA, Spring Security, JWT(jjwt)
- PostgreSQL
- Groq API (`llama-3.1-8b-instant`) 기반 AI 퀘스트 분석
- springdoc-openapi (Swagger UI)
- Gradle

## 아키텍처

도메인 중심 패키지 구조를 따릅니다.

```
src/main/java/zaman/dongnaemoa/
├── domain/
│   ├── user/            # 회원가입, 로그인, 내 정보 조회
│   ├── neighborhood/    # GPS 기반 동네 가입/조회
│   ├── quest/           # 퀘스트 등록/조회/삭제, AI 분석 연동
│   ├── participation/   # 퀘스트 참여, 인증 제출, 승인/반려
│   └── reward/          # 퀘스트 완료 보상(포인트)
└── global/
    ├── ai/               # Groq 기반 퀘스트 분석기(GroqQuestAnalyzer)
    ├── security/, jwt/   # 인증/인가
    ├── geo/              # 위치 기반 동네 판별
    ├── storage/, multipart/  # 이미지 업로드 처리
    ├── config/, exception/
```

각 도메인은 `controller / service / repository / entity / dto` 레이어로 구성됩니다.

## 주요 API

| 기능 | Method | Endpoint |
|---|---|---|
| 회원가입 | POST | `/v1/auth/signup` |
| 로그인 | POST | `/v1/auth/login` |
| 내 정보 조회 | GET | `/v1/users/me` |
| 동네 가입 | POST | `/v1/neighborhoods/join` |
| 동네 목록 조회 | GET | `/v1/neighborhoods` |
| 동네 상세 조회 | GET | `/v1/neighborhoods/{neighborhoodId}` |
| 퀘스트 등록 (이미지 첨부 가능) | POST | `/v1/quests` |
| 퀘스트 목록 조회 | GET | `/v1/quests` |
| 퀘스트 삭제 | DELETE | `/v1/quests/{questId}` |
| 퀘스트 참여 | POST | `/v1/quests/{questId}/participations` |
| 참가자 목록 조회 | GET | `/v1/quests/{questId}/participations` |
| 참여 인증 제출 | POST | `/v1/participations/{participationId}/proof` |
| 참여 승인 | POST | `/v1/participations/{participationId}/approve` |
| 참여 반려 | POST | `/v1/participations/{participationId}/reject` |

인증이 필요한 API는 `Authorization: Bearer {accessToken}` 헤더가 필요합니다.

퀘스트 등록 시 AI(Groq)가 제목/설명을 분석해 소요 시간(minutes), 보상 포인트(rewardPoint), 난이도(difficulty), 인증 체크리스트(checkpoints)를 자동 산정합니다. 단순 상태 확인(신고형) 퀘스트는 `minutes=1`, `rewardPoint=10`으로 낮게, 실제 처리 작업(청소 등)이 필요한 퀘스트는 `minutes 5~30`, `rewardPoint 10~200` 범위로 산정됩니다. Groq API 키가 없거나 호출에 실패하면 기본값(`minutes=10, rewardPoint=50, NORMAL`)으로 대체됩니다.

## 로컬 실행

### 요구 사항

- JDK 21
- PostgreSQL (로컬 실행 시)

### 환경 변수

| 변수 | 설명 | 기본값 |
|---|---|---|
| `DB_URL` | PostgreSQL 접속 URL | `jdbc:postgresql://localhost:5432/dongnaemoa` |
| `DB_USERNAME` | DB 사용자명 | `postgres` |
| `DB_PASSWORD` | DB 비밀번호 | `postgres` |
| `JWT_SECRET` | JWT 서명 키 | (테스트용 기본값 제공) |
| `JWT_ACCESS_EXPIRATION` | Access Token 만료(ms) | `10800000` (3시간) |
| `GROQ_API_KEY` | Groq API 키 (미설정 시 AI 분석은 기본값으로 대체) | - |
| `FILE_UPLOAD_DIR` | 업로드 이미지 저장 경로 | `uploads` |
| `FILE_PUBLIC_BASE_URL` | 업로드 이미지 공개 URL prefix | `http://localhost:8080/files` |

### 실행

```bash
./gradlew bootRun
```

### 테스트

```bash
./gradlew test
```

### API 문서

애플리케이션 실행 후 Swagger UI에서 확인할 수 있습니다.

```
http://localhost:8080/swagger-ui.html
```

## 배포

GitHub Actions 기반 CI/CD 파이프라인을 사용합니다. `main` 브랜치 push 시 CI(컴파일/테스트, Testcontainers PostgreSQL 포함)가 통과해야만 CD(SSH 배포, systemd 재시작)가 실행됩니다. 자세한 내용은 [`docs/deploy/`](./docs/deploy) 문서를 참고하세요.

- [CI/CD 파이프라인](./docs/deploy/ci-cd-pipeline.md)
- [systemd 설정](./docs/deploy/systemd-setup.md)
- [PostgreSQL 설정](./docs/deploy/postgresql-setup.md)

## 참고 문서

- [`docs/PROJECT_INFO.md`](./docs/PROJECT_INFO.md) — 프로젝트 한 줄 소개
- [`docs/FUNCTION.md`](./docs/FUNCTION.md) — MVP 기능 정의 및 시나리오 예시