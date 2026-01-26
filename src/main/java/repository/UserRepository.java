package repository;

import core.user.User;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Optional;

@Repository
public class UserRepository implements core.user.UserRepository {
    // UserRepository interface へ定義したメソッドを実装する
    private final JdbcTemplate jdbcTemplate;

    public UserRepository(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void save(User user){
        if(!user.getId().isPresent() && !user.getRoleId().isPresent()){
            jdbcTemplate.update("INSERT INTO user (username,email,password) VALUES (?,?,?)",user.getUsername(),user.getEmail(),user.getPassword());
        }
        else if(user.getRoleId().isPresent()){
            jdbcTemplate.update("INSERT INTO user VALUES (?,?,?,?)",user.getRoleId().get(),user.getUsername(),user.getEmail(),user.getPassword());
        }
        else{
            jdbcTemplate.update("UPDATE user SET role_id=?, username=?, email=?, password=? WHERE id=?",user.getRoleId().get(),user.getUsername(),user.getEmail(),user.getPassword(),user.getId().get());
        }
    }

    public Optional<User> findUserByMail(String email) {
            try{
                User user = jdbcTemplate.queryForObject(
                        "SELECT * FROM user WHERE email = ?",
                        (rs, rowNum) -> {
                            return new User(
                                    rs.getObject("id", Integer.class),
                                    rs.getObject("role_id", Integer.class),
                                    rs.getString("username"),
                                    rs.getString("email"),
                                    rs.getString("password")
                            );
                        },
                        email
                );
                return Optional.ofNullable(user);
            } catch(DataAccessException e){
                return Optional.empty();
            }
    }
}
