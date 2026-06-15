package com.agenda.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/** Fábrica do pool de conexões HikariCP. */
public class DatabaseConfig {

    private DatabaseConfig() {}

    /** Cria e retorna um DataSource configurado para agenda_db. */
    public static HikariDataSource build() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/agenda_db");
        config.setUsername("agenda");
        config.setPassword("agenda123");
        config.setConnectionInitSql("SET search_path TO public");
        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(30_000);
        return new HikariDataSource(config);
    }
}
