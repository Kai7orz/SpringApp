package service;

import core.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.UserRepository;

import java.util.Optional;

@Service
public class UserService {
    private UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public void saveUser(User user){
        this.userRepository.save(user);
    }

    public User getUserByMail(String email){
        Optional<User> user = this.userRepository.findUserByMail(email);
        return user.orElseThrow();
    }
}
