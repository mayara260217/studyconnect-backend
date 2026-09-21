package com.itb.inf3em.studyconnect.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

@Configuration
@Profile("local")
public class LocalDatabaseConfiguration {

    private static final Logger log = LoggerFactory.getLogger(LocalDatabaseConfiguration.class);

    @Bean
    public DataSource dataSource(
            @Value("${app.local.database.host:localhost}") String host,
            @Value("${app.local.database.port:1433}") int port,
            @Value("${app.local.database.name:StudyConnect}") String database,
            @Value("${spring.datasource.username:sa}") String username,
            @Value("${spring.datasource.password}") String password) {

        String masterUrl = "jdbc:sqlserver://" + host + ":" + port
                + ";databaseName=master;encrypt=true;trustServerCertificate=true";
        String appUrl = "jdbc:sqlserver://" + host + ":" + port
                + ";databaseName=" + database + ";encrypt=true;trustServerCertificate=true";

        // 1. Cria o banco se não existir
        try (Connection conn = DriverManager.getConnection(masterUrl, username, password);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("IF DB_ID(N'" + escapeLiteral(database)
                    + "') IS NULL CREATE DATABASE [" + escapeIdentifier(database) + "]");
            log.info("[DB] Banco '{}' verificado/criado.", database);
        } catch (Exception e) {
            throw new IllegalStateException("Nao foi possivel criar ou acessar o banco '" + database + "'.", e);
        }

        // 2. Cria tabelas se não existirem (verifica pela tabela Usuario)
        try (Connection conn = DriverManager.getConnection(appUrl, username, password);
             Statement stmt = conn.createStatement()) {

            boolean tabelasExistem;
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'Usuario'")) {
                rs.next();
                tabelasExistem = rs.getInt(1) > 0;
            }

            if (!tabelasExistem) {
                log.info("[DB] Tabelas nao encontradas. Executando schema.sql...");
                String schema = StreamUtils.copyToString(
                        new ClassPathResource("db/schema.sql").getInputStream(), StandardCharsets.UTF_8);
                for (String sql : schema.split(";")) {
                    String trimmed = sql.trim();
                    if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                        stmt.execute(trimmed);
                    }
                }
                log.info("[DB] Schema criado com sucesso.");
            } else {
                log.info("[DB] Tabelas ja existem.");
            }

        } catch (Exception e) {
            throw new IllegalStateException("Falha no bootstrap do schema local.", e);
        }

        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(appUrl);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        ds.setMaximumPoolSize(5);
        ds.setMinimumIdle(1);
        return ds;
    }

    private String escapeLiteral(String value) { return value.replace("'", "''"); }
    private String escapeIdentifier(String value) { return value.replace("]", "]]"); }
}
