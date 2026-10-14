// [역할] 회원 엔티티 (jpa_member 테이블). 로그인 아이디와 닉네임을 가진다.
package chapter03.lesson07;

import jakarta.persistence.*;

/**
 * 회원(Member) 엔티티. jpa_member 테이블의 행 하나를 객체로 표현한다.
 * 로그인 아이디(loginId)와 닉네임(nickname)을 가진다.
 */
@Entity // JPA가 관리하는 엔티티
@Table(name = "jpa_member") // 연결할 테이블 이름
public class JpaMember {

    // 기본키(PK). 값은 DB가 자동으로 부여한다 (AUTO_INCREMENT)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 로그인 아이디. DB 컬럼명은 login_id, 필수, 중복 불가(unique), 최대 30자
    @Column(name = "login_id", nullable = false, unique = true, length = 30)
    private String loginId;

    // 닉네임. 필수, 최대 30자 (중복은 허용)
    @Column(name = "nickname", nullable = false, length = 30)
    private String nickname;

    // JPA 전용 기본 생성자. DB에서 읽은 행을 객체로 만들 때 JPA가 사용한다.
    // 외부에서 빈 회원을 만들지 못하도록 protected
    protected JpaMember() {
    }

    // 회원 생성자. 값이 올바른지 검사한 뒤에만 객체를 만든다.
    public JpaMember(String loginId, String nickname) {
        // 아이디: 비어 있으면 안 되고 30자 이하
        if (loginId == null || loginId.isBlank()) {
            throw new IllegalArgumentException("loginId required");
        }
        if (loginId.length() > 30) {
            throw new IllegalArgumentException("loginId too long");
        }
        // 닉네임: 비어 있으면 안 되고 30자 이하
        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("nickname required");
        }
        if (nickname.length() > 30) {
            throw new IllegalArgumentException("nickname too long");
        }
        this.loginId = loginId;
        this.nickname = nickname;
    }

    // 값을 읽는 getter들
    public Integer getId() {
        return id;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getNickname() {
        return nickname;
    }

    // 닉네임 변경. 생성할 때와 같은 검증을 한다.
    // loginId를 바꾸는 메서드는 없다. 아이디는 한번 정하면 바꾸지 않는다는 설계다.
    public void changeNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("nickname required");
        }
        if (nickname.length() > 30) {
            throw new IllegalArgumentException("nickname too long");
        }
        this.nickname = nickname;


    }


}
