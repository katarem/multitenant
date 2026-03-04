package com.example.demo.tenant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tenants")
public class TenantDemoController {

    private final TenantDemoService service;

    public TenantDemoController(TenantDemoService service) {
        this.service = service;
    }

    @GetMapping("/{tenantId}/probe")
    public TenantProbeResponse probe(@PathVariable String tenantId) {
        return service.probe(tenantId);
    }

    @PostMapping("/{tenantId}/notes")
    public TenantProbeResponse createNote(@PathVariable String tenantId, @RequestBody CreateNoteRequest request) {
        return service.createNote(tenantId, request.content());
    }

    @GetMapping("/{tenantId}/notes")
    public List<TenantNote> listNotes(@PathVariable String tenantId) {
        return service.listNotes(tenantId);
    }

    public record CreateNoteRequest(String content) {
    }
}
