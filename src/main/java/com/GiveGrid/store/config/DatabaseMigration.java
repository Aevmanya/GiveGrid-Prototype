package com.GiveGrid.store.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Small compatibility migrations for older GiveGrid databases.
 */
@Component
public class DatabaseMigration implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        // The organisation description can now contain up to 3000 characters.
        // Explicitly resize the existing MySQL column because older databases
        // may still have the original VARCHAR(255) definition.
        try {
            jdbcTemplate.execute(
                    "ALTER TABLE users MODIFY COLUMN organisation_description VARCHAR(3000) NULL"
            );
        } catch (Exception ignored) {
            // Keep startup resilient if this database has not created the
            // profile column yet; Hibernate ddl-auto=update will handle it.
        }

        // Email is contact/profile information, not the login identifier.
        // Remove any legacy UNIQUE index on users.email.
        try {
            List<Map<String, Object>> indexes = jdbcTemplate.queryForList(
                    "SHOW INDEX FROM users WHERE Column_name = 'email' AND Non_unique = 0"
            );

            for (Map<String, Object> index : indexes) {
                String indexName = String.valueOf(index.get("Key_name"));
                if (!"PRIMARY".equalsIgnoreCase(indexName)) {
                    jdbcTemplate.execute("ALTER TABLE users DROP INDEX \`" + indexName + "\`");
                }
            }
        } catch (Exception ignored) {
            // Do not prevent the application from starting if the database
            // user cannot alter indexes or the schema is not yet available.
        }
    }
}
