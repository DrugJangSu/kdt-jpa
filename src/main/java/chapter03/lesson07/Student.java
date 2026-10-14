package chapter03.lesson07; // 이 파일이 속한 패키지(폴더)입니다.

import jakarta.persistence.*; // JPA 기능(@Entity, @Id 등)을 한꺼번에 가져옵니다.

@Entity // 이 클래스를 DB 테이블과 연결되는 "엔티티"로 만듭니다.
@Table(name = "student") // 연결할 DB 테이블 이름은 student 입니다.
public class Student {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) // id는 기본키이며, DB가 자동으로 1, 2, 3... 번호를 매깁니다.
    private Integer id; // 학생을 구분하는 고유 번호입니다.

    @Column(nullable = false, length = 50) // 비어 있으면 안 되고(null 불가), 최대 50글자인 열입니다.
    private String name; // 학생 이름입니다.

    protected Student() {} // JPA가 객체를 만들 때 쓰는 기본 생성자입니다(외부에서는 못 쓰게 protected).

    public Student(String name) { // 이름을 받아 새 학생을 만드는 생성자입니다.
        if (name == null || name.isBlank()) // 이름이 없거나 공백뿐이면
            throw new IllegalArgumentException("student name required"); // "이름 필수" 오류를 냅니다.
        // 매핑 길이는 Java 입력 검증을 대신하지 않으므로 저장 전에 검사합니다.
        if (name.length() > 50) throw new IllegalArgumentException("student name too long"); // 50글자를 넘으면 오류를 냅니다.
        this.name = name; // 검사를 통과한 이름을 이 객체의 name 필드에 저장합니다.
    }

    public Integer getId() { return id; } // 학생 번호를 꺼내 보는 getter 메서드입니다.
}
