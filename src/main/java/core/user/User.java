package core.user;

import ch.qos.logback.core.util.StringUtil;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.beans.ConstructorProperties;
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

    public User (String username,String email,String password) {
        this(null,null,username,email,password);
    }

   public User (Integer id,Integer roleId,String username,String email,String password) {
        this.id = id;
        this.roleId = roleId;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public Optional<Integer> getId(){
        return Optional.ofNullable(this.id);
    }

    public Optional<Integer> getRoleId(){
        return Optional.ofNullable(this.roleId);
    }
}
