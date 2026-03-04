package com.example.demo.tenant;

public record TenantProbeResponse(
        String tenantId,
        String currentDatabase,
        String currentUser,
        Long insertedNoteId,
        Long notesInTenant
) {
}
