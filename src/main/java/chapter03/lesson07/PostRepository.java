// [역할] 게시글 저장소의 '기능 목록(약속)'만 적은 인터페이스. 실제 동작은 JpaPostRepository가 채운다.
package chapter03.lesson07;

import java.util.List;       // 여러 개를 담는 목록
import java.util.Optional;   // "있을 수도, 없을 수도 있는" 값을 담는 상자

// interface = "이런 기능들이 있어야 한다"는 약속(목록)만 적는 곳
// 메서드 이름과 모양만 있고, 실제로 어떻게 하는지({ } 안 내용)는 없다.
// 실제 동작은 나중에 이 인터페이스를 구현(implements)하는 클래스가 채운다.
//
// Repository = 게시글을 DB에 저장하고 꺼내 오는 '전담 창구'
public interface PostRepository {

    // 게시글 저장하기
    // 나중에 구현 클래스에서 em.persist(post)를 부르게 된다. (JpaSeed에서 하던 것)
    void save(JpaPost post);

    // 번호로 게시글 하나 찾기
    // 나중에 em.find(JpaPost.class, id)를 부르게 된다.
    // 그런데 그 번호의 글이 없을 수도 있다 → 그래서 그냥 JpaPost가 아니라 Optional로 감싸서 돌려준다.
    Optional<JpaPost> findById(int id);

    // 게시글 전부 가져오기
    // 나중에 "select p from JpaPost p order by p.id" 같은 JPQL을 쓰게 된다. (JpaPostQuery 1번)
    List<JpaPost> findAll();

    // 제목에 keyword가 '포함된' 게시글 찾기
    // 나중에 "where p.title like :keyword" + "%" + keyword + "%" 를 쓰게 된다. (JpaPostQuery 2번)
    List<JpaPost> findByTitleContaining(String keyword);

    List<JpaPost> findPage(int offset, int size);
}