package ru.leshchev.NauJava.dal.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "app_users", uniqueConstraints = @UniqueConstraint(name = "uk_user_email", columnNames = "email"))
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank
    @Size(max = 100)
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @NotBlank
    @Email
    @Size(max = 320)
    @Column(nullable = false, length = 320)
    private String email;

    @Min(0)
    @Max(150)
    private Integer age;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String role = "MEMBER";

    @OneToMany(mappedBy = "user")
    private List<ProjectParticipant> participations = new ArrayList<>();

    @OneToMany(mappedBy = "author")
    private List<TaskComment> comments = new ArrayList<>();
}
