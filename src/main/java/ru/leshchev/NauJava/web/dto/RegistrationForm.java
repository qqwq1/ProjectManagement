package ru.leshchev.NauJava.web.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.nio.charset.StandardCharsets;

@Getter
@Setter
public class RegistrationForm {
    @NotBlank(message = "Укажите логин")
    @Size(max = 64, message = "Логин не должен превышать 64 символа")
    private String username;

    @NotBlank(message = "Укажите пароль")
    @Size(max = 72, message = "Пароль не должен превышать 72 символа")
    private String password;

    @NotBlank(message = "Укажите имя")
    @Size(max = 100, message = "Имя не должно превышать 100 символов")
    private String name;

    @NotBlank(message = "Укажите фамилию")
    @Size(max = 100, message = "Фамилия не должна превышать 100 символов")
    private String lastName;

    @NotBlank(message = "Укажите email")
    @Email(message = "Укажите корректный email")
    @Size(max = 320, message = "Email не должен превышать 320 символов")
    private String email;

    @AssertTrue(message = "Пароль не должен превышать 72 байта в UTF-8")
    public boolean isPasswordWithinByteLimit() {
        return password == null || password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }
}
