package ru.leshchev.NauJava.dal.repository;

import org.springframework.data.repository.CrudRepository;
import ru.leshchev.NauJava.dal.entity.User;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
