package ru.leshchev.NauJava.dal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "reports")
@Getter
@Setter
@Accessors(chain = true)
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status = ReportStatus.CREATED;

    @Column(columnDefinition = "text")
    private String content;
}
