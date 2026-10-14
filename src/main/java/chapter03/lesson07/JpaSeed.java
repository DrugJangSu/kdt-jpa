// [역할] 실습용 첫 데이터(회원 jpa01, 게시글 "JPA start")를 DB에 저장한다. 다른 실습보다 먼저 실행.
package chapter03.lesson07;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

// 실습에 쓸 첫 데이터(회원 1명, 게시글 1개)를 DB에 저장하는 파일. 다른 실습 파일보다 먼저 실행한다.
// 실행하면 jpa_member에 jpa01 회원, jpa_post에 "JPA start" 글이 들어간다.
public class JpaSeed {

    public static void main(String[] args) {
        // EntityManagerFactory: JPA 공장(앱에서 한 번만 만든다)
        // EntityManager: 공장에서 꺼낸 작업자. 저장/조회/삭제를 이 객체로 한다.
        // 둘 다 try (...)에 넣어서 끝나면 자동으로 닫는다.
        try (EntityManagerFactory emf = Persistence.createEntityManagerFactory("kdt");
             EntityManager em = emf.createEntityManager();
        ) {
            // 트랜잭션 시작: 여기부터 commit까지를 "한 묶음"으로 처리한다 (전부 성공하거나 전부 취소)
            em.getTransaction().begin();
            try {
                JpaMember member = new JpaMember("jpa01", "kim"); // 비영속: 그냥 자바 객체, JPA는 아직 모름
                em.persist(member); // 영속: JPA가 관리 시작, 저장 대상으로 등록

                JpaPost post = new JpaPost(member, "JPA start", "pure java"); // 비영속
                em.persist(post); // 영속

                // 커밋: 모아 둔 INSERT를 DB에 보내고 확정한다
                em.getTransaction().commit();

                // 같은 게시글을 두 번 조회한다. persist한 객체는 이미 1차 캐시(EntityManager 안의 보관함)에
                // 들어 있으므로, 두 번 모두 DB에 가지 않고 캐시의 같은 객체를 돌려준다.
                // 그래서 아래 비교는 true가 나온다. 같은 EntityManager 안에서는 같은 id면 같은 객체이기 때문이다.
                // (postId = 1은 새로 저장된 글의 id가 1이라고 가정한 값. 다르게 나오면 이 값을 바꿔야 한다)
                int postId = 1;
                JpaPost first = em.find(JpaPost.class, postId);
                JpaPost second = em.find(JpaPost.class, postId);
                System.out.println(first == second);

                // 저장된 뒤에는 DB가 부여한 id가 객체에 채워져 있다
                System.out.println("memberId = " + member.getId());
                System.out.println("postId = " + post.getId());
            } catch (RuntimeException e) {
                // 문제가 생기면 트랜잭션을 취소해서 반쯤 저장된 데이터가 남지 않게 한다
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e; // 원인을 볼 수 있도록 예외는 다시 던진다
            }
        }
    }
}
