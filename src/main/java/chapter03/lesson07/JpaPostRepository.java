// [역할] PostRepository의 실제 구현체. EntityManager로 게시글을 DB에 저장·조회한다.
package chapter03.lesson07;

import jakarta.persistence.EntityManager; // JPA의 핵심 도구: DB 저장·조회를 대신 해 주는 관리자

import java.util.List;     // 여러 개를 담는 목록
import java.util.Optional; // "있을 수도, 없을 수도 있는" 값을 담는 상자

// PostRepository의 약속(메서드 목록)을 실제로 구현하는 클래스
public class JpaPostRepository implements PostRepository {

    // DB 작업을 맡길 EntityManager (한 번 받으면 바꾸지 못하게 final)
    private final EntityManager em;

    // 생성할 때 밖에서 EntityManager를 받아 저장해 둔다
    public JpaPostRepository(EntityManager em) {
        this.em = em;
    }

    // 인터페이스에 적힌 메서드를 구현한다는 표시
    @Override
    public void save(JpaPost post) {
        // persist = 새 게시글을 DB에 저장하도록 JPA에 등록 (실제 INSERT는 commit 때 나간다)
        em.persist(post);

    }

    @Override
    public Optional<JpaPost> findById(int id) {
        // em.find = 번호(PK)로 한 건 조회. 없으면 null이라서 Optional로 감싸 돌려준다
        return Optional.ofNullable(em.find(JpaPost.class, id));
    }

    @Override
    public List<JpaPost> findAll() {
        // JPQL: 테이블이 아닌 엔티티(JpaPost) 기준으로 쓰는 쿼리. 전부 가져와 id 순으로 정렬
        return em.createQuery(

                "select p from JpaPost p order by p.id",
                JpaPost.class)           // 결과를 JpaPost 타입으로 받는다
                .getResultList();        // 결과 여러 건을 List로 반환
    }

    @Override
    public List<JpaPost> findByTitleContaining(String keyword) {
        // 제목에 keyword가 들어간 글을 찾는 쿼리 (like 검색), id 순 정렬
        return em.createQuery(
                "select p from JpaPost p where p.title like :keyword order by p.id",
                JpaPost.class)
                // :keyword 자리에 값을 안전하게 넣는다. 앞뒤 %는 "어디에 있든 포함"이라는 뜻
                .setParameter("keyword", "%" + keyword + "%")
                .getResultList();
    }

    @Override
    public List<JpaPost> findPage(int offset, int size) {
        if (offset < 0 || size <= 0) {
            throw new IllegalArgumentException("invalid page range");
        }
        return em.createQuery("select p from JpaPost p order by p.id", JpaPost.class)
                .setFirstResult(offset)
                .setMaxResults(size)
                .getResultList();

    }
}
