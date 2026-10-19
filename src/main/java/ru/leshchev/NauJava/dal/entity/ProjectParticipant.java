package ru.leshchev.NauJava.dal.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "project_participants", uniqueConstraints = @UniqueConstraint(
        name = "uk_project_user", columnNames = {"project_id", "user_id"}))
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class ProjectParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ParticipantRole role = ParticipantRole.MEMBER;

    @NotNull
    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt = Instant.now().truncatedTo(ChronoUnit.MICROS);

    @NotNull
    @Min(1)
    @Max(100)
    @Column(name = "allocation_percent", nullable = false)
    private Integer allocationPercent = 100;
}
