package ru.leshchev.NauJava.dal.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.leshchev.NauJava.dal.entity.User;

import java.util.Optional;

@RepositoryRestResource(path = "users", collectionResourceRel = "users")
public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByEmail(@Param("email") String email);
}
