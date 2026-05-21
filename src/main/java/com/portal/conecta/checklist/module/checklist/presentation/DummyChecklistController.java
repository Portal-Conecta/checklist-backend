package com.portal.conecta.checklist.module.checklist.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checklists")
public class DummyChecklistController {

    @PostMapping("/executions")
    @PreAuthorize("@authService.canCreateExecution(#turmaId)")
    public ResponseEntity<String> createExecution(@RequestParam Long turmaId) {
        return ResponseEntity.ok("Execution created successfully for class: " + turmaId);
    }

    @GetMapping("/dashboard")
    @PreAuthorize("@authService.canViewDashboard()")
    public ResponseEntity<String> viewDashboard() {
        return ResponseEntity.ok("Dashboard data loaded successfully.");
    }

    @PutMapping("/completed")
    @PreAuthorize("@authService.canEditCompletedChecklist(#scope)")
    public ResponseEntity<String> editCompletedChecklist(@RequestParam String scope) {
        return ResponseEntity.ok("Completed checklist edited successfully in scope: " + scope);
    }

    @GetMapping("/operational")
    @PreAuthorize("@authService.hasOperationalAccess()")
    public ResponseEntity<String> getOperationalData() {
        return ResponseEntity.ok("Operational data retrieved successfully.");
    }
}
