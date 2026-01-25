import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@Profile("prod")
public class MyDevConfig {
    @Bean
    public DataSource dataSource() {
        //spring.datasource.url=jdbc:h2:mem:testdb
        //spring.datasource.driver-class-name=org.h2.Driver
        //spring.datasource.username=sa
        //spring.datasource.password=
        //# H2コンソールをブラウザで見れるようにする設定
        //spring.h2.console.enabled=true
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3307:dev_database");
        dataSource.setUsername("root");
        dataSource.setPassword("dev_password");
        return dataSource;
    }
}