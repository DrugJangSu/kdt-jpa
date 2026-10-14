package chapter03.lesson07;

import java.util.List;

public interface CommentRepository {
    void save(JpaComment comment);
    List<JpaComment> findByPostId(int PostId);
    long countByPostId(int postId);

}
