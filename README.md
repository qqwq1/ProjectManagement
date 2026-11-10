# NauJava — практическая работа 5

Java 21, Maven, Spring Boot 4.1.1, PostgreSQL.

## Запуск

```sh
docker compose up -d postgres
mvn spring-boot:run
```

## Выполненные требования

1. Подключены Web MVC, Thymeleaf, Spring Data REST, HATEOAS и Springdoc.
   Spring Boot 4 использует `spring-boot-starter-webmvc` вместо прежнего starter-web.
   Springdoc 3.2.0 выбран для совместимости с Boot 4.1.1 (версия 2.6.0 из методички предназначена для Boot 3).
2. Все пять CRUD-репозиториев экспортированы через `@RepositoryRestResource`.
3. Собственный REST-контроллер расположен в отдельном пакете `web.controller`.
   Он использует оба запроса предыдущей работы; сервис получает репозиторий через конструктор.
4. Thymeleaf-шаблон `src/main/resources/templates/projectList.html` показывает таблицу проектов.
   HTML-контроллер поддерживает оба запроса поиска, пустую выдачу и экранирование данных.
5. Общий обработчик возвращает ошибки в формате `status`, `error`, `message`, `path`.
   Обработаны неверные параметры/JSON/данные (400), отсутствие ресурса (404),
   неподдерживаемый HTTP-метод (405), конфликт данных (409) и внутренние ошибки (500).
   Валидация сущностей выполняется перед созданием и сохранением через Spring Data REST.
6. Swagger UI и OpenAPI включают CRUD, связи и поиск репозиториев, а также собственный REST-контроллер.

## Адреса после запуска

- HTML-таблица: http://localhost:8080/projects
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- Репозитории: `/api/projects`, `/api/users`, `/api/projectParticipants`, `/api/projectTasks`, `/api/taskComments`.
- Поиск по точному названию и статусу: `/custom/projects/findByNameAndStatus?name=Платформа&status=ACTIVE`.
- Поиск по email участника: `/custom/projects/findByParticipantEmail?email=anna@example.org`.
- Один проект: `/custom/projects/{id}`.
- HTML-поиск: `/projects/findByNameAndStatus` и `/projects/findByParticipantEmail` с такими же параметрами.
- Поиск репозитория: `/api/projects/search`.

Возможные статусы проекта: `PLANNED`, `ACTIVE`, `COMPLETED`, `ARCHIVED`.
Создать проект можно POST-запросом на `/api/projects` с JSON `{"name":"Платформа","status":"ACTIVE"}`.
Связи при создании участника, задачи и комментария задаются URI соответствующего ресурса,
например `{"project":"/api/projects/1","user":"/api/users/1"}` для `/api/projectParticipants`.
Автоматическое заполнение рабочей БД демонстрационными данными не выполняется.



