package chapter03.lesson07; // 이 파일이 속한 패키지(폴더)입니다.

import jakarta.persistence.EntityManager; // JPA에서 DB 작업을 담당하는 핵심 객체를 가져옵니다.
import java.util.Optional; // "값이 있을 수도, 없을 수도 있음"을 표현하는 타입을 가져옵니다.

// EnrollmentRepository의 약속을 JPA로 실제 구현한 클래스입니다.
public class JpaEnrollmentRepository implements EnrollmentRepository {
    private final EntityManager em; // DB 작업에 사용할 EntityManager입니다(한번 정하면 바꾸지 않음).

    public JpaEnrollmentRepository(EntityManager em) { // 생성할 때 EntityManager를 받아서
        this.em = em; // 필드에 보관합니다.
    }

    @Override // 인터페이스에 적어 둔 메서드를 구현한다는 표시입니다.
    public void save(Enrollment enrollment) {
        // 새 신청을 영속화합니다. IDENTITY라면 이 호출에서 INSERT할 수 있습니다.
        em.persist(enrollment); // 신청 객체를 JPA가 관리하도록 등록해 DB에 저장합니다.
    }

    @Override
    public boolean existsByStudentIdAndCourseId(int studentId, int courseId) {
        // 학생·강좌 조합 전체를 집계해 중복 여부만 반환합니다.
        Long count = em.createQuery( // JPQL(객체용 SQL)로 개수를 세는 쿼리를 만듭니다.
                        "select count(e) from Enrollment e " // Enrollment 신청의 개수를 세되
                                + "where e.student.id = :studentId and e.course.id = :courseId", // 학생 번호와 강좌 번호가 모두 일치하는 것만 셉니다.
                        Long.class) // 결과는 Long(숫자) 타입입니다.
                .setParameter("studentId", studentId) // :studentId 자리에 실제 학생 번호를 넣습니다.
                .setParameter("courseId", courseId) // :courseId 자리에 실제 강좌 번호를 넣습니다.
                .getSingleResult(); // 결과 한 개(개수)를 받아옵니다.
        return count > 0; // 1개 이상이면 이미 신청한 것이므로 true를 돌려줍니다.
    }

    @Override
    public Optional<Enrollment> findById(int id) {
        // 결과 없음도 반환 타입으로 표현합니다.
        return Optional.ofNullable(em.find(Enrollment.class, id)); // 번호로 찾고, 없으면 빈 Optional을 돌려줍니다.
    }

    @Override
    public Optional<Student> findStudentById(int id) {
        // 연결 엔티티를 생성할 실제 학생 조회도 저장소가 담당합니다.
        return Optional.ofNullable(em.find(Student.class, id)); // 번호로 학생을 찾고, 없으면 빈 Optional입니다.
    }

    @Override
    public Optional<Course> findCourseById(int id) {
        // 학생·강좌 조회는 현재 실습의 수강 신청 저장소로 함께 모읍니다.
        return Optional.ofNullable(em.find(Course.class, id)); // 번호로 강좌를 찾고, 없으면 빈 Optional입니다.
    }
}
