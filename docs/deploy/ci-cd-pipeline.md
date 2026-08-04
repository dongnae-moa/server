# CI/CD 파이프라인

## 흐름

```
main 브랜치 push
      |
      v
  CI (.github/workflows/ci.yml)
  - compileJava, compileTestJava, test 실행 (Testcontainers PostgreSQL 포함)
      |
      v  (성공 시에만)
  CD (.github/workflows/cd.yml)
  - SSH로 배포 서버 접속
  - git fetch/clone
  - gradle clean bootJar -x test  (테스트는 CI에서 이미 검증됨, 중복 실행 안 함)
  - systemctl restart dongnae-moa
```

## 책임 분리

- **CI**: 코드 품질 게이트. 컴파일 실패 또는 테스트 실패 시 여기서 멈춘다.
  배포는 절대 진행되지 않는다.
- **CD**: CI가 성공(`conclusion == success`)한 커밋만 배포한다.
  `workflow_run` 트리거로 CI 완료 이벤트에 연결되어 있으며, CI가 실패/취소되면
  CD 자체가 트리거되지 않는다.
  배포 서버에서는 테스트를 다시 돌리지 않는다(`-x test`) — 이미 CI가 검증한
  동일 커밋이므로 중복이며, 배포 서버에 Testcontainers용 Docker가 없을 수도 있다.

## 수동 배포

`workflow_dispatch`로 CI 결과와 무관하게 즉시 배포할 수 있다
(GitHub Actions 탭에서 CD workflow를 수동 실행). 긴급 롤백 등에 사용한다.

## 필요 GitHub Secrets

- `SERVER_IP`: 배포 대상 서버 퍼블릭 IP
- `SERVER_SSH_KEY`: 배포 서버 접속용 SSH private key (PEM)

서버 측 사전 준비는 [systemd-setup.md](./systemd-setup.md),
[postgresql-setup.md](./postgresql-setup.md) 참고.
