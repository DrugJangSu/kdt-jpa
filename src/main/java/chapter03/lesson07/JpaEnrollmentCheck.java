package chapter03.lesson07; // 이 파일이 속한 패키지(폴더)입니다.

import jakarta.persistence.EntityManager; // DB 작업을 담당하는 객체입니다.
import jakarta.persistence.EntityManagerFactory; // EntityManager를 만들어 주는 공장입니다.
import jakarta.persistence.Persistence; // 공장을 만들 때 쓰는 도우미 클래스입니다.

// 수강 신청 기능이 제대로 동작하는지 직접 실행해 확인하는 프로그램입니다.
public class JpaEnrollmentCheck {
    public static void main(String[] args) { // 프로그램이 시작되는 곳입니다.
        try (EntityManagerFactory emf = Persistence.createEntityManagerFactory("kdt")) { // persistence.xml의 "kdt" 설정으로 공장을 만들고, 끝나면 자동으로 닫습니다.
            int studentId; // 준비한 학생의 번호를 담을 변수입니다.
            int courseId; // 준비한 강좌의 번호를 담을 변수입니다.
            // 시작 데이터를 한 트랜잭션에서 준비하고 실제 ID를 기록합니다.
            try (EntityManager em = emf.createEntityManager()) { // 작업용 EntityManager를 만들고, 끝나면 자동으로 닫습니다.
                // 트랜잭션을 시작해 조회와 변경을 하나의 작업으로 묶습니다.
                em.getTransaction().begin(); // 트랜잭션 시작
                try {
                    Student student = new Student("practice student"); // 연습용 학생 객체를 만듭니다.
                    Course course = new Course("JPA practice course"); // 연습용 강좌 객체를 만듭니다.
                    em.persist(student); // 학생을 DB에 저장하도록 등록합니다.
                    em.persist(course); // 강좌를 DB에 저장하도록 등록합니다.
                    // 변경 사항을 DB에 반영하고 성공한 작업을 확정합니다.
                    em.getTransaction().commit(); // 커밋: 저장을 최종 확정합니다.
                    studentId = student.getId(); // DB가 매겨 준 학생 번호를 기록합니다.
                    courseId = course.getId(); // DB가 매겨 준 강좌 번호를 기록합니다.
                } catch (RuntimeException e) { // 오류가 나면
                    // 아직 진행 중인 트랜잭션만 롤백해 실패한 변경을 취소합니다.
                    if (em.getTransaction().isActive()) em.getTransaction().rollback(); // 작업을 되돌립니다.
                    throw e; // 오류를 다시 던져 프로그램을 멈춥니다.
                }
            }
            int enrollmentId; // 만들어진 수강 신청 번호를 담을 변수입니다.
            // 서비스가 한 요청의 조회·중복 검사·저장·커밋을 조정합니다.
            try (EntityManager em = emf.createEntityManager()) { // 새 EntityManager를 만듭니다.
                EnrollmentService service = new EnrollmentService(em, new JpaEnrollmentRepository(em)); // 리포지토리를 끼운 서비스를 만듭니다.
                enrollmentId = service.apply(studentId, courseId); // 첫 번째 수강 신청 → 성공해야 합니다.
                try {
                    service.apply(studentId, courseId); // 같은 신청을 한 번 더 시도합니다(중복).
                    throw new IllegalStateException("duplicate rejection expected"); // 여기까지 오면 중복이 안 막힌 것이므로 오류를 냅니다.
                } catch (IllegalStateException e) { // 중복 오류가 잡히면 정상입니다.
                    if (!"already enrolled".equals(e.getMessage())) throw e; // 예상한 메시지가 아니면 진짜 문제이니 다시 던집니다.
                    System.out.println("duplicate=" + e.getMessage()); // 중복이 잘 거절됐다고 화면에 출력합니다.
                }
            }
            // 관리 객체가 아닌 새 조회에서 처음 신청이 보존되었는지 확인합니다.
            try (EntityManager em = emf.createEntityManager()) { // 깨끗한 새 EntityManager로 DB에서 다시 읽습니다.
                Enrollment saved = em.find(Enrollment.class, enrollmentId); // 저장된 신청을 번호로 조회합니다.
                if (saved == null || saved.getStatus() != EnrollmentStatus.APPLIED) { // 없거나 상태가 "신청됨"이 아니면
                    throw new IllegalStateException("saved enrollment missing"); // 저장이 잘못된 것이므로 오류를 냅니다.
                }
                System.out.println("status=" + saved.getStatus()); // 현재 상태를 화면에 출력합니다.
            }
        }
    }
}
