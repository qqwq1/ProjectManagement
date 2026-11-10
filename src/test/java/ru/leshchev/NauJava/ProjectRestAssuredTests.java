package ru.leshchev.NauJava;

import io.restassured.filter.cookie.CookieFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.leshchev.NauJava.dal.entity.Project;
import ru.leshchev.NauJava.dal.entity.ProjectParticipant;
import ru.leshchev.NauJava.dal.entity.ProjectStatus;
import ru.leshchev.NauJava.dal.entity.User;
import ru.leshchev.NauJava.dal.repository.ProjectParticipantRepository;
import ru.leshchev.NauJava.dal.repository.ProjectRepository;
import ru.leshchev.NauJava.dal.repository.UserRepository;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProjectRestAssuredTests {
    private static final String PROJECTS = "/custom/projects";
    private static final String USERNAME = "rest-assured-user";
    private static final String PASSWORD = "test-password";
    private static final String EMAIL = "rest-assured@example.org";
    @LocalServerPort int port;
    @Autowired ProjectRepository projects;
    @Autowired ProjectParticipantRepository participants;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder passwordEncoder;
    private CookieFilter cookies;
    private User user;
    private Project project;
    private ProjectParticipant participant;

    @BeforeEach
    void setUp() {
        cookies = new CookieFilter();
        user = users.save(new User().setName("REST").setLastName("Assured").setEmail(EMAIL)
                .setUsername(USERNAME).setPassword(passwordEncoder.encode(PASSWORD)));
        project = projects.save(new Project().setName("Проект REST Assured").setStatus(ProjectStatus.ACTIVE));
        participant = participants.save(new ProjectParticipant().setProject(project).setUser(user));

        String csrf = request().get("/login").then().statusCode(200).extract().htmlPath()
                .getString("**.find { it.@name == '_csrf' }.@value");
        request().formParam("username", USERNAME).formParam("password", PASSWORD).formParam("_csrf", csrf)
                .post("/login").then().statusCode(302).header("Location", org.hamcrest.Matchers.endsWith("/projects"));
    }

    @AfterEach
    void cleanUp() {
        if (participant != null) participants.deleteById(participant.getId());
        if (project != null) projects.deleteById(project.getId());
        if (user != null) users.deleteById(user.getId());
    }

    @Test
    void returnsProjectById() {
        request().get(PROJECTS + "/{id}", project.getId()).then()
                .statusCode(200).contentType(ContentType.JSON)
                .body("id", equalTo(project.getId().intValue()))
                .body("name", equalTo(project.getName()))
                .body("status", equalTo("ACTIVE"));
    }

    @Test
    void returnsNotFoundForUnknownId() {
        request().get(PROJECTS + "/{id}", Long.MAX_VALUE).then()
                .statusCode(404).contentType(ContentType.JSON)
                .body("status", equalTo(404))
                .body("message", notNullValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "9223372036854775808"})
    void rejectsInvalidId(String id) {
        request().get(PROJECTS + "/{id}", id).then()
                .statusCode(400).contentType(ContentType.JSON)
                .body("status", equalTo(400));
    }

    @Test
    void findsProjectByNameAndStatus() {
        request().queryParam("name", project.getName()).queryParam("status", "ACTIVE")
                .get(PROJECTS + "/findByNameAndStatus").then()
                .statusCode(200).body("$", hasSize(1))
                .body("[0].id", equalTo(project.getId().intValue()));
    }

    @Test
    void returnsEmptyListForUnmatchedStatus() {
        request().queryParam("name", project.getName()).queryParam("status", "ARCHIVED")
                .get(PROJECTS + "/findByNameAndStatus").then()
                .statusCode(200).body("$", hasSize(0));
    }

    @Test
    void findsProjectByParticipantEmail() {
        request().queryParam("email", EMAIL)
                .get(PROJECTS + "/findByParticipantEmail").then()
                .statusCode(200).body("$", hasSize(1))
                .body("[0].id", equalTo(project.getId().intValue()));
    }

    @Test
    void rejectsMalformedParticipantEmail() {
        request().queryParam("email", "invalid-email")
                .get(PROJECTS + "/findByParticipantEmail").then()
                .statusCode(400).body("status", equalTo(400));
    }

    private RequestSpecification request() {
        return given().baseUri("http://127.0.0.1").port(port).filter(cookies).redirects().follow(false);
    }
}
