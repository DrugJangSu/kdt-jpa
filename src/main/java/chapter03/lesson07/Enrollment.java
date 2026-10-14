package chapter03.lesson07; // 이 파일이 속한 패키지(폴더)입니다.

import jakarta.persistence.*; // JPA 기능을 한꺼번에 가져옵니다.
import java.time.LocalDateTime; // 날짜+시간을 다루는 Java 클래스를 가져옵니다.

@Entity // DB 테이블과 연결되는 엔티티입니다.
@Table(name = "enrollment", // 연결할 테이블 이름은 enrollment(수강 신청) 입니다.
        uniqueConstraints = @UniqueConstraint( // 중복을 막는 "유니크 제약 조건"을 겁니다.
                name = "uk_enrollment_student_course", // 제약 조건의 이름입니다.
                columnNames = {"student_id", "course_id"})) // 같은 학생+같은 강좌 조합은 한 번만 저장할 수 있습니다.
public class Enrollment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본키이며 DB가 번호를 자동으로 매깁니다.
    private Integer id; // 수강 신청 고유 번호입니다.

    @ManyToOne(fetch = FetchType.LAZY, optional = false) // 신청 여러 개가 학생 1명에 속하는 관계이며, 필요할 때만 조회(LAZY)하고 필수입니다.
    @JoinColumn(name = "student_id", nullable = false) // enrollment 테이블의 student_id 열(외래키)로 학생과 연결합니다.
    private Student student; // 이 신청을 한 학생입니다.

    @ManyToOne(fetch = FetchType.LAZY, optional = false) // 신청 여러 개가 강좌 1개에 속하는 관계이며, 필요할 때만 조회하고 필수입니다.
    @JoinColumn(name = "course_id", nullable = false) // course_id 열(외래키)로 강좌와 연결합니다.
    private Course course; // 신청 대상 강좌입니다.

    @Enumerated(EnumType.STRING) // enum 값을 숫자가 아닌 글자("APPLIED" 등)로 DB에 저장합니다.
    @Column(nullable = false, length = 20) // 비어 있으면 안 되고 최대 20글자입니다.
    private EnrollmentStatus status = EnrollmentStatus.APPLIED; // 처음 만들면 "신청됨" 상태로 시작합니다.

    @Version // 낙관적 락용 버전 번호입니다. 수정할 때마다 자동으로 1씩 올라가 동시 수정 충돌을 감지합니다.
    @Column(nullable = false) // 비어 있으면 안 됩니다.
    private Long version; // 현재 데이터의 버전 번호입니다.

    @Column(name = "created_at", nullable = false, updatable = false) // 만든 시각 열이며, 한번 저장하면 수정할 수 없습니다.
    private LocalDateTime createdAt; // 신청이 만들어진 시각입니다.

    @Column(name = "updated_at", nullable = false) // 수정 시각 열입니다.
    private LocalDateTime updatedAt; // 마지막으로 수정된 시각입니다.

    protected Enrollment() {} // JPA가 객체를 만들 때 쓰는 기본 생성자입니다.

    public Enrollment(Student student, Course course) { // 학생과 강좌를 받아 새 신청을 만드는 생성자입니다.
        if (student == null) throw new IllegalArgumentException("student required"); // 학생이 없으면 오류를 냅니다.
        if (course == null) throw new IllegalArgumentException("course required"); // 강좌가 없으면 오류를 냅니다.
        // 필수 관계를 확인한 뒤 FK를 관리할 두 참조를 저장합니다.
        this.student = student; // 학생을 이 신청에 연결합니다.
        this.course = course; // 강좌를 이 신청에 연결합니다.
    }

    public void approve() { // 신청을 승인하는 메서드입니다.
        // 이 예시는 승인으로 값을 변경해 버전 충돌을 검증합니다.
        status = EnrollmentStatus.APPROVED; // 상태를 "승인됨"으로 바꿉니다.
    }

    @PrePersist // DB에 처음 저장(INSERT)되기 직전에 자동으로 실행됩니다.
    private void onCreate() {
        // 생성 시점 하나로 두 시각의 초기값을 맞춥니다.
        LocalDateTime now = LocalDateTime.now(); // 지금 시각을 구합니다.
        createdAt = now; // 만든 시각을 지금으로 기록합니다.
        updatedAt = now; // 수정 시각도 처음에는 같은 값으로 맞춥니다.
    }

    @PreUpdate // DB에서 수정(UPDATE)되기 직전에 자동으로 실행됩니다.
    private void onUpdate() {
        // 실제 UPDATE에 사용할 수정 시각만 갱신합니다.
        updatedAt = LocalDateTime.now(); // 수정 시각을 지금으로 바꿉니다.
    }

    public Integer getId() { return id; } // 신청 번호를 꺼내 봅니다.
    public EnrollmentStatus getStatus() { return status; } // 현재 상태를 꺼내 봅니다.
    public Long getVersion() { return version; } // 현재 버전 번호를 꺼내 봅니다.
}
