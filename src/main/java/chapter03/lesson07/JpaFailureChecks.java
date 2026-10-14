package chapter03.lesson07;

import jakarta.persistence.OptimisticLockException;
import java.sql.SQLException;

/**
 * 커밋이 실패했을 때 던져진 예외가 어떤 DB 충돌인지 구분한다.
 * EntityManager를 다루지 않는다. catch에 잡힌 Throwable의 원인 체인만 따라간다.
 *
 * flush나 commit이 실패하면 호출자에게 바로 OptimisticLockException이나 SQLException이 오지 않는 경우가 많다.
 * Hibernate가 JDBC 오류를 자기 예외로 감싸고, JPA가 그것을 PersistenceException으로 다시 감싼다.
 * catch (RuntimeException e)의 e는 맨 바깥 껍질이므로, e만 instanceof로 보면 실제 원인을 놓친다.
 * getCause()를 null이 될 때까지 내려가며 안쪽 예외의 타입과 MySQL 오류 번호를 확인한다.
 *
 * 낙관적 락은 JpaPost의 @Version 컬럼과 연결된다.
 * 영속 엔티티를 고치고 commit하면 UPDATE에 "WHERE id = ? AND version = ?"가 붙는다.
 * 버전은 읽었을 때의 값이고, UPDATE가 성공하면 version이 1 증가한다.
 * 그 사이에 다른 트랜잭션이 같은 행을 먼저 커밋하면 version이 이미 바뀌어 영향 받은 행이 0건이 된다.
 * 이때 JPA는 OptimisticLockException을 던지고, 이 트랜잭션은 롤백된다.
 * 메모리의 엔티티와 DB가 어긋나므로 그 EntityManager로는 이어서 작업하지 않는다.
 *
 * 중복 키는 제약이 거절한 INSERT/UPDATE다. JpaMember.loginId는 unique라 같은 login_id를 다시 넣으면 MySQL이 1062를 반환한다.
 * 외래 키 위반은 다른 오류다. 없는 member_id로 게시글을 넣으면 MySQL 1452가 나고, 이것은 중복 키가 아니다.
 * SQLState 23000은 둘 다 무결성 위반이라 구분되지 않는다. 그래서 벤더 오류 번호 1062만 중복 키로 본다.
 */
public class JpaFailureChecks {
    public static boolean isOptimisticLock(Throwable error) {
        // error는 commit 실패 때 잡힌 맨 바깥 예외다. OptimisticLockException이 그 안에 들어 있을 수 있다.
        Throwable current = error;
        while (current != null) {
            // jakarta.persistence.OptimisticLockException이면 버전 충돌이다.
            // Hibernate의 StaleStateException이 더 안쪽에 있어도, JPA 계층이 이 예외로 감싸 두면 여기서 걸린다.
            if (current instanceof OptimisticLockException) return true;
            // 한 겹 안으로. 원인이 없으면 null이 되어 반복이 끝난다.
            current = current.getCause();
        }
        // 체인 끝까지 낙관적 락 예외가 없으면 버전 충돌이 아니다. 중복 키나 다른 오류일 수 있다.
        return false;
    }

    public static boolean isDuplicateKey(Throwable error) {
        // MySQL 1062(ER_DUP_ENTRY)만 중복 키로 본다. 외래 키 위반 1452는 true가 되면 안 된다.
        Throwable current = error;
        while (current != null) {
            // PersistenceException 안의 ConstraintViolationException 안에 SQLException이 있는 구조를 가정하고 내려간다.
            // getErrorCode()는 SQLState가 아니라 MySQL이 부여한 정수 번호다. 같은 1062라도 다른 DB 제품에서는 의미가 다르다.
            if (current instanceof SQLException sql && sql.getErrorCode() == 1062) return true;
            current = current.getCause();
        }
        // 1062가 없으면 중복 키가 아니다. 1452나 연결 오류, 낙관적 락은 여기로 온다.
        return false;
    }
}