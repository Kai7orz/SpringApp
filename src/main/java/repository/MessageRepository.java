package repository;

import core.message.Message;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class MessageRepository implements core.message.MessageRepository {
    // jdbc の DI 記述
    // interface の実装
    private final JdbcTemplate jdbcTemplate;

    public MessageRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void save(Message message) {
           this.jdbcTemplate.update(
                   "INSERT INTO message (message_id,user_id,content,createdAt) VALUES (?,?,?,?)",
                   message.getId(),message.getUserId(),message.getContent(),message.getCreatedAt());
      }

      public List<Message> getAllMessageByUser(Integer userId){
        // User によって Message 獲得の User 判定は何を気中にするべきか ?
        // User を特定する要素は，①id ② email （Mail 複数登録時に対応不可になる）Mail 変更時にここに紐づか
          // この関数をどこで使うかを考える
          // そのうえで前提として持っているデータが userId があるなら①が適切
          // なぜなら email や password によるユーザ特定は，FE -> BE でデータ漏れたら重大事故なのに対して userId ならもれても致命的にならないから

        // jdbcTemplateUpdate で SQL 発行する
        // wrap する処理
          List<Message> messages = this.jdbcTemplate.query(
                  "SELECT * FROM message WHERE user_id=?",new DataClassRowMapper<>(Message.class),userId
          );
        return messages;
    }

      public Optional<Message> getMessageById(Integer messageId) {
        try{
            Message message = this.jdbcTemplate.queryForObject(
                    "SELECT * FROM message WHERE message_id=?",
                    (rs,rowNum) -> {
                        return new Message(
                                rs.getObject("message_id",Integer.class),
                                rs.getObject("user_id",Integer.class),
                                rs.getObject("content",String.class),
                                rs.getObject("created_at", LocalDateTime.class)
                        );
                    }, messageId
            );
            return Optional.ofNullable(message);
        } catch(DataAccessException e){
            return Optional.empty();
        }
      }

}
