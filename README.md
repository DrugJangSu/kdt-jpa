# kdt-jpa

부트캠프에서 학습한 Java / JPA 실습 프로젝트입니다.

- Java 21, Gradle, Hibernate ORM, MySQL
- 소스: `src/main/java/chapter03/lesson07`
- 엔티티, 연관 관계, JPQL, 페이징, 트랜잭션, 리포지토리 및 서비스 실습

## IntelliJ에서 열기

1. File → Open에서 프로젝트 폴더를 선택합니다.
2. Gradle 프로젝트로 불러오고 Project SDK를 Java 21로 지정합니다.
3. Gradle 동기화를 진행합니다.

## DB 설정

기존 로컬 `src/main/resources/META-INF/persistence.xml`이 있으면 그대로 사용합니다.
처음 내려받은 경우 같은 폴더의 `persistence-example.xml`을 복사해
`persistence.xml`로 이름을 바꾸고 DB 이름, 계정, 비밀번호를 자신의 값으로 수정합니다.
예제의 `YOUR_DATABASE`, `YOUR_DB_USERNAME`, `YOUR_DB_PASSWORD`는 직접 바꾸는 자리표시자입니다.
환경변수로 자동 치환되지 않습니다.

실제 `persistence.xml`은 Git에서 제외합니다. 예제 파일에는 실제 비밀번호를 넣지 않습니다.
예제의 스키마 자동 생성 설정은 `none`이므로 실습에 필요한 DB와 테이블을 먼저 준비해야 합니다.
데이터를 수정하는 실습이 있으므로 실습용 DB를 사용합니다.

## GitHub에 올리는 파일

Java 소스, Gradle 설정과 Wrapper, 비밀번호 없는 설정 예제, 이 안내를 올립니다.
로컬 DB 설정, IntelliJ 설정(`.idea/`), `.env`, 개인 키, 빌드 결과 및 OS 파일은 제외합니다.
공유할 SQL 스키마 파일은 내용 확인 후 올릴 수 있으며 DB 덤프는 `dumps/`에 보관합니다.
새 application/profile 설정을 추가할 때도 실제 비밀번호나 토큰을 넣지 마세요.

## IntelliJ에서 다음 변경 올리기

1. Git → Manage Remotes에서 `origin`이 `https://github.com/DrugJangSu/kdt-jpa.git`인지 확인합니다.
2. Commit 창에서 변경 내용과 올릴 파일을 확인합니다. 실제 DB 설정이 보이면 커밋하지 말고 제외 설정을 확인합니다.
3. 변경 내용을 설명하는 메시지를 입력하고 Commit을 누릅니다. 여기까지는 내 Mac에만 저장됩니다.
4. Git → Push에서 `main → origin/main`을 확인하고 Push를 누릅니다. 이때 GitHub에 올라갑니다.
5. GitHub 저장소에서 최신 커밋과 파일을 확인합니다.
