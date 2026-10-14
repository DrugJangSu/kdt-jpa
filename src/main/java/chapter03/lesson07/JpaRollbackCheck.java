// [역할] 롤백(rollback)하면 DB 저장은 취소되지만 자바 객체의 id는 그대로 남는다는 것을 확인하는 실습.
package chapter03.lesson07;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

// 목표: "rollback하면 DB는 되돌아가지만, Java 객체는 안 되돌아간다"를 직접 확인한다.
public class JpaRollbackCheck {
    public static void main(String[] args) {

        // 공장(emf)은 하나만 만든다. 작업자(EntityManager)는 이 공장에서 두 번 꺼내 쓴다.
        try (EntityManagerFactory emf = Persistence.createEntityManagerFactory("kdt")) {

            // 저장할 때 받은 번호를 담아 둘 변수.
            // 아래 두 번째 작업자도 이 번호로 조회해야 하니까 블록 '바깥'에 선언한다.
            Integer assignedId;

            // ════════════ 1. 첫 번째 작업자: 저장했다가 취소하기 ════════════
            // 작업자 em을 꺼낸다. 이 작업자는 자기 전용 서랍(1차 캐시)을 가진다.
            // try (...) 덕분에 블록이 끝나면 em은 자동으로 닫힌다.
            try (EntityManager em = emf.createEntityManager()) {

                // 트랜잭션 시작: 여기부터 '한 묶음'. 끝에 확정(commit)하거나 취소(rollback)한다.
                em.getTransaction().begin();

                try {
                    // ── TODO 1: 저장하고 번호 출력 ──

                    // 회원 객체 생성 → 비영속 (JPA는 아직 모름, id = null)
                    JpaMember member = new JpaMember("mission-01-rollback", "rollbackUser");

                    // 서랍에 넣기 → 영속
                    // IDENTITY라서 번호를 받으려고 이 줄에서 바로 INSERT가 실행된다.
                    // 하지만 아직 commit 전이라 '임시저장' 상태일 뿐, 확정은 아니다.
                    em.persist(member);

                    // commit 전인데도 번호가 이미 들어 있다. (방금 INSERT로 받아 왔으니까)
                    assignedId = member.getId();
                    System.out.println("assignedId=" + assignedId);

                    // ── TODO 2: 일부러 취소하고, 객체의 번호 다시 출력 ──

                    // rollback → 묶음 전체 취소. 방금 실행된 INSERT도 DB에서 없던 일이 된다.
                    em.getTransaction().rollback();

                    // 그런데 rollback은 'DB만' 되돌린다. Java 객체는 손대지 않는다.
                    // 그래서 member 객체 안의 번호는 그대로 남아 있다.
                    System.out.println("idAfterRollback=" + member.getId());

                } catch (RuntimeException e) {
                    // 안전장치: rollback 줄에 도착하기 전에 에러가 나면 여기로 온다.
                    // 트랜잭션이 아직 열려 있으면 닫고(취소하고) 나간다.
                    // "열었으면 어떤 경우에도 닫고 나간다"는 습관.
                    if (em.getTransaction().isActive()) {
                        em.getTransaction().rollback();
                    }
                    // 무슨 에러였는지 콘솔에서 보이도록 다시 던진다.
                    throw e;
                }
            }   // 첫 번째 작업자 닫힘 → 서랍도 같이 사라짐

            // ════════════ 2. 두 번째 작업자: DB에 진짜 없는지 확인하기 ════════════
            // ── TODO 3: 새 작업자로 같은 번호 조회 ──

            // 왜 '새' 작업자를 쓰나?
            // 새 작업자 = 텅 빈 새 서랍. 서랍에 아무것도 없으니 find()가 무조건 DB에 가서 확인한다.
            // → 서랍이 아니라 'DB의 진짜 상태'를 보는 것.
            try (EntityManager verifyEm = emf.createEntityManager()) {

                // 조회만 하니까 트랜잭션(begin)은 열지 않는다. 바꾸는 게 없어서 확정할 것도 없음.
                // 해당 번호의 회원이 DB에 없으면 find()는 null을 돌려준다.
                JpaMember found = verifyEm.find(JpaMember.class, assignedId);

                // found != null → 있으면 true, 없으면 false
                System.out.println("existsAfterRollback=" + (found != null));
            }
        }   // 공장 닫힘
    }
}