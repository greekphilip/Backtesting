//package com.example.demo.config;
//
//import org.apache.commons.dbcp2.BasicDataSource;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.transaction.annotation.EnableTransactionManagement;
//
//import javax.sql.DataSource;
//
//@ComponentScan(basePackages = { "com.example.demo" })
//@EnableJpaRepositories(basePackages = "com.example.demo.repository")
//@EnableTransactionManagement
//public class AppConfig {
//
//    public DataSource dataSource() {
//        BasicDataSource dataSource = new BasicDataSource();
//        dataSource.setDriverClassName("org.postgresql.Driver");
//        dataSource.setUrl("jdbc:postgresql://localhost:5432/postgres");
//        dataSource.setUsername("postgres");
//        dataSource.setPassword("postgres");
//        dataSource.setInitialSize(10);
//        return dataSource;
//    }
//
//
//    @Bean
//    public JdbcTemplate jdbcTemplate() {
//        return new JdbcTemplate(dataSource());
//    }
//
//
//
//}
