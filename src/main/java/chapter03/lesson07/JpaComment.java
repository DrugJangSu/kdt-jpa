// [역할] 댓글 엔티티 (jpa_comment 테이블). 게시글·작성자와 연결되고, 공개된 글에만 댓글을 달 수 있다.
package chapter03.lesson07;

import jakarta.persistence.*;

/**
 * 댓글(Comment) 엔티티.
 * 엔티티란 "DB 테이블 한 줄(행)"을 자바 객체 하나로 표현한 클래스다.
 * 이 클래스의 객체 하나는 jpa_comment 테이블의 행 하나와 대응된다.
 *
 * 관계: 게시글 1개에 댓글 여러 개(N:1), 회원 1명이 댓글 여러 개(N:1).
 */
@Entity // "이 클래스는 DB 테이블과 연결되는 JPA 엔티티입니다"라는 표시
@Table(name = "jpa_comment") // 연결할 테이블 이름
public class JpaComment {

    // 기본키(PK): 행을 구분하는 고유 번호
    @Id
    // 번호는 우리가 정하지 않고 DB가 자동으로 1, 2, 3... 부여한다 (AUTO_INCREMENT)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 댓글 내용. 비어 있으면 안 되고(NOT NULL) 최대 500자
    @Column(nullable = false, length = 500)
    private String content;

    // 이 댓글이 달린 게시글. 댓글(N) : 게시글(1)
    // LAZY(지연 로딩): 댓글만 조회할 때 게시글은 바로 읽지 않고, 실제로 쓸 때 읽는다 (불필요한 조회 방지)
    // optional = false: 게시글 없는 댓글은 있을 수 없다
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    // jpa_comment 테이블의 post_id 컬럼이 게시글을 가리키는 외래키(FK)가 된다
    @JoinColumn(name = "post_id", nullable = false)
    private JpaPost post;

    // 이 댓글을 쓴 회원. 댓글(N) : 회원(1). 설정 의미는 위 post와 같다
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private JpaMember member;

    // JPA가 DB에서 읽은 데이터로 객체를 만들 때 쓰는 기본 생성자.
    // JPA 규칙상 꼭 필요하고, 우리 코드에서는 못 쓰게 protected로 막아 둔다.
    protected JpaComment(){}

    // 댓글을 새로 만드는 생성자. 잘못된 값이면 객체를 만들지 않고 예외를 던진다.
    public JpaComment(JpaPost post, JpaMember member, String content) {
        // 게시글, 작성자는 필수
        if (post == null) {
            throw new IllegalArgumentException("post required");
        }
        if (member == null) {
            throw new IllegalArgumentException("member required");
        }
        // 내용은 null, 빈 문자열, 공백뿐인 문자열이면 안 됨
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content required");
        }
        // DB 컬럼 길이(500자)를 넘기면 저장 단계에서 오류가 나므로 미리 막는다
        if (content.length() > 500) {
            throw new IllegalArgumentException("content too long");
        }
        // 공개(PUBLISHED)된 게시글에만 댓글을 달 수 있다
        if (!post.canReceiveComment()) {
            throw new IllegalStateException("post is not published");
        }
        // 검사를 모두 통과했을 때만 필드에 값을 넣는다
        this.post = post;
        this.member = member;
        this.content = content;
    }

    // 값을 꺼내 보는 getter. 수정용 setter는 일부러 만들지 않았다 (아래 edit만 허용)
    public Integer getId() { return id; }
    public JpaPost getPost() { return post; }
    public JpaMember getMember() { return member; }
    public String getContent() { return content; }

    // 댓글 내용 수정. 생성할 때와 같은 검증을 한다.
    // 영속 상태의 객체라면 값만 바꿔도 커밋할 때 JPA가 알아서 UPDATE를 보낸다.
    public void edit(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content required");
        }
        if (content.length() > 500) {
            throw new IllegalArgumentException("content too long");
        }
        this.content = content;
    }
}
