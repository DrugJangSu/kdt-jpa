package chapter03.lesson07; // 이 파일이 속한 패키지(폴더)입니다.

import jakarta.persistence.*; // JPA 기능(@Entity, @Id 등)을 한꺼번에 가져옵니다.

@Entity // 이 클래스를 DB 테이블과 연결되는 "엔티티"로 만듭니다.
@Table(name = "course") // 연결할 DB 테이블 이름은 course 입니다.
public class Course {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) // id는 기본키이며, DB가 자동으로 번호를 매깁니다.
    private Integer id; // 강좌를 구분하는 고유 번호입니다.

    @Column(nullable = false, length = 100) // 비어 있으면 안 되고, 최대 100글자인 열입니다.
    private String title; // 강좌 제목입니다.

    protected Course() {} // JPA가 객체를 만들 때 쓰는 기본 생성자입니다.

    public Course(String title) { // 제목을 받아 새 강좌를 만드는 생성자입니다.
        if (title == null || title.isBlank()) // 제목이 없거나 공백뿐이면
            throw new IllegalArgumentException("course title required"); // "제목 필수" 오류를 냅니다.
        // DB 열 길이와 같은 상한을 Java에서도 명시합니다.
        if (title.length() > 100) throw new IllegalArgumentException("course title too long"); // 100글자를 넘으면 오류를 냅니다.
        this.title = title; // 검사를 통과한 제목을 이 객체에 저장합니다.
    }

    public Integer getId() { return id; } // 강좌 번호를 꺼내 보는 getter 메서드입니다.
}
