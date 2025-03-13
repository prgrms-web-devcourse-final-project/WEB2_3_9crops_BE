# 36.5 (온라인 편지 교환 플랫폼)

36.5는 사용자들이 익명으로 편지를 주고받을 수 있는 소셜 플랫폼입니다. 온라인에서 따뜻한 인간 관계를 형성하고 소통할 수 있는 공간을 제공합니다.

![Thumbnail](https://github.com/user-attachments/assets/8752a9d3-8f3d-4523-b51f-472dcbcf1b4a)
![Image](https://github.com/user-attachments/assets/328e3267-35c7-47ae-9091-850b90d07499)

- 프로젝트 기간 : 2025.02.07 ~ 2025.03.14
- 배포 URL : https://www.ddasum.kr/

## 구성원
| [이영섭](https://github.com/chesthyeon) | [고진영](https://github.com/Kojinyoung7220) | [양한동](https://github.com/yhd1101) | [이종찬](https://github.com/LeeVell) | [허정현](https://github.com/HeoJeongHyeon) |
| --- | --- | --- | --- | --- |
| <a href="https://github.com/leeys9423"><img src="https://avatars.githubusercontent.com/u/67312117?v=4" width="100px;" alt=""/></a> | <a href="https://github.com/Kojinyoung7220"><img src="https://avatars.githubusercontent.com/u/133757475?v=4" width="100px;" alt=""/></a> | <a href="https://github.com/yhd1101"><img src="https://avatars.githubusercontent.com/u/117626705?v=4" width="100px;" alt=""/></a> | <a href="https://github.com/LeeVell"><img src="https://avatars.githubusercontent.com/u/50389081?v=4" width="100px;" alt=""/></a> | <a href="https://github.com/HeoJeongHyeon"><img src="https://avatars.githubusercontent.com/u/188425240?v=4" width="100px;" alt=""/></a> |
| PO | Team-Lead  | Clerk | Clerk | Clerk |
| 소셜로그인,<br/> 인증 및 인가 | 편지, JIRA | AWS,<br/> 금칙어, 신고 |  알림,<br/> 이벤트 게시판 | 공유 편지,<br/> 공유 편지 게시판 |

## 📋 프로젝트 개요

WarmLetter는 다음과 같은 기능을 제공합니다:

- **랜덤 편지**: 카테고리별 랜덤 편지 작성 및 매칭
- **편지 주고받기**: 매칭된 사용자와 지속적인 편지 교환
- **공유 기능**: 의미 있는 대화를 게시판에 공유
- **알림 시스템**: 실시간 SSE 기반 알림 제공
- **소셜 로그인**: Google, Kakao, Naver를 통한 간편 로그인
- **온도 시스템**: 사용자 신뢰도를 나타내는 온도 지표

## 🛠 기술 스택

### 백엔드
- **언어 및 프레임워크**: Java 17, Spring Boot 3.0
- **보안**: Spring Security, JWT
- **데이터베이스**: MySQL, Redis
- **ORM**: Spring Data JPA, Querydsl
- **문서화**: Swagger (OpenAPI 3.0)
- **빌드 도구**: Gradle

### 인프라
- **배포**: Docker, AWS EC2
- **CI/CD**: Github Actions
- **품질 관리**: SonarCloud, JaCoCo

## 📂 프로젝트 구조

도메인 중심 구조로 설계

```
src/main/java/io/crops/warmletter/
├── domain/
│   ├── auth/            # 인증 관련 로직
│   ├── badword/         # 금칙어 필터링 
│   ├── eventpost/       # 이벤트 게시판
│   ├── letter/          # 편지 핵심 기능
│   ├── member/          # 회원 관리
│   ├── report/          # 신고 기능
│   ├── share/           # 편지 공유 기능
│   └── timeline/        # 알림 및 타임라인
├── global/
│   ├── config/          # 전역 설정
│   ├── entity/          # 공통 엔티티
│   ├── error/           # 예외 처리
│   ├── jwt/             # JWT 인증
│   ├── oauth/           # OAuth2 인증
│   ├── response/        # 공통 응답 형식
│   ├── schedule/        # 스케줄링 작업
│   └── util/            # 유틸리티 클래스
└── WarmLetterApplication.java
```

## 🔒 인증 및 보안

- **JWT 기반 인증**: 액세스 토큰 및 리프레시 토큰
- **OAuth2 소셜 로그인**: Google, Kakao, Naver 지원
- **CORS 설정**: 안전한 교차 출처 리소스 공유
- **토큰 블랙리스트**: 로그아웃 및 토큰 무효화 관리

## 📬 편지 시스템

- **랜덤 매칭**: 사용자는 관심 카테고리를 선택하여 랜덤 편지를 받을 수 있음
- **배송 시스템**: 편지는 일정 시간 후 배달되어 실제 편지 느낌을 제공
- **서신함**: 주고받은 편지를 저장하고 관리하는 공간
- **임시 저장**: 작성 중인 편지를 임시 저장할 수 있는 기능

## 🔔 알림 시스템

실시간 알림은 SSE(Server-Sent Events)를 기반

- 새 편지 도착
- 편지 발송 중
- 공유 요청 수신
- 게시글 공유 완료
- 신고 처리 결과

## 🔍 코드 품질 관리

![Image](https://github.com/user-attachments/assets/75bdcad3-b165-4138-9c1e-f5b882dc7824)

### SonarCloud
- 코드 품질 및 보안 취약점 지속적 분석
- 코드 중복, 복잡도, 잠재적 버그 감지
- 코드 커버리지 측정 및 보고

### JaCoCo (Java Code Coverage)
- 코드 테스트 커버리지 측정
- 브랜치, 라인, 메소드 커버리지 분석
- CI/CD 파이프라인과 통합되어 품질 게이트 적용

## 🌟 주요 기능 하이라이트

### 1. 온도 시스템
사용자의 신뢰도와 활동을 기반으로 한 온도 시스템이 구현되어 있습니다. 편지 평가, 신고 처리 등에 따라 온도가 변동됩니다.

### 2. 금칙어 필터링
사용자 생성 콘텐츠는 금칙어 필터링을 통과해야 하며, 관리자가 금칙어를 관리할 수 있습니다.

### 3. 실시간 알림
SSE를 활용한 실시간 알림 시스템으로 사용자 경험을 향상시켰습니다.

### 4. 신고 및 모더레이션
AI 기반 콘텐츠 검토 및 관리자 검토 시스템을 통해 안전한 커뮤니티를 유지합니다.

## 📝 API 문서

API 문서는 Swagger를 통해 제공됩니다
