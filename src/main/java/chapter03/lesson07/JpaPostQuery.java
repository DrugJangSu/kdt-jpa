// [역할] JPQL로 게시글·회원을 조회하는 실습. 일반 조회, LIKE 검색, join, fetch join, 완전 일치 비교.
package chapter03.lesson07;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;

import java.util.List;

/**
 * 같은 게시글을 조건과 조인 방식만 바꿔 여러 번 조회한다.
 * 이 파일은 값을 고치지 않으므로 begin/commit이 없다.
 * Hibernate는 EntityManager가 열려 있으면 트랜잭션 없이도 SELECT를 실행하고,
 * 결과 엔티티를 이 EM의 영속성 컨텍스트에 영속 상태로 올린다.
 *
 * JPQL은 SQL이 아니다. 테이블/컬럼 이름이 아니라 엔티티 이름과 필드 이름을 쓴다.
 * "JpaPost"는 클래스 이름이고, p.title은 필드 title이다. DB 컬럼 title, login_id로 바꾸면 이 쿼리는 실패한다.
 * createQuery의 두 번째 인자는 select 절의 결과 타입이다. TypedQuery라 getResultList()가 List<JpaPost>로 돌아온다.
 *
 * JpaPost.member는 FetchType.LAZY다.
 * 게시글만 조회하면 member 자리에는 회원 본체가 아니라 프록시가 들어간다.
 * 프록시는 member_id(FK)만 알고 있고, getLoginId()처럼 실제 필드에 손댈 때 그제야 회원 SELECT가 나간다.
 * 이 추가 SELECT는 영속성 컨텍스트가 살아 있을 때만 가능하다. em.close() 이후에는 LazyInitializationException이 난다.
 * 게시글 N건의 회원을 하나씩 늦게 읽으면 쿼리가 1+N번이 된다. fetch join은 그 추가 조회를 처음 SELECT 한 번으로 합친다.
 *
 * 각 조회 앞의 em.clear()는 1차 캐시를 비운다.
 * 비우지 않으면 앞에서 이미 올려 둔 같은 id의 회원을 다음 조회가 재사용해서,
 * 조인 방식에 따라 SQL이 어떻게 달라지는지 콘솔에서 구분하기 어렵다.
 * clear된 엔티티는 준영속이다. 이 코드는 clear 이후 이전 리스트를 다시 쓰지 않는다.
 */
public class JpaPostQuery {

    public static void main(String[] args) {
        // LIKE에 넣을 가운데 글자. 바인딩 직전에 %를 붙여 "%JPA%"가 된다.
        String keyword = "JPA";
        // 제목 전체와 같아야 하는 값. JpaPostEdit가 저장해 둔 제목이다.
        String exactTitle = "JPA edited";
        // jpa_member.login_id. 컬럼은 unique라 이 값의 회원은 최대 한 명이다.
        String wantedLoginId = "jpa01";

        // 팩토리는 persistence.xml의 "kdt"를 읽고, EM은 비어 있는 영속성 컨텍스트를 만든다.
        // 닫는 순서는 역순이라 em.close()가 먼저다. 닫히면 남아 있던 영속 엔티티는 모두 준영속이 되고 지연 로딩도 끝난다.
        try (EntityManagerFactory emf = Persistence.createEntityManagerFactory("kdt");
             EntityManager em = emf.createEntityManager()) {
            // 1. 전체 목록.
            // select 대상은 게시글뿐이다. 제목만 출력하므로 getMember()를 호출하지 않고, 회원 SELECT도 나가지 않는다.
            // 조회된 각 JpaPost는 영속이고, member는 아직 초기화되지 않은 지연 프록시다.
            System.out.println("all");

            // "from JpaPost p"의 JpaPost는 엔티티다. SQL로 바뀌면 FROM jpa_post 가 된다.
            // order by p.id는 필드 id이며, DB에서는 PK 컬럼 정렬이 된다.
            // 이 호출은 SQL을 바로 보내지 않는다. 실행은 getResultList()다.
            TypedQuery<JpaPost> allQuery = em.createQuery(
                    "select p from JpaPost p order by p.id", JpaPost.class
            );

            // 컨텍스트에 없으므로 SELECT가 나가고, 행마다 영속 JpaPost를 만들어 1차 캐시에 id로 담는다.
            // 같은 EM에서 같은 id를 다시 찾으면 이 리스트 안의 그 인스턴스가 재사용된다.
            List<JpaPost> allPosts = allQuery.getResultList();
            System.out.println("allCount = " + allPosts.size());

            for (JpaPost post : allPosts) {
                // getId(), getTitle()은 이미 채워진 게시글 필드다. 프록시인 member에는 손대지 않는다.
                System.out.println(post.getId() + "|" + post.getTitle());
            }

            // 2. 제목 LIKE 검색. 결과의 작성자까지 출력한다.
            // clear로 1번에서 올린 게시글을 컨텍스트에서 떼어 낸다.
            // 안 그러면 같은 id는 DB를 다시 읽지 않고 캐시의 객체를 돌려줄 수 있다.
            em.clear();
            System.out.println("normal");
            // JPQL의 like와 SQL LIKE는 같다. :keyword는 이름 있는 파라미터다.
            // 값을 문자열에 이어 붙이지 않고 setParameter로 넘기므로, 검색어가 쿼리 구조를 바꾸지 못한다.
            // "%" + keyword + "%"는 자바에서 만든 뒤 통째로 바인딩된다. 전송되는 값은 "%JPA%"다.
            // join이 없으므로 SQL은 게시글 테이블만 읽고, member는 계속 지연 프록시다.
            List<JpaPost> normalPosts = em.createQuery(
                            "select p from JpaPost p " + "where p.title like :keyword order by p.id"
                            , JpaPost.class)
                    .setParameter("keyword", "%" + keyword + "%")
                    .getResultList();
            System.out.println("normalCount = " + normalPosts.size());
            // printPosts가 getMember().getLoginId()를 호출한다.
            // 프록시가 비어 있고 컨텍스트에도 그 회원이 없으면, 게시글마다 회원 SELECT가 추가로 나간다.
            // 여러 게시글의 member_id가 같으면 첫 로딩 이후에는 캐시의 같은 JpaMember를 쓴다.
            printPosts(normalPosts);

            // 3. 일반 조인.
            // join p.member m 은 SQL INNER JOIN이다. m은 회원 별칭이지만 where에서 쓰지 않는다.
            // 조인 조건으로 행을 거르는 일과, 회원 필드를 메모리에 채워 영속 엔티티로 만드는 일은 별개다.
            // select 절이 p뿐이면 하이버네이트는 게시글 컬럼만 읽고 member는 지연 프록시로 둔다.
            // 그래서 printPosts()에서는 2번과 같이 회원 SELECT가 다시 나갈 수 있다.
            // member는 nullable = false 라 회원 없는 게시글이 없고, inner join이어도 2번과 건수는 같다.
            em.clear();
            System.out.println("join");
            List<JpaPost> joinPosts = em.createQuery(
                            "select p from JpaPost p join p.member m where p.title like :keyword order by p.id"
                            , JpaPost.class)
                    .setParameter("keyword", "%" + keyword + "%")
                    .getResultList();

            System.out.println("joincount =" + joinPosts.size());
            printPosts(joinPosts);

            // 4. fetch join.
            // join fetch p.member 는 게시글 행과 회원 행을 한 SQL로 함께 읽는다.
            // 결과 JpaPost의 member는 프록시가 아니라 필드가 채워진 영속 JpaMember다.
            // 그 회원도 같은 영속성 컨텍스트에 id로 등록된다.
            // 연관이 ManyToOne이라 게시글 한 건이 회원 한 명과만 만나서, 게시글이 결과에서 중복되지 않는다.
            // printPosts()의 getLoginId()는 이미 채워진 필드를 읽으므로 회원 SELECT가 추가되지 않는다.
            em.clear();
            System.out.println("fetch");
            List<JpaPost> fetchPosts = em.createQuery(
                            "select p from JpaPost p join fetch p.member m where p.title like :keyword order by p.id"
                            , JpaPost.class)
                    .setParameter("keyword", "%" + keyword + "%")
                    .getResultList();

            System.out.println("fetchJoincount =" + fetchPosts.size());
            printPosts(fetchPosts);

            // 5. 제목 완전 일치.
            // = 는 LIKE와 달리 제목 전체가 exactTitle과 같아야 한다. "%JPA%"에 걸리던 다른 제목은 빠진다.
            // 여기서는 id와 title만 출력해서 getMember()를 호출하지 않는다. 회원 지연 로딩 SQL은 없다.
            em.clear();
            System.out.println("exact");
            List<JpaPost> exactPosts = em.createQuery(
                            "select p from JpaPost p where p.title = :title order by p.id",
                            JpaPost.class)
                    .setParameter("title", exactTitle)
                    .getResultList();
            System.out.println("exactCount=" + exactPosts.size());
            for (JpaPost post : exactPosts) {
                System.out.println(post.getId() + "|" + post.getTitle());
            }

            // 6. 회원 조회.
            // select 절이 m(JpaMember)이므로 결과 타입도 JpaMember.class여야 한다.
            // JpaPost.class로 받으면 쿼리 결과와 타입이 맞지 않아 실행 시 예외가 난다.
            // m.loginId는 필드 이름이다. SQL에서는 login_id 컬럼과 비교한다.
            em.clear();
            System.out.println("member");
            List<JpaMember> members = em.createQuery(
                            "select m from JpaMember m where m.loginId = :loginId",
                            JpaMember.class)
                    .setParameter("loginId", wantedLoginId)
                    .getResultList();
            System.out.println("memberCount=" + members.size());
            if (members.isEmpty()) {
                // getSingleResult()는 0건이면 예외다. 목록으로 받아서 없음을 직접 구분한다.
                System.out.println("member not found");
            } else {
                // login_id 컬럼이 unique라 이 조건의 회원은 두 명 이상일 수 없다.
                // members.get(0)도 영속 상태다. 이 메서드가 끝날 때 em.close()가 호출되면 준영속이 된다.
                JpaMember member = members.get(0);
                System.out.println(member.getLoginId());
            }
        }
    }

    private static void printPosts(List<JpaPost> posts) {
        // 호출 시점은 아직 try 안이라 EntityManager가 열려 있다.
        // 2번, 3번처럼 member가 지연 프록시이면 여기서 회원 SELECT가 나간다.
        // 4번처럼 fetch join으로 이미 채워졌으면 메모리 값을 읽고 SQL은 나가지 않는다.
        // 이 메서드를 em.close() 다음에 부르면, 초기화되지 않은 프록시에서 예외가 난다.
        for (JpaPost post : posts) {
            System.out.println(post.getId() + "|"
                    + post.getMember().getLoginId() + "|" + post.getTitle());
        }
    }
}