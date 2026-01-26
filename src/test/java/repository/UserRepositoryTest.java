package repository;

import core.user.User;
import core.user.UserRepository;
import org.example.Main;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;
@JdbcTest
@Import(repository.UserRepository.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@org.springframework.test.context.ContextConfiguration(classes = org.example.Main.class)
public class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    private User user;
    private final static String TEST_USER_NAME = "test_user";
    private final static String TEST_USER_EMAIL = "test@example.com";
    private final static String TEST_USER_PASSWORD = "test_password";
    @BeforeEach
    void setUp() {
        user = new User(TEST_USER_NAME,TEST_USER_EMAIL,TEST_USER_PASSWORD);
    }

    @Test
    public void should_save_user_success() {
     this.userRepository.save(this.user);
     User receivedUser = this.userRepository.findUserByMail(TEST_USER_EMAIL).orElse(new User("fake","fake","fake"));
        System.out.println("Equals result: " + this.user.equals(receivedUser));
        System.out.println("User Email: [" + this.user.getEmail() + "]");
        System.out.println("Received Email: [" + receivedUser.getEmail() + "]");
     Assertions.assertEquals(receivedUser,this.user);
    }
}
