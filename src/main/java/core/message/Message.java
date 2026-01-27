package core.message;

import lombok.Getter;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@Repository
public class Message {
    private Integer messageId;
    private Integer userId;
    @Getter
    private String content;
    @Getter
    private LocalDateTime createdAt;

    public Message(String content) {
        this(null,null,content,LocalDateTime.now());
    }

    public Message(Integer userId,String content){
        this(null,userId,content,LocalDateTime.now());
    }

    public Message(Integer messageId,Integer userId,String content,LocalDateTime createdAt){
        this.messageId = messageId;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Optional<Integer> getId() {return Optional.ofNullable(this.messageId);}
    public Optional<Integer> getUserId() {return Optional.ofNullable(this.userId);}
}
