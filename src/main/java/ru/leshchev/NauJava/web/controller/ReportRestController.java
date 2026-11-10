package ru.leshchev.NauJava.web.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.leshchev.NauJava.service.ReportService;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/custom/reports")
@RequiredArgsConstructor
public class ReportRestController {
    private final ReportService reports;

    @PostMapping
    public ResponseEntity<Map<String, Long>> create() {
        Long id = reports.createReport();
        reports.generateAsync(id);
        var location = ServletUriComponentsBuilder.fromCurrentRequestUri().path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.accepted().location(location).body(Map.of("id", id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable @Positive Long id) {
        var report = reports.getReport(id);
        return switch (report.getStatus()) {
            case CREATED -> ResponseEntity.accepted().body(Map.of("status", report.getStatus()));
            case ERROR -> ResponseEntity.internalServerError().body(Map.of("status", report.getStatus()));
            case COMPLETED -> ResponseEntity.ok()
                    .contentType(new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8))
                    .body(report.getContent());
        };
    }
}
