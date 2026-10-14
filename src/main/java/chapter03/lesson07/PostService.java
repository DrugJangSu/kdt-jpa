package chapter03.lesson07;

import jakarta.persistence.EntityManager;

public class PostService {
    private final EntityManager em;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public PostService(EntityManager em,
                       MemberRepository memberRepository,
                       PostRepository postRepository,
                       CommentRepository commentRepository) {
        this.em = em;
        this.memberRepository = memberRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    public void publish(int postId) {
        // 1. 조회·도메인 처리·저장을 한 작업 단위로 묶기 위해 트랜잭션을 시작합니다.
        em.getTransaction().begin();
        try {
            // 2. 대상 게시글을 먼저 조회하고, 없으면 예외로 중단해 이후 처리를 실행하지 않습니다.
            JpaPost post = postRepository.findById(postId)
                    .orElseThrow(() -> new IllegalArgumentException("post not found"));
            // 3. 조회한 영속 게시글에서 DRAFT → PUBLISHED 규칙을 실행해 변경 감지로 반영합니다.
            post.publish();
            // 4. 도메인 처리와 저장 요청이 모두 성공한 뒤 변경을 커밋으로 확정합니다.
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            // 실패하면 아직 활성인 트랜잭션만 롤백해 이 요청의 DB 변경을 취소합니다.
            if (em.getTransaction().isActive()) {
                // 활성 여부를 확인했으므로 이미 끝난 트랜잭션에 롤백을 요청하지 않습니다.
                em.getTransaction().rollback();
            }
            // 실패한 작업의 객체를 이후 조회에 재사용하지 않도록 영속성 컨텍스트를 비웁니다.
            em.clear();
            // 실패 원인을 호출 측에 전달하여 정상 완료로 처리하지 않게 합니다.
            throw e;
        }
    }

    public void addComment(int postId, int memberId, String content) {
        // 1. 조회·도메인 처리·저장을 한 작업 단위로 묶기 위해 트랜잭션을 시작합니다.
        em.getTransaction().begin();
        try {
            // 2. 대상 게시글을 먼저 조회하고, 없으면 예외로 중단해 이후 처리를 실행하지 않습니다.
            JpaPost post = postRepository.findById(postId)
                    .orElseThrow(() -> new IllegalArgumentException("post not found"));
            // 3. 댓글 작성자 회원을 조회하며 없으면 저장 전에 예외를 발생시킵니다.
            JpaMember member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("member not found"));
            // 4. 댓글 생성자가 게시글 공개 상태와 필수 관계·내용을 검사한 뒤 객체를 만듭니다.
            JpaComment comment = new JpaComment(post, member, content);
            // 5. 생성자 검증을 통과한 댓글만 리포지토리에 전달해 저장을 요청합니다.
            commentRepository.save(comment);
            // 6. 도메인 처리와 저장 요청이 모두 성공한 뒤 변경을 커밋으로 확정합니다.
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            // 실패하면 아직 활성인 트랜잭션만 롤백해 이 요청의 DB 변경을 취소합니다.
            if (em.getTransaction().isActive()) {
                // 활성 여부를 확인했으므로 이미 끝난 트랜잭션에 롤백을 요청하지 않습니다.
                em.getTransaction().rollback();
            }
            // 실패한 작업의 객체를 이후 조회에 재사용하지 않도록 영속성 컨텍스트를 비웁니다.
            em.clear();
            // 실패 원인을 호출 측에 전달하여 정상 완료로 처리하지 않게 합니다.
            throw e;
        }
    }
}