package ru.leshchev.NauJava;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import ru.leshchev.NauJava.dal.entity.*;
import ru.leshchev.NauJava.dal.repository.*;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

/** Full HTTP integration, with real repositories and a running servlet container. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebIntegrationTests {
    @LocalServerPort int port;
    @Autowired ProjectRepository projects;
    @Autowired UserRepository users;
    @Autowired ProjectParticipantRepository participants;
    @Autowired ProjectTaskRepository tasks;
    @Autowired TaskCommentRepository comments;
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();
    private Long projectId;
    private Long userId;

    @BeforeEach
    void seed() {
        cleanDatabase();
        var project = projects.save(new Project().setName("Платформа <script>").setStatus(ProjectStatus.ACTIVE));
        projectId = project.getId();
        projects.save(new Project().setName("Другой").setStatus(ProjectStatus.COMPLETED));
        var user = users.save(new User().setName("Анна").setLastName("Иванова").setEmail("anna@example.org"));
        userId = user.getId();
        participants.save(new ProjectParticipant().setProject(project).setUser(user));
        var task = tasks.save(new ProjectTask().setProject(project).setTitle("Задача"));
        comments.save(new TaskComment().setTask(task).setAuthor(user).setText("Комментарий"));
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
    void allFiveRepositoriesAndTheirAssociationsAreAvailable() throws Exception {
        for (String resource : new String[]{"projects", "users", "projectParticipants", "projectTasks", "taskComments"}) {
            var response = request("GET", "/api/" + resource, null);
            assertThat(response.statusCode()).as(resource).isEqualTo(200);
            assertThat(tree(response).path("_embedded").path(resource).isArray()).as(resource).isTrue();
        }
        assertThat(request("GET", "/api/projects/" + projectId + "/participants", null).statusCode()).isEqualTo(200);
        assertThat(request("GET", "/api/users/" + userId + "/participations", null).statusCode()).isEqualTo(200);
    }

    @Test
    void repositoryCrudPersistsChanges() throws Exception {
        var created = request("POST", "/api/projects", json.writeValueAsString(Map.of("name", "Новый")));
        assertThat(created.statusCode()).isEqualTo(201);
        String location = created.headers().firstValue("Location").orElseThrow();
        String path = URI.create(location).getPath();
        assertThat(request("GET", path, null).statusCode()).isEqualTo(200);
        assertThat(request("PATCH", path, "{\"name\":\"Изменён\"}").statusCode()).isEqualTo(200);
        assertThat(tree(request("GET", path, null)).path("name").asText()).isEqualTo("Изменён");
        assertThat(request("PUT", path, "{\"name\":\"Заменён\",\"status\":\"ACTIVE\"}").statusCode()).isEqualTo(200);
        assertThat(tree(request("GET", path, null)).path("name").asText()).isEqualTo("Заменён");
        assertThat(request("DELETE", path, null).statusCode()).isIn(200, 204);
        assertError(request("GET", path, null), 404);
    }

    @Test
    void otherRepositoriesSupportCreatingUpdatingAndDeletingRelatedEntities() throws Exception {
        long taskId = tasks.findAll().iterator().next().getId();
        Object[][] cases = {
                {"users", Map.of("name", "Борис", "lastName", "Петров", "email", "boris@example.org"), "name", "Борис 2"},
                {"projectTasks", Map.of("title", "Новая задача", "project", "/api/projects/" + projectId), "title", "Задача 2"},
                {"taskComments", Map.of("text", "Новый комментарий", "task", "/api/projectTasks/" + taskId,
                        "author", "/api/users/" + userId), "text", "Комментарий 2"}
        };
        for (Object[] item : cases) {
            var created = request("POST", "/api/" + item[0], json.writeValueAsString(item[1]));
            assertThat(created.statusCode()).as(created.body()).isEqualTo(201);
            String path = URI.create(created.headers().firstValue("Location").orElseThrow()).getPath();
            assertThat(request("GET", path, null).statusCode()).isEqualTo(200);
            assertThat(request("PATCH", path, json.writeValueAsString(Map.of(item[2], item[3]))).statusCode()).isEqualTo(200);
            assertThat(tree(request("GET", path, null)).path((String) item[2]).asText()).isEqualTo(item[3]);
            assertThat(request("PUT", path, json.writeValueAsString(item[1])).statusCode()).isEqualTo(200);
            assertThat(request("DELETE", path, null).statusCode()).isIn(200, 204);
            assertError(request("GET", path, null), 404);
        }
        var newUser = users.save(new User().setName("Вера").setLastName("Орлова").setEmail("vera@example.org"));
        var created = request("POST", "/api/projectParticipants", json.writeValueAsString(Map.of(
                "project", "/api/projects/" + projectId, "user", "/api/users/" + newUser.getId())));
        assertThat(created.statusCode()).as(created.body()).isEqualTo(201);
        String path = URI.create(created.headers().firstValue("Location").orElseThrow()).getPath();
        assertThat(request("PATCH", path, "{\"allocationPercent\":50}").statusCode()).isEqualTo(200);
        assertThat(tree(request("GET", path, null)).path("allocationPercent").asInt()).isEqualTo(50);
        assertThat(request("PUT", path, json.writeValueAsString(Map.of("project", "/api/projects/" + projectId,
                "user", "/api/users/" + newUser.getId(), "role", "MANAGER"))).statusCode()).isEqualTo(200);
        assertThat(request("DELETE", path, null).statusCode()).isIn(200, 204);
        assertError(request("GET", path, null), 404);
    }

    @Test
    void customQueriesReturnOnlyMatchingProjectsWithoutRecursiveAssociations() throws Exception {
        String query = "name=" + encode("Платформа <script>") + "&status=ACTIVE";
        for (String path : new String[]{"/custom/projects/findByNameAndStatus?" + query,
                "/custom/projects/findByParticipantEmail?email=anna@example.org"}) {
            var response = request("GET", path, null);
            assertThat(response.statusCode()).isEqualTo(200);
            JsonNode body = tree(response);
            assertThat(body.size()).isEqualTo(1);
            assertThat(body.get(0).path("id").asLong()).isEqualTo(projectId);
            assertThat(body.get(0).has("participants")).isFalse();
        }
        var empty = request("GET", "/custom/projects/findByParticipantEmail?email=nobody@example.org", null);
        assertThat(empty.statusCode()).isEqualTo(200);
        assertThat(tree(empty).isEmpty()).isTrue();
        assertThat(request("GET", "/custom/projects/" + projectId, null).statusCode()).isEqualTo(200);
        var repositorySearch = request("GET", "/api/projects/search/findByNameAndStatusOrderByIdAsc?" + query, null);
        assertThat(repositorySearch.statusCode()).isEqualTo(200);
        assertThat(tree(repositorySearch).path("_embedded").path("projects").size()).isEqualTo(1);
        assertThat(request("GET", "/api/projects/search/findByParticipantEmail?email=anna@example.org", null).statusCode()).isEqualTo(200);
    }

    @Test
    void htmlTableEscapesDataAndSupportsBothQueriesAndEmptyResults() throws Exception {
        var all = request("GET", "/projects", null);
        assertThat(all.statusCode()).isEqualTo(200);
        assertThat(all.headers().firstValue("Content-Type").orElseThrow()).contains("text/html");
        assertThat(all.body()).contains("<table>", "Платформа &lt;script&gt;", "Другой").doesNotContain("Платформа <script>");
        for (String path : new String[]{"/projects/findByNameAndStatus?name=" + encode("Платформа <script>") + "&status=ACTIVE",
                "/projects/findByParticipantEmail?email=anna@example.org"}) {
            var response = request("GET", path, null);
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).contains("Платформа &lt;script&gt;").doesNotContain("Другой");
        }
        assertThat(request("GET", "/projects/findByParticipantEmail?email=nobody@example.org", null).body())
                .contains("Проекты не найдены");
    }

    @Test
    void invalidRequestsUseTheSameJsonErrorFormat() throws Exception {
        for (String path : new String[]{"/custom/projects/findByNameAndStatus?name=x",
                "/custom/projects/findByNameAndStatus?name=x&status=UNKNOWN",
                "/custom/projects/findByNameAndStatus?name=%20&status=ACTIVE",
                "/custom/projects/findByParticipantEmail?email=bad-email",
                "/custom/projects/not-a-number", "/custom/projects/-1"}) {
            assertError(request("GET", path, null), 400);
        }
        assertError(request("GET", "/custom/projects/999999999", null), 404);
        assertError(request("GET", "/api/projects/999999999", null), 404);
        assertError(request("GET", "/missing-page", null), 404);
        assertError(request("POST", "/api/projects", "{broken"), 400);
        assertError(request("POST", "/api/projects", "{\"name\":\" \"}"), 400);
        assertError(request("POST", "/api/users", "{\"name\":\"Анна\",\"lastName\":\"Иванова\",\"email\":\"anna@example.org\"}"), 409);
        assertError(request("POST", "/custom/projects/1", "{}"), 405);
    }

    @Test
    void swaggerUiAndOpenApiIncludeRepositoryCrudAndCustomQueries() throws Exception {
        assertThat(request("GET", "/swagger-ui/index.html", null).statusCode()).isEqualTo(200);
        assertThat(request("GET", "/swagger-ui/swagger-ui-bundle.js", null).statusCode()).isEqualTo(200);
        assertThat(request("GET", "/v3/api-docs/swagger-config", null).statusCode()).isEqualTo(200);
        var response = request("GET", "/v3/api-docs", null);
        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode paths = tree(response).path("paths");
        for (String resource : new String[]{"projects", "users", "projectParticipants", "projectTasks", "taskComments"}) {
            assertThat(paths.path("/api/" + resource).has("get")).as(resource + " GET").isTrue();
            assertThat(paths.path("/api/" + resource).has("post")).as(resource + " POST").isTrue();
            for (String method : new String[]{"get", "put", "patch", "delete"}) {
                assertThat(paths.path("/api/" + resource + "/{id}").has(method)).as(resource + " " + method).isTrue();
            }
        }
        assertThat(paths.has("/custom/projects/findByNameAndStatus")).isTrue();
        assertThat(paths.has("/custom/projects/findByParticipantEmail")).isTrue();
        assertThat(paths.has("/api/projects/search/findByParticipantEmail")).isTrue();
    }

    private void assertError(HttpResponse<String> response, int status) {
        assertThat(response.statusCode()).as(response.body()).isEqualTo(status);
        assertThat(response.headers().firstValue("Content-Type").orElseThrow()).contains("json");
        var error = tree(response);
        assertThat(error.path("status").asInt()).isEqualTo(status);
        assertThat(error.path("error").asText()).isNotBlank();
        assertThat(error.path("message").asText()).isNotBlank();
        assertThat(error.path("path").asText()).startsWith("/");
        assertThat(error.has("trace")).isFalse();
    }

    private JsonNode tree(HttpResponse<String> response) {
        return json.readTree(response.body());
    }

    private HttpResponse<String> request(String method, String path, String body) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Accept", "application/json, text/html, */*");
        if (body != null) builder.header("Content-Type", "application/json");
        return client.send(builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
