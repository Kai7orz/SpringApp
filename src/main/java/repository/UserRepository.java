package repository;

import core.user.User;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class UserRepository implements core.user.UserRepository {
    private final JdbcTemplate jdbcTemplate;

    private static final String SELECT_USER_WITH_ROLE =
            "SELECT u.id, u.role_id, u.username, u.email, u.password, u.created_at, r.role_name " +
            "FROM user u LEFT JOIN role r ON u.role_id = r.id ";

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
        return new User(
                rs.getObject("id", Integer.class),
                rs.getObject("role_id", Integer.class),
                rs.getString("username"),
                rs.getString("email"),
                rs.getString("password"),
                createdAt,
                rs.getString("role_name")
        );
    };

    public UserRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void save(User user) {
        if (user.getId().isEmpty()) {
            // 新規ユーザー登録
            if (user.getRoleId().isPresent()) {
                jdbcTemplate.update(
                        "INSERT INTO user (role_id, username, email, password, created_at) VALUES (?, ?, ?, ?, ?)",
                        user.getRoleId().get(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getPassword(),
                        Timestamp.valueOf(user.getCreatedAt() != null ? user.getCreatedAt() : LocalDateTime.now())
                );
            } else {
                jdbcTemplate.update(
                        "INSERT INTO user (username, email, password, created_at) VALUES (?, ?, ?, ?)",
                        user.getUsername(),
                        user.getEmail(),
                        user.getPassword(),
                        Timestamp.valueOf(user.getCreatedAt() != null ? user.getCreatedAt() : LocalDateTime.now())
                );
            }
        } else {
            // 既存ユーザー更新
            jdbcTemplate.update(
                    "UPDATE user SET role_id = ?, username = ?, email = ?, password = ? WHERE id = ?",
                    user.getRoleId().orElse(null),
                    user.getUsername(),
                    user.getEmail(),
                    user.getPassword(),
                    user.getId().get()
            );
        }
    }

    @Override
    public Optional<User> findUserByMail(String email) {
        try {
            User user = jdbcTemplate.queryForObject(
                    SELECT_USER_WITH_ROLE + "WHERE u.email = ?",
                    userRowMapper,
                    email
            );
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (DataAccessException e) {
            System.out.println("error: " + e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findById(Integer id) {
        try {
            User user = jdbcTemplate.queryForObject(
                    SELECT_USER_WITH_ROLE + "WHERE u.id = ?",
                    userRowMapper,
                    id
            );
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (DataAccessException e) {
            System.out.println("error: " + e);
            return Optional.empty();
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM user WHERE email = ?",
                    Integer.class,
                    email
            );
            return count != null && count > 0;
        } catch (DataAccessException e) {
            return false;
        }
    }
}
