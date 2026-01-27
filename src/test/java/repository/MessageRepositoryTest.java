package repository;

import core.message.Message;
import core.message.MessageRepository;
import core.user.User;
import core.user.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.util.Assert;

import java.util.List;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@org.springframework.test.context.ContextConfiguration(classes = org.example.Main.class)
public class MessageRepositoryTest {

    // このテスト実行する前に sql fileで message table create するの忘れずに

    // MessageRepository Autowired
    // Message は User に紐づくから @BeforeEach で User 作成しINSERT しておく
    // DB から User を取得
    // 取得した User の userId を入れた状態で Message を生成
    // Message を INSERT
    // DB から Message を取得
    // assert
    // Message のために UserRepository を 注入するのは微妙な気もする（Message が UserRepository に依存していいのか）
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private TestEntityManager entityManager;

    private User testUser;
    private final static String TEST_USER_NAME = "test_user";
    private final static String TEST_USER_EMAIL = "test@example.com";
    private final static String TEST_USER_PASSWORD = "test_password";
    private final static String TEST_CONTENT = "test_content";
    @BeforeEach
    void setUp(){
        User user = new User(TEST_USER_NAME,TEST_USER_EMAIL,TEST_USER_PASSWORD);
        this.testUser = entityManager.persistFlushFind(user);
    }

    @Test
    public void should_save_message_success(){
        Integer userId = testUser.getId().orElseThrow();
        Message message = new Message(userId,TEST_CONTENT);
        this.messageRepository.save(message);
        List<Message> receivedMessage = this.messageRepository.getAllMessageByUser(userId);
        Assertions.assertFalse(receivedMessage.isEmpty(),"メッセージが取得できているか");
        Assertions.assertEquals(TEST_CONTENT,receivedMessage.get(0).getContent());
        Assertions.assertEquals(userId,receivedMessage.get(0).getUserId());
    }
}
