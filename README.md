# kdt-jpa

부트캠프 Java / JPA 실습 프로젝트입니다.

- **기술 스택:** Java 21, Gradle, Hibernate ORM, MySQL
- **학습 내용:** 엔티티, 연관 관계, JPQL, 페이징, 트랜잭션, 리포지토리와 서비스

## DB 설정

1. `src/main/resources/META-INF/persistence-example.xml`을 같은 폴더에 `persistence.xml`로 복사합니다. 기존 파일이 있으면 그대로 사용합니다.
2. DB 이름, 계정, 비밀번호를 자신의 환경에 맞게 입력합니다.
3. 실습용 DB와 테이블을 준비합니다. 테이블은 자동 생성되지 않습니다.

실제 접속 정보가 담긴 `persistence.xml`은 Git에 포함되지 않습니다.
