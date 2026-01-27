package core.message;

import core.user.User;

import java.util.List;
import java.util.Optional;

public interface MessageRepository {
    void save(Message message);
    List<Message> getAllMessageByUser(Integer userId);
    Optional<Message> getMessageById(Integer messageId);
}
