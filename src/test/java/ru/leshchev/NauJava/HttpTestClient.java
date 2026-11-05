package ru.leshchev.NauJava;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class HttpTestClient {
    private final String baseUrl;
    private final HttpClient client;
    private String csrf;

    HttpTestClient(int port) {
        baseUrl = "http://localhost:" + port;
        client = HttpClient.newBuilder().cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL)).build();
    }

    HttpResponse<String> get(String path) throws Exception {
        return send("GET", path, null, null, false);
    }

    String csrf(String path) throws Exception {
        var response = get(path);
        var matcher = Pattern.compile("name=\"_csrf\"[^>]*value=\"([^\"]+)\"").matcher(response.body());
        if (!matcher.find()) throw new AssertionError("CSRF token missing: " + response.body());
        csrf = matcher.group(1);
        return csrf;
    }

    HttpResponse<String> login(String username, String password) throws Exception {
        csrf("/login");
        return form("/login", Map.of("username", username, "password", password), true);
    }

    HttpResponse<String> form(String path, Map<String, String> fields, boolean withCsrf) throws Exception {
        String body = fields.entrySet().stream().map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .collect(Collectors.joining("&"));
        return send("POST", path, body, "application/x-www-form-urlencoded", withCsrf);
    }

    HttpResponse<String> json(String method, String path, String body) throws Exception {
        if (!method.equals("GET")) csrf("/projects");
        return send(method, path, body, "application/json", !method.equals("GET"));
    }

    HttpResponse<String> send(String method, String path, String body, String contentType, boolean withCsrf) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .header("Accept", "application/json, text/html, */*");
        if (contentType != null) builder.header("Content-Type", contentType);
        if (withCsrf) builder.header("X-CSRF-TOKEN", csrf);
        return client.send(builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
