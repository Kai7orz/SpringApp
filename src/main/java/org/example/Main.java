package org.example;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import repository.UserRepository;

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
}