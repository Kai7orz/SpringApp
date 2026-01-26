package core.user;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository {
    // find など User へのDB 操作をinterface として定義する
    void save(User user);
    Optional<User> findUserByMail(String email);
}
