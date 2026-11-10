package ru.leshchev.NauJava;

import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.leshchev.NauJava.dal.entity.User;
import ru.leshchev.NauJava.dal.repository.*;
import ru.leshchev.NauJava.security.AppUserDetailsService;
import ru.leshchev.NauJava.service.UserService;
import ru.leshchev.NauJava.web.dto.RegistrationForm;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityIntegrationTests {
    @LocalServerPort int port;
    @Autowired UserRepository users;
    @Autowired ProjectRepository projects;
    @Autowired ProjectParticipantRepository participants;
    @Autowired ProjectTaskRepository tasks;
    @Autowired TaskCommentRepository comments;
    @Autowired UserService userService;
    @Autowired AppUserDetailsService detailsService;
    @Autowired PasswordEncoder passwordEncoder;
    private HttpTestClient guest;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        guest = new HttpTestClient(port);
    }

    @AfterEach
    void cleanDatabase() {
        comments.deleteAll();
        tasks.deleteAll();
        participants.deleteAll();
        projects.deleteAll();
        users.deleteAll();
    }

    @Test
    void anonymousUsersCanOpenFormsButProtectedResourcesRedirectToLogin() throws Exception {
        assertThat(guest.get("/login").statusCode()).isEqualTo(200);
        assertThat(guest.get("/registration").statusCode()).isEqualTo(200);
        assertThat(guest.get("/login").body()).contains("autocomplete=\"current-password\"", "/registration");
        for (String path : new String[]{"/", "/projects", "/api/projects", "/api/users",
                "/custom/projects/findByParticipantEmail?email=a@example.org", "/swagger-ui/index.html",
                "/swagger-ui.html", "/v3/api-docs", "/v3/api-docs.yaml"}) {
            assertRedirect(guest.get(path), "/login");
        }
    }

    @Test
    void registrationPersistsHashedPasswordAndAlwaysAssignsUserRole() throws Exception {
        var fields = new HashMap<>(registration("new.user", "new@example.org"));
        fields.put("role", "ADMIN");
        fields.put("id", "1");
        guest.csrf("/registration");
        assertRedirect(guest.form("/registration", fields, true), "/login?registered");
        User user = userService.findByUsername("new.user").orElseThrow();
        assertThat(user.getUsername()).isEqualTo("new.user");
        assertThat(user.getEmail()).isEqualTo("new@example.org");
        assertThat(user.getRole()).isEqualTo("USER");
        assertThat(user.getPassword()).startsWith("$2").isNotEqualTo("Password123!");
        assertThat(passwordEncoder.matches("Password123!", user.getPassword())).isTrue();
        assertRedirect(guest.login("new.user", "Password123!"), "/projects");
        assertThat(guest.get("/projects").statusCode()).isEqualTo(200);
        assertThat(guest.get("/api/projects").statusCode()).isEqualTo(200);
        assertThat(guest.get("/projects").body()).contains("Выйти").doesNotContain("Документация API");
    }

    @Test
    void wrongCredentialsAndUnknownUsersCannotAuthenticate() throws Exception {
        account("member", "USER");
        assertRedirect(guest.login("member", "wrong-password"), "/login?error");
        assertRedirect(guest.get("/projects"), "/login");
        assertRedirect(guest.login("missing", "Password123!"), "/login?error");
        assertThat(guest.get("/login?error").body()).contains("Неверный логин или пароль");
        assertThatThrownBy(() -> detailsService.loadUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void onlyAdminCanAccessSwaggerAndOpenApi() throws Exception {
        account("member", "USER");
        account("administrator", "ADMIN");
        var member = new HttpTestClient(port);
        assertRedirect(member.login("member", "Password123!"), "/projects");
        String[] adminPaths = {"/swagger-ui/index.html", "/swagger-ui/swagger-ui-bundle.js",
                "/swagger-ui.html", "/v3/api-docs", "/v3/api-docs/swagger-config", "/v3/api-docs.yaml"};
        for (String path : adminPaths) assertThat(member.get(path).statusCode()).as(path).isEqualTo(403);
        assertThat(member.get("/api/users").statusCode()).isEqualTo(200);
        assertThat(member.json("PATCH", "/api/users/" + users.findByUsername("member").orElseThrow().getId(),
                "{\"role\":\"ADMIN\",\"username\":\"replacement\",\"password\":\"replacement\"}").statusCode()).isEqualTo(200);
        assertThat(users.findByUsername("member").orElseThrow().getRole()).isEqualTo("USER");
        var admin = new HttpTestClient(port);
        assertRedirect(admin.login("administrator", "Password123!"), "/projects");
        for (String path : adminPaths) {
            if (path.equals("/swagger-ui.html")) assertThat(admin.get(path).statusCode()).isEqualTo(302);
            else assertThat(admin.get(path).statusCode()).as(path).isEqualTo(200);
        }
        assertThat(admin.get("/projects").body()).contains("Документация API");
        assertThat(admin.get("/api/users").body()).doesNotContain("password", "$2", "Password123!");
        assertThat(detailsService.loadUserByUsername("administrator").getAuthorities())
                .extracting(authority -> authority.getAuthority()).containsExactly("ROLE_ADMIN");
    }

    @Test
    void logoutInvalidatesTheAuthenticatedSession() throws Exception {
        account("member", "USER");
        assertRedirect(guest.login("member", "Password123!"), "/projects");
        guest.csrf("/projects");
        assertThat(guest.form("/logout", Map.of(), false).statusCode()).isEqualTo(403);
        assertThat(guest.get("/projects").statusCode()).isEqualTo(200);
        assertRedirect(guest.form("/logout", Map.of(), true), "/login?logout");
        assertRedirect(guest.get("/projects"), "/login");
        assertRedirect(guest.get("/api/projects"), "/login");
    }

    @Test
    void csrfProtectsRegistrationLoginAndRepositoryWrites() throws Exception {
        assertThat(guest.form("/registration", registration("member", "member@example.org"), false).statusCode()).isEqualTo(403);
        assertThat(users.count()).isZero();
        account("member", "USER");
        assertThat(guest.form("/login", Map.of("username", "member", "password", "Password123!"), false).statusCode()).isEqualTo(403);
        assertRedirect(guest.login("member", "Password123!"), "/projects");
        assertThat(guest.send("POST", "/api/projects", "{\"name\":\"Blocked\"}", "application/json", false).statusCode()).isEqualTo(403);
        assertThat(projects.count()).isZero();
        assertThat(guest.json("POST", "/api/projects", "{\"name\":\"Allowed\"}").statusCode()).isEqualTo(201);
    }

    @Test
    void duplicateUsernameAndEmailAreRejectedWithoutChangingTheExistingAccount() throws Exception {
        User original = account("member", "USER");
        for (var fields : List.of(registration("member", "other@example.org"), registration("another", "member@example.org"))) {
            guest.csrf("/registration");
            var response = guest.form("/registration", fields, true);
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).contains("Логин или email уже используется").doesNotContain("Password123!");
            assertThat(users.count()).isEqualTo(1);
            assertThat(users.findByUsername("member").orElseThrow().getPassword()).isEqualTo(original.getPassword());
        }
    }

    @Test
    void invalidRegistrationAndOversizedUtf8PasswordsDoNotCreateAccounts() throws Exception {
        for (var changes : List.of(Map.of("username", " "), Map.of("password", ""),
                Map.of("email", "invalid"), Map.of("name", " "), Map.of("password", "я".repeat(40)))) {
            var fields = new HashMap<>(registration("member", "member@example.org"));
            fields.putAll(changes);
            guest.csrf("/registration");
            var response = guest.form("/registration", fields, true);
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).contains("class=\"error\"");
            assertThat(users.count()).isZero();
        }
    }

    @Test
    void legacyUsersWithoutCredentialsCannotAuthenticate() {
        users.save(new User().setName("Existing").setLastName("Person").setEmail("existing@example.org"));
        assertThat(users.count()).isEqualTo(1);
        assertThatThrownBy(() -> detailsService.loadUserByUsername("existing"))
                .isInstanceOf(UsernameNotFoundException.class);
    }


    @Test
    void htmlFormsAcceptTheirRenderedCsrfParameter() throws Exception {
        var fields = new HashMap<>(registration("member", "member@example.org"));
        fields.put("_csrf", guest.csrf("/registration"));
        assertRedirect(guest.form("/registration", fields, false), "/login?registered");
        assertRedirect(guest.form("/login", Map.of("username", "member", "password", "Password123!",
                "_csrf", guest.csrf("/login")), false), "/projects");
        assertRedirect(guest.form("/logout", Map.of("_csrf", guest.csrf("/projects")), false), "/login?logout");
    }



    private User account(String username, String role) {
        var form = new RegistrationForm();
        form.setUsername(username);
        form.setPassword("Password123!");
        form.setEmail(username + "@example.org");
        form.setName("Test");
        form.setLastName("User");
        User user = userService.addUser(form);
        user.setRole(role);
        return users.save(user);
    }

    private Map<String, String> registration(String username, String email) {
        return Map.of("username", username, "password", "Password123!", "name", "Анна", "lastName", "Иванова", "email", email);
    }

    private void assertRedirect(HttpResponse<String> response, String target) {
        assertThat(response.statusCode()).as(response.body()).isEqualTo(302);
        assertThat(response.headers().firstValue("Location").orElseThrow()).endsWith(target);
    }
}
