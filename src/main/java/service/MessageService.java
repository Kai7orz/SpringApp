package service;

import core.message.Message;
import core.message.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {
    private MessageRepository messageRepository;

    @Autowired
    public MessageService(MessageRepository messageRepository){
        this.messageRepository = messageRepository;
    }

    public void save(Message message){
        this.messageRepository.save(message);
    }

    public List<Message> getAllMessageByUserId(Integer userId){
        return getAllMessageByUserId(userId);
    }
}
