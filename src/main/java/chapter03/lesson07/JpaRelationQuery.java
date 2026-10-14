// [역할] 연관관계(댓글-회원-게시글) 조회 실습. fetch join, count 집계, 상태 불일치(<>) 조회를 JPQL로 연습한다.
package chapter03.lesson07;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.List;

public class JpaRelationQuery {
    public static void main(String[] args) {
        int postId = 1;
        String content = "first comment";

        try (EntityManagerFactory emf = Persistence.createEntityManagerFactory("kdt");
             EntityManager em = emf.createEntityManager()) {

            // ───────── 문제 1 ─────────
            // JpaComment를 조회한다.
            // member와 post를 같은 조회에서 함께 읽어, 나중에 접근해도 추가 조회가 나가지 않게 한다.
            // 댓글의 게시글 id가 :postId와 같은 행만, 댓글 id 오름차순.
            List<JpaComment> comments = em.createQuery(
                            "select c from JpaComment c " +
                                    "join fetch c.member " +
                                    "join fetch c.post " +
                                    "where c.post.id = :postId " +
                                    "order by c.id",// ← 여기에 JPQL 작성
                            JpaComment.class)
                    .setParameter("postId", postId)
                    .getResultList();
            System.out.println("commentRows=" + comments.size());
            for (JpaComment comment : comments) {
                System.out.println(comment.getId() + "|"
                        + comment.getMember().getLoginId() + "|"
                        + comment.getPost().getId() + "|" + comment.getContent());
            }

            // ───────── 문제 2 ─────────
            // 게시글 id가 :postId이고 content가 :content와 같은 JpaComment의 개수.
            // 목록이 아니라 개수 하나만 반환한다.
            Long count = em.createQuery(
                            "select count(c) from JpaComment c where c.post.id = :postId and c.content = :content",// ← 여기에 JPQL 작성
                            Long.class)
                    .setParameter("postId", postId)
                    .setParameter("content", content)
                    .getSingleResult();
            System.out.println("matchingContentCount=" + count);

            // ───────── 문제 3 ─────────
            // JpaPost 중 status가 :excludedStatus와 다른 게시글만, 게시글 id 오름차순.
            List<JpaPost> notDraft = em.createQuery(
                            "select p from JpaPost p " +
                                    "where p.status <> :excludedStatus order by p.id",   // ← 여기에 JPQL 작성
                            JpaPost.class)
                    .setParameter("excludedStatus", PostStatus.DRAFT)
                    .getResultList();
            System.out.println("notDraftCount=" + notDraft.size());
        }
    }
}