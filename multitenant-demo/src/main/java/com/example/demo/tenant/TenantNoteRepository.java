package com.example.demo.tenant;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantNoteRepository extends JpaRepository<TenantNote, Long> {
}
