// [역할] JPA 설정(persistence.xml, DB 연결)이 정상인지 확인하는 연결 테스트. "emf ok"가 나오면 성공.
package chapter03.lesson07;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

// JPA 설정이 제대로 됐는지 확인하는 가장 간단한 실행 파일(연결 테스트).
// 에러 없이 "emf ok"가 출력되면 persistence.xml 설정과 DB 접속 준비가 정상이다.
public class JpaPing {
    public static void main(String[] args) {
        // persistence.xml의 이름이 "kdt"인 설정을 읽어 EntityManagerFactory(JPA 공장)를 만든다.
        // try (...) 형태라서 블록이 끝나면 자동으로 close() 된다.
        try (EntityManagerFactory emf = Persistence.createEntityManagerFactory("kdt")) {
            System.out.println("emf ok");
        }
    }
}
