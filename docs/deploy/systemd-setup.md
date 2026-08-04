# systemd 배포 설정 가이드

Ubuntu 배포 서버(`ubuntu-vm-112`)에서 `dongnae-moa`를 systemd 서비스로 등록하는 절차.
CD 워크플로우(`.github/workflows/cd.yml`)가 `sudo systemctl restart dongnae-moa`를 호출하므로,
아래 작업을 서버에서 먼저 완료해야 CD가 정상 동작한다.

## 1. systemd 유닛 파일 생성

```bash
sudo tee /etc/systemd/system/dongnae-moa.service > /dev/null <<'EOF'
[Unit]
Description=dongnae-moa Spring Boot application
After=network.target

[Service]
User=ubuntu
WorkingDirectory=/home/ubuntu/dongnae-moa
ExecStart=/bin/bash -c 'exec java -jar $(ls -t /home/ubuntu/dongnae-moa/build/libs/*.jar | grep -v plain | head -1) --spring.profiles.active=prod'
Restart=on-failure
RestartSec=5
Environment=DB_URL=jdbc:postgresql://localhost:5432/dongnaemoa
Environment=DB_USERNAME=postgres
Environment=DB_PASSWORD=여기에_실제_DB_비밀번호
Environment=JWT_SECRET=여기에_실제_JWT_시크릿

[Install]
WantedBy=multi-user.target
EOF
```

> `DB_PASSWORD`, `JWT_SECRET` 값은 실제 운영값으로 교체할 것.
> 다른 방식(예: `.env`, `application-prod.yml`)으로 이미 환경변수를 주입 중이라면 `Environment=` 두 줄은 생략해도 된다.

## 2. 서비스 등록 및 활성화

```bash
    sudo systemctl daemon-reload
    sudo systemctl enable dongnae-moa
    sudo systemctl start dongnae-moa
    sudo systemctl status dongnae-moa --no-pager
```

`Active: active (running)`이 뜨면 정상.

## 3. 배포 사용자(ubuntu) 비밀번호 없이 재시작 허용

CD 스크립트가 `sudo systemctl restart dongnae-moa`를 비대화형으로 실행하므로,
`ubuntu` 계정이 비밀번호 프롬프트 없이 이 명령을 실행할 수 있어야 한다.

```bash
echo "ubuntu ALL=(ALL) NOPASSWD: /bin/systemctl restart dongnae-moa, /bin/systemctl status dongnae-moa" | sudo tee /etc/sudoers.d/dongnae-moa-deploy
sudo visudo -c
```

`visudo -c`가 `parsed OK`를 출력하면 완료. 이후 GitHub Actions에서 main 브랜치로 push하면
`sudo systemctl restart dongnae-moa`가 비밀번호 없이 정상 동작한다.

## 4. 확인

```bash
sudo systemctl status dongnae-moa --no-pager
curl -i http://localhost:8080/api/v1/quests
```

또는 애플리케이션에 헬스체크 엔드포인트가 없다면 실제 API 하나를 curl로 호출해 응답을 확인한다.
