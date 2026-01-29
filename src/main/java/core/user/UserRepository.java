package core.user;

import java.util.Optional;

public interface UserRepository {
    // find など User へのDB 操作をinterface として定義する
    void save(User user);
    Optional<User> findUserByMail(String email);
    Optional<User> findById(Integer id);
    boolean existsByEmail(String email);
}
