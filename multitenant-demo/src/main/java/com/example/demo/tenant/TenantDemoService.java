package com.example.demo.tenant;

import com.katarem.multitenant.TenantContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Supplier;

@Service
public class TenantDemoService {

    private final TenantNoteRepository repository;
    private final JdbcTemplate jdbcTemplate;

    public TenantDemoService(TenantNoteRepository repository, JdbcTemplate jdbcTemplate) {
        this.repository = repository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public TenantProbeResponse createNote(String tenantId, String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content is required");
        }
        return withTenant(tenantId, () -> {
            ensureTableExists();
            TenantNote saved = repository.save(new TenantNote(content.trim()));
            return new TenantProbeResponse(
                    tenantId,
                    currentDatabase(),
                    currentUser(),
                    saved.getId(),
                    repository.count()
            );
        });
    }

    @Transactional(readOnly = true)
    public List<TenantNote> listNotes(String tenantId) {
        return withTenant(tenantId, repository::findAll);
    }

    @Transactional(readOnly = true)
    public TenantProbeResponse probe(String tenantId) {
        return withTenant(tenantId, () -> new TenantProbeResponse(
                tenantId,
                currentDatabase(),
                currentUser(),
                null,
                null
        ));
    }

    private <T> T withTenant(String tenantId, Supplier<T> action) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId is required");
        }

        TenantContext.set(tenantId);
        try {
            return action.get();
        } finally {
            TenantContext.clear();
        }
    }

    private void ensureTableExists() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS tenant_notes (
                    id BIGSERIAL PRIMARY KEY,
                    content VARCHAR(255) NOT NULL,
                    created_at TIMESTAMP WITH TIME ZONE NOT NULL
                )
                """);
    }

    private String currentDatabase() {
        return jdbcTemplate.queryForObject("select current_database()", String.class);
    }

    private String currentUser() {
        return jdbcTemplate.queryForObject("select current_user", String.class);
    }
}
