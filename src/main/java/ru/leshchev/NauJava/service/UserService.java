package ru.leshchev.NauJava.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.leshchev.NauJava.dal.entity.User;
import ru.leshchev.NauJava.dal.repository.UserRepository;
import ru.leshchev.NauJava.web.dto.RegistrationForm;
import java.util.Optional;

@Service
@Validated
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public Optional<User> findByUsername(String username) {
        return repository.findByUsername(username);
    }

    @Transactional
    public User addUser(@Valid RegistrationForm form) {
        String username = form.getUsername();
        String email = form.getEmail();
        if (repository.findByUsername(username).isPresent() || repository.findByEmail(email).isPresent()) {
            throw new DuplicateUserException();
        }
        return repository.save(new User()
                .setUsername(username)
                .setPassword(passwordEncoder.encode(form.getPassword()))
                .setName(form.getName().strip())
                .setLastName(form.getLastName().strip())
                .setEmail(email)
                .setRole("USER"));
    }
}
