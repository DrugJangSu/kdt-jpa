package chapter03.lesson07;

import java.util.Optional;

public interface MemberRepository {

    Optional<JpaMember> findById(int id);
}
