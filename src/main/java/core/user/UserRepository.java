package core.user;

public interface UserRepository {
    // find など User へのDB 操作をinterface として定義する
    void save(User user);
    User findUserByMail(String mail);
}
