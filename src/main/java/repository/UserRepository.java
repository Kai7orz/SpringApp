package repository;

import org.apache.catalina.User;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

public class UserRepository implements core.user.UserRepository {
    // UserRepository interface へ定義したメソッドを実装する
    private final JdbcTemplate jdbcTemplate;

    public UserRepository(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void save(User user){
        jdbcTemplate()
    }


}
