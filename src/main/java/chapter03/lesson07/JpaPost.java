// [역할] 게시글 엔티티 (jpa_post 테이블). 제목·본문·작성자·상태(초안/공개/보관)를 가진다.
package chapter03.lesson07;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 게시글(Post) 엔티티. jpa_post 테이블의 행 하나를 객체로 표현한다.
 * 게시글은 작성자(회원) 1명을 가지고, 상태(초안/공개/보관)를 가진다.
 *
 * 상태 흐름: DRAFT(초안) → PUBLISHED(공개) → ARCHIVED(보관)
 */
@Entity // JPA가 관리하는 엔티티
@Table(name = "jpa_post") // 연결할 테이블 이름
public class JpaPost {

    // 기본키(PK). DB가 자동으로 번호를 부여한다 (AUTO_INCREMENT)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 제목: 필수, 최대 200자
    @Column(name = "title", length = 200, nullable = false)
    private String title;

    // 본문: 필수, 최대 1000자
    @Column(name = "body", length = 1000, nullable = false)
    private String body;

    // 작성자. 게시글(N) : 회원(1)
    // LAZY: 게시글만 읽을 때 회원은 바로 읽지 않고, getMember()로 실제 값을 쓸 때 읽는다
    // 외래키 컬럼은 member_id, 작성자 없는 게시글은 불가
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private JpaMember member;

    // 이 게시글에 달린 댓글 목록. 게시글(1) : 댓글(N)
    // mappedBy = "post": 외래키는 JpaComment의 post 필드가 관리하고, 여기서는 그걸 '읽기용'으로 가리킨다
    // cascade = ALL: 게시글을 저장/삭제하면 딸린 댓글도 같이 저장/삭제된다
    // orphanRemoval = true: 목록에서 뺀 댓글(주인 잃은 댓글)은 DB에서도 삭제된다
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    // 처음엔 빈 목록으로 시작한다 (null이면 add할 때 오류가 나므로)
    private List<JpaComment> comments = new ArrayList<>();

    // 댓글 목록 꺼내기. 읽기 전용으로 감싸서 돌려줘, 밖에서 add/remove로 마음대로 못 바꾸게 막는다
    public List<JpaComment> getComments() {
        return Collections.unmodifiableList(comments);
    }

    // 댓글 추가하기: 이 게시글의 댓글이 맞을 때만 목록에 넣는다
    public void addComment(JpaComment comment) {
        // 댓글이 null이거나, 댓글이 가리키는 게시글이 내가 아니면 잘못된 요청이라 예외를 던진다
        if (comment == null || comment.getPost() != this) {
            throw new IllegalArgumentException("comment post mismatch");
        }
        // 검사를 통과하면 목록에 추가
        comments.add(comment);
    }

    // 댓글 삭제하기: 목록에서 빼면 orphanRemoval 덕분에 DB에서도 삭제된다
    public void removeComment(JpaComment comment) {
        comments.remove(comment);
    }

    // 게시글 상태. 새 글은 기본값이 DRAFT(초안)
    // EnumType.STRING: DB에 "DRAFT" 같은 글자로 저장한다.
    // (기본값 ORDINAL은 0,1,2 숫자로 저장해서, enum 순서가 바뀌면 데이터가 꼬이므로 STRING을 쓴다)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostStatus status = PostStatus.DRAFT;

    @Version
    @Column(name = "version")
    private Long version;

    // 공개하기: 초안일 때만 가능하다
    public void publish() {
        if (status != PostStatus.DRAFT) {
            throw new IllegalStateException("only draft can be published");
        }
        status = PostStatus.PUBLISHED;
    }

    // 보관하기: 이미 보관 중이면 또 할 수 없다
    public void archive() {
        if (status == PostStatus.ARCHIVED) {
            throw new IllegalStateException("already archived");
        }
        status = PostStatus.ARCHIVED;
    }

    // 댓글을 받을 수 있는지: 공개된 글만 true (JpaComment 생성자가 이 값을 확인한다)
    public boolean canReceiveComment() {
        return status == PostStatus.PUBLISHED;
    }

    // JPA 전용 기본 생성자. DB에서 읽은 행을 객체로 만들 때 JPA가 사용한다.
    protected JpaPost() {

    }

    // 게시글 생성자. 검증을 통과해야 객체가 만들어진다. 상태는 기본값 DRAFT가 된다.
    public JpaPost(JpaMember member, String title, String body) {
        if (member == null) throw new IllegalArgumentException("member required");
        validate(title, body);
        this.member = member;
        this.title = title;
        this.body = body;
    }

    // 제목/본문 검증을 한곳에 모은 메서드. 생성자와 edit가 같이 사용한다.
    // static: 객체 없이도 호출 가능, private: 이 클래스 안에서만 사용
    private static void validate(String title, String body) {
        if (title == null || title.isBlank())
            throw new IllegalArgumentException("title required");
        if (title.length() > 200)
            throw new IllegalArgumentException("title too long");
        if (body == null || body.isBlank())
            throw new IllegalArgumentException("body required");
        if (body.length() > 1000)
            throw new IllegalArgumentException("body too long");
    }

    // 값을 읽는 getter들
    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public JpaMember getMember() {
        return member;
    }

    public PostStatus getStatus() {
        return status;
    }

    public Long getVersion() {
        return version;
    }

    // 제목과 본문 수정. 검증 후 값을 바꾼다.
    // 영속 상태 객체라면 커밋할 때 JPA가 변경을 감지(dirty checking)해 UPDATE를 보낸다.
    public void edit(String title, String body) {
        validate(title, body);
        this.title = title;
        this.body = body;
    }

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    private void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}



