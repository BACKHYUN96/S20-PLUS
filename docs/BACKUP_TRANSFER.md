# 아티팩트 다운로드 실패 시 대체 전달

2026-10-07 사용자 보고: 큰 프로젝트 ZIP과 3KB 서명 ZIP 모두 일반 파일 링크와 sandbox 링크에서 아티팩트 다운로드 오류가 발생했습니다. 원본 파일의 checksum/ZIP 검사는 정상입니다. 용량만으로 설명하기 어렵지만 클라이언트/다운로드 서비스의 정확한 원인은 확인하지 못했습니다. 표준 다운로드 경로 /mnt/data 복사도 운영체제 권한으로 실패했습니다. 자동 승인 검토 거절과는 다릅니다. 파일 패널 열기 요청은 queued이며 저장 성공이 아닙니다.

## 프로젝트 소스

GitHub에서 직접 받습니다: https://github.com/BACKHYUN96/S20-PLUS/archive/refs/heads/main.zip

실제 HEAD 요청은 HTTP 200을 반환했습니다. 이 ZIP은 소스/PNG/검사/문서를 포함하며 기존 전체 백업의 APK·Git 이력·개인 서명키는 포함하지 않습니다. Git 이력까지 보존하려면 Git clone을 사용합니다. 기존 전체 백업은 클라우드에 그대로 있습니다.

## 개인 서명 백업의 암호화 텍스트 전달

노트북 PowerShell에서 `pixel-traffic/tools/Prepare-BackupTransfer.ps1`의 내용을 실행합니다. 일회성 전송용 RSA 키를 만들고 개인 부분은 해당 Windows 사용자 DPAPI로 암호화하여 `%LOCALAPPDATA%/PixelTrafficTransfer/transport-private.dpapi`에 보관합니다. 같은 파일이 있으면 가져와 재사용합니다. 출력은 공개 RSAKeyValue XML뿐이며 이를 채팅에 전달합니다. 현재 폰 앱의 debug 서명키와는 별개의 전송용 키입니다.

클라우드에서는 사용자의 공개 XML을 별도 임시 파일에 저장하고 다음 도우미로 작은 서명 ZIP을 암호화합니다. Python cryptography가 필요하며 현재 환경에는 설치되어 있습니다. 개인키/원본 ZIP/base64 원문을 채팅이나 Git에 출력하지 않습니다.

```bash
cd pixel-traffic
python tools/encrypt-signing-backup.py /tmp/user-public.xml /workspace/artifacts/S20-PLUS-0.29.0-signing-backup.zip /tmp/encrypted-signing-packet.json
```

도우미는 공개키만 허용하고 RSA 최소 2048비트, 작은 파일만 허용하며 기존 출력 파일을 덮어쓰지 않습니다. 전송 패킷은 OAEP-SHA1 암호문 블록, 블록 크기와 원본 SHA256을 포함합니다. SHA1은 Windows RSACryptoServiceProvider의 OAEP 호환 파라미터로만 쓰며 파일 무결성은 SHA256으로 검사합니다. 암호문은 사용자 노트북의 전송용 개인키로만 복호화합니다.

공개키가 도착하면 사용자가 붙여넣어 실행할 PowerShell 복원 명령과 암호문을 함께 제공합니다. 복호화 후 원본 ZIP SHA256과 압축 검사를 확인하고 기존 signing restore 도우미를 사용합니다. 기존 개인 파일이나 다른 서명키를 덮어쓰지 않습니다.

## 실제 확인과 대기

현재 클라우드에서 생성한 임시 RSA fixture로 실제 2,989bytes 서명 ZIP의 암호화/복호화 byte 일치를 확인했습니다. 암호문은 3,584bytes입니다. Windows/DPAPI 실행은 미검증입니다. 실제 사용자 전송은 공개키 수신·노트북 복원 확인 대기이며 완료된 것으로 보고하지 않습니다. 앱 소스/버전/자산은 바뀌지 않았습니다.
