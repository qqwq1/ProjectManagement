package ru.leshchev.NauJava.web.controller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.leshchev.NauJava.dal.entity.ProjectStatus;
import ru.leshchev.NauJava.service.ProjectQueryService;

@Controller
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectViewController {
    private final ProjectQueryService service;

    @ModelAttribute("isAdmin")
    public boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("projects", service.findAll());
        return "projectList";
    }

    @GetMapping("/findByNameAndStatus")
    public String findByNameAndStatus(@RequestParam @NotBlank String name,
                                      @RequestParam ProjectStatus status, Model model) {
        model.addAttribute("projects", service.findByNameAndStatus(name, status));
        model.addAttribute("name", name);
        model.addAttribute("status", status);
        return "projectList";
    }

    @GetMapping("/findByParticipantEmail")
    public String findByParticipantEmail(@RequestParam @NotBlank @Email String email, Model model) {
        model.addAttribute("projects", service.findByParticipantEmail(email));
        model.addAttribute("email", email);
        return "projectList";
    }
}
