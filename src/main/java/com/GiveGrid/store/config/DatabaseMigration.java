package com.GiveGrid.store.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * The email address is contact/profile information, not the login identifier.
 * Older versions of the schema created a UNIQUE index on users.email. Remove
 * that legacy index so profile edits are not rejected by the database.
 */
@Component
public class DatabaseMigration implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            List<Map<String, Object>> indexes = jdbcTemplate.queryForList(
                    "SHOW INDEX FROM users WHERE Column_name = 'email' AND Non_unique = 0"
            );

            for (Map<String, Object> index : indexes) {
                String indexName = String.valueOf(index.get("Key_name"));
                if (!"PRIMARY".equalsIgnoreCase(indexName)) {
                    jdbcTemplate.execute("ALTER TABLE users DROP INDEX `" + indexName + "`");
                }
            }
        } catch (Exception ignored) {
            // Do not prevent the application from starting if the database
            // user cannot alter indexes or the schema is not yet available.
        }
    }
}
