package ru.leshchev.NauJava.dal.repository;

import java.util.List;

public interface CrudRepository<T, ID> {
    void create(T entity);

    T read(ID id);

    List<T> readAll();

    void update(T entity);

    void delete(ID id);
}
