package chapter03.lesson07; // 이 파일이 속한 패키지(폴더)입니다.

import java.util.Optional; // "값이 있을 수도, 없을 수도 있음"을 표현하는 타입을 가져옵니다.

// 리포지토리 인터페이스: DB 접근에 필요한 기능의 "목록(약속)"만 적어 둡니다.
public interface EnrollmentRepository {
    void save(Enrollment enrollment); // 수강 신청을 저장합니다.
    boolean existsByStudentIdAndCourseId(int studentId, int courseId); // 같은 학생+강좌 신청이 이미 있는지 true/false로 알려 줍니다.
    Optional<Enrollment> findById(int id); // 번호로 신청을 찾습니다(없을 수 있음).
    Optional<Student> findStudentById(int id); // 번호로 학생을 찾습니다(없을 수 있음).
    Optional<Course> findCourseById(int id); // 번호로 강좌를 찾습니다(없을 수 있음).
}
