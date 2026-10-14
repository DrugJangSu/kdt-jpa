package chapter03.lesson07; // 이 파일이 속한 패키지(폴더)입니다.

import jakarta.persistence.EntityManager; // JPA에서 DB 작업을 담당하는 핵심 객체를 가져옵니다.

// 서비스: "수강 신청"이라는 업무의 순서와 규칙을 담당하는 클래스입니다.
public class EnrollmentService {
    private final EntityManager em; // 트랜잭션(시작/확정/취소)을 다루기 위한 EntityManager입니다.
    private final EnrollmentRepository enrollments; // DB 조회·저장을 맡기는 리포지토리입니다.

    public EnrollmentService(EntityManager em, EnrollmentRepository enrollments) { // 필요한 두 객체를 밖에서 받아서
        this.em = em; // EntityManager를 보관하고
        this.enrollments = enrollments; // 리포지토리를 보관합니다.
    }

    public int apply(int studentId, int courseId) { // 수강 신청을 처리하고, 만들어진 신청 번호를 돌려줍니다.
        // 트랜잭션을 시작해 조회와 변경을 하나의 작업으로 묶습니다.
        em.getTransaction().begin(); // 여기서부터 "전부 성공 or 전부 취소" 구간이 시작됩니다.
        try {
            // 일반적인 중복 요청은 INSERT 전에 이해하기 쉬운 오류로 거절합니다.
            if (enrollments.existsByStudentIdAndCourseId(studentId, courseId)) { // 이미 신청한 적이 있다면
                throw new IllegalStateException("already enrolled"); // "이미 신청됨" 오류를 냅니다.
            }
            // 조회 방법은 리포지토리에 위임하고 서비스는 결과 없음과 작업 순서를 조정합니다.
            Student student = enrollments.findStudentById(studentId) // 학생을 찾고
                    .orElseThrow(() -> new IllegalArgumentException("student not found")); // 없으면 "학생 없음" 오류를 냅니다.
            Course course = enrollments.findCourseById(courseId) // 강좌를 찾고
                    .orElseThrow(() -> new IllegalArgumentException("course not found")); // 없으면 "강좌 없음" 오류를 냅니다.

            Enrollment enrollment = new Enrollment(student, course); // 학생과 강좌를 묶어 새 신청 객체를 만듭니다.
            enrollments.save(enrollment); // 신청을 저장합니다.
            // 변경 사항을 DB에 반영하고 성공한 작업을 확정합니다.
            em.getTransaction().commit(); // 커밋: 지금까지의 작업을 DB에 최종 확정합니다.
            return enrollment.getId(); // 저장된 신청의 번호를 돌려줍니다.
        } catch (RuntimeException e) { // 위 과정에서 오류가 하나라도 나면
            // 아직 진행 중인 트랜잭션만 롤백해 실패한 변경을 취소합니다.
            if (em.getTransaction().isActive()) em.getTransaction().rollback(); // 롤백: 진행 중이던 작업을 모두 되돌립니다.
            // 실패한 작업에서 관리하던 객체를 분리해 다음 작업과 섞이지 않게 합니다.
            em.clear(); // EntityManager가 기억하던 객체들을 비웁니다.
            throw e; // 오류를 호출한 쪽에 다시 던져서 알립니다.
        }
    }
}
