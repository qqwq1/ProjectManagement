package ru.leshchev.NauJava.dal.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String role = "USER";

    @Size(max = 64)
    @Column(unique = true, length = 64)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String username;

    @JsonIgnore
    @Size(max = 100)
    @Column(length = 100)
    private String password;

    @OneToMany(mappedBy = "user")
    private List<ProjectParticipant> participations = new ArrayList<>();

    @OneToMany(mappedBy = "author")
    private List<TaskComment> comments = new ArrayList<>();
}
