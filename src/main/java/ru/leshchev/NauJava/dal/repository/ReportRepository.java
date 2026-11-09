package ru.leshchev.NauJava.dal.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.leshchev.NauJava.dal.entity.Report;

@RepositoryRestResource(exported = false)
public interface ReportRepository extends CrudRepository<Report, Long> {
}
