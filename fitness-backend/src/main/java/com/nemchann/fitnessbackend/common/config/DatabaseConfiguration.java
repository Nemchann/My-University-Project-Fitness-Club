package com.nemchann.fitnessbackend.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//Тут подправить, потому что файл application.yaml, а не application.properties
@Component
public class DatabaseConfiguration {
    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Bean
    public Connection connection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}
