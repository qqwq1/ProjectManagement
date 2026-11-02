package ru.leshchev.NauJava.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.leshchev.NauJava.dal.entity.ProjectStatus;
import ru.leshchev.NauJava.service.ProjectQueryService;
import ru.leshchev.NauJava.web.dto.ProjectResponse;
import java.util.List;

@RestController
@RequestMapping("/custom/projects")
@RequiredArgsConstructor
@Tag(name = "Project queries", description = "Custom queries from practical work 4")
public class ProjectRestController {
    private final ProjectQueryService service;

    @GetMapping("/findByNameAndStatus")
    @Operation(summary = "Find projects by exact name and status")
    public List<ProjectResponse> findByNameAndStatus(@RequestParam @NotBlank String name,
                                                    @RequestParam ProjectStatus status) {
        return service.findByNameAndStatus(name, status);
    }

    @GetMapping("/findByParticipantEmail")
    @Operation(summary = "Find projects by participant email")
    public List<ProjectResponse> findByParticipantEmail(@RequestParam @NotBlank @Email String email) {
        return service.findByParticipantEmail(email);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one project")
    public ProjectResponse findById(@PathVariable @Positive Long id) {
        return service.findById(id);
    }
}
