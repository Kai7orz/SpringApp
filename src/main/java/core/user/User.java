package core.user;

import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Optional;

@EqualsAndHashCode(of = {"email", "password"})
public class User {
    // 純粋な Userデータを定義する メソッドは null　と 空 チェックにとどめる
    private Integer id;
    private Integer roleId;
    @Getter
    @NotBlank
    private String username;
    @Getter
    @NotBlank
    private String email;
    @Getter
    @NotBlank
    private String password;
    @Getter
    private LocalDateTime createdAt;
    @Getter
    private String roleName;

    public User(String username, String email, String password) {
        this(null, null, username, email, password, LocalDateTime.now(), null);
    }

    public User(Integer id, Integer roleId, String username, String email, String password) {
        this(id, roleId, username, email, password, LocalDateTime.now(), null);
    }

    public User(Integer id, Integer roleId, String username, String email, String password,
                LocalDateTime createdAt, String roleName) {
        this.id = id;
        this.roleId = roleId;
        this.username = username;
        this.email = email;
        this.password = password;
        this.createdAt = createdAt;
        this.roleName = roleName;
    }

    public Optional<Integer> getId() {
        return Optional.ofNullable(this.id);
    }

    public Optional<Integer> getRoleId() {
        return Optional.ofNullable(this.roleId);
    }

    public boolean isAdmin() {
        return "ADMIN".equals(this.roleName);
    }
}
