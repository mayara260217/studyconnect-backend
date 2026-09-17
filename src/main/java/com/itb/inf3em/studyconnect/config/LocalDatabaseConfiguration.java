package com.itb.inf3em.studyconnect.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/** Database bootstrap exclusivo do desenvolvimento local. */
@Configuration
@Profile("local")
public class LocalDatabaseConfiguration {

    @Bean
    public DataSource dataSource(
            @Value("${app.local.database.host:localhost}") String host,
            @Value("${app.local.database.port:1433}") int port,
            @Value("${app.local.database.name:StudyConnect}") String database,
            @Value("${spring.datasource.username:sa}") String username,
            @Value("${spring.datasource.password}") String password) {
        String masterUrl = "jdbc:sqlserver://" + host + ":" + port
                + ";databaseName=master;encrypt=true;trustServerCertificate=true";
        String applicationUrl = "jdbc:sqlserver://" + host + ":" + port
                + ";databaseName=" + database + ";encrypt=true;trustServerCertificate=true";

        try (Connection connection = DriverManager.getConnection(masterUrl, username, password);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("IF DB_ID(N'" + escapeLiteral(database)
                    + "') IS NULL CREATE DATABASE [" + escapeIdentifier(database) + "]");
        } catch (Exception exception) {
            throw new IllegalStateException("Nao foi possivel criar ou acessar o banco local '" + database + "'.", exception);
        }

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(applicationUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        dataSource.setMaximumPoolSize(5);
        dataSource.setMinimumIdle(1);
        return dataSource;
    }

    private String escapeLiteral(String value) {
        return value.replace("'", "''");
    }

    private String escapeIdentifier(String value) {
        return value.replace("]", "]]" );
    }
}
