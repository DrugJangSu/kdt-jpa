package chapter03.lesson07;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.List;

public class JpaPageCheck {
    public static void main(String[] args) {
        try (EntityManagerFactory emf = Persistence.createEntityManagerFactory("kdt")) {
            int[] allIds = JpaPracticeData.prepare(emf, "page-check4-01", 3);
            int[] postIds = {allIds[1], allIds[2], allIds[3]};
            verifyPages(emf, postIds);
        }
    }

    private static void verifyPages(EntityManagerFactory emf, int[] preparedIds) {
        try (EntityManager em = emf.createEntityManager()) {
            PostRepository posts = new JpaPostRepository(em);

            // 준비한 세 게시글의 시작 위치를 구한 뒤 크기 1로 연속 조회합니다.
            long earlierCount = em.createQuery(
                            "select count(p) from JpaPost p where p.id < :firstId",
                            Long.class)
                    .setParameter("firstId", preparedIds[0])
                    .getSingleResult();
            int offset = Math.toIntExact(earlierCount);

            List<JpaPost> first = posts.findPage(offset, 1);
            List<JpaPost> second = posts.findPage(offset + 1, 1);
            List<JpaPost> third = posts.findPage(offset + 2, 1);

            // 각 페이지가 정확히 한 건이며 준비한 순서와 일치해야 중복·누락이 없습니다.
            if (first.size() != 1 || second.size() != 1 || third.size() != 1
                    || !first.get(0).getId().equals(preparedIds[0])
                    || !second.get(0).getId().equals(preparedIds[1])
                    || !third.get(0).getId().equals(preparedIds[2])) {
                throw new IllegalStateException("unexpected page contents");
            }
            System.out.println("pageIds=" + first.get(0).getId() + ","
                    + second.get(0).getId() + "," + third.get(0).getId());

            // 잘못된 범위는 리포지토리에서 즉시 거절해야 합니다.
            try {
                posts.findPage(-1, 1);
                throw new IllegalStateException("negative offset must fail");
            } catch (IllegalArgumentException e) {
                System.out.println("invalidOffset=" + e.getMessage());
            }
            try {
                posts.findPage(0, 0);
                throw new IllegalStateException("zero size must fail");
            } catch (IllegalArgumentException e) {
                System.out.println("invalidSize=" + e.getMessage());
            }
        }
    }
}