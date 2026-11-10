package ru.leshchev.NauJava.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.leshchev.NauJava.service.DuplicateUserException;
import ru.leshchev.NauJava.service.UserService;
import ru.leshchev.NauJava.web.dto.RegistrationForm;

@Controller
@RequiredArgsConstructor
public class AuthenticationController {
    private final UserService userService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/registration")
    public String registration(Model model) {
        model.addAttribute("registrationForm", new RegistrationForm());
        return "registration";
    }

    @PostMapping("/registration")
    public String register(@Valid @ModelAttribute RegistrationForm registrationForm, BindingResult errors) {
        if (errors.hasErrors()) {
            return "registration";
        }
        try {
            userService.addUser(registrationForm);
        } catch (DuplicateUserException | DataIntegrityViolationException exception) {
            errors.reject("duplicate", "Логин или email уже используется");
            return "registration";
        }
        return "redirect:/login?registered";
    }

}
