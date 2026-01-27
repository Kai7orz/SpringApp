package org.example;

import com.zaxxer.hikari.HikariDataSource;
import core.message.Message;
import core.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import repository.MessageRepository;
import repository.UserRepository;
import service.UserService;

import javax.sql.DataSource;

//TIP コードを<b>実行</b>するには、<shortcut actionId="Run"/> を押すか
// ガターの <icon src="AllIcons.Actions.Execute"/> アイコンをクリックします。
@Configuration
@ComponentScan
public class Main {
    public static void main(String[] args) {
        // Datasauce で DB 接続のためのセットアップして DB コネクションを確立
        // JDBCTemplate などを用いて，FQL 発行
        // UserRepository を実装して，依存製注入しておく
        ApplicationContext context = new AnnotationConfigApplicationContext(Main.class);
        UserService userService = context.getBean(UserService.class);
        String TEST_USER_NAME = "test_user";
        String TEST_USER_EMAIL = "test@example.com";
        String TEST_USER_PASSWORD = "test_password";
        String TEST_CONTENT = "test_content";

        User testUser = new User(TEST_USER_NAME,TEST_USER_EMAIL,TEST_USER_PASSWORD);
        userService.saveUser(testUser);
        System.out.println("User:"+userService.getUserByMail(TEST_USER_EMAIL).getEmail());
    }

    @Bean
    public UserService userService(UserRepository userRepository) {
        return new UserService(userRepository);
    }

    @Bean
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setJdbcUrl("jdbc:mysql://127.0.0.1:3307/dev_database");
        dataSource.setUsername("root");
        dataSource.setPassword("dev_password");
        return dataSource;
    }

    @Bean
    public UserRepository userRepository(DataSource dataSource){
        return new UserRepository(dataSource);
    }

    @Bean
    public MessageRepository messageRepository(DataSource dataSource) { return new MessageRepository(dataSource);}
}