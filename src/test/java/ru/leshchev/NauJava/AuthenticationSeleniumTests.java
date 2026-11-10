package ru.leshchev.NauJava;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.leshchev.NauJava.dal.entity.User;
import ru.leshchev.NauJava.dal.repository.UserRepository;

import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthenticationSeleniumTests {
    private static final String REMOTE_URL = System.getProperty("selenium.remote.url");
    private static final String USERNAME = "selenium-user";
    private static final String PASSWORD = "test-password";
    @LocalServerPort int port;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder passwordEncoder;
    @TempDir Path browserProfile;
    private WebDriver driver;
    private Long userId;

    @BeforeAll
    static void setUpDriver() {
        if (REMOTE_URL == null) {
            WebDriverManager.chromedriver().cachePath("target/webdriver")
                    .resolutionCachePath("target/webdriver").setup();
        }
    }

    @BeforeEach
    void setUp() throws MalformedURLException {
        userId = users.save(new User().setName("Selenium").setLastName("User")
                .setEmail("selenium@example.org").setUsername(USERNAME)
                .setPassword(passwordEncoder.encode(PASSWORD))).getId();
        var options = new ChromeOptions().addArguments("--headless");
        String binary = System.getProperty("selenium.chrome.binary");
        if (binary != null) {
            options.setBinary(binary);
        }
        if (REMOTE_URL == null) {
            options.addArguments("--user-data-dir=" + browserProfile);
            driver = new ChromeDriver(options);
        } else {
            driver = new RemoteWebDriver(URI.create(REMOTE_URL).toURL(), options);
        }
    }

    @AfterEach
    void cleanUp() {
        try {
            if (driver != null) driver.quit();
        } finally {
            if (userId != null) users.deleteById(userId);
        }
    }

    @Test
    void logsInAndLogsOutThroughTheBrowser() {
        String baseUrl = "http://" + System.getProperty("selenium.app.host", "127.0.0.1") + ":" + port;
        var wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        driver.get(baseUrl + "/login");
        driver.findElement(By.id("username")).sendKeys(USERNAME);
        driver.findElement(By.id("password")).sendKeys(PASSWORD);
        driver.findElement(By.cssSelector("form[action='/login'] button[type='submit']")).click();

        wait.until(ExpectedConditions.urlToBe(baseUrl + "/projects"));
        assertEquals("Проекты", driver.findElement(By.tagName("h1")).getText());
        driver.findElement(By.cssSelector("form[action='/logout'] button[type='submit']")).click();

        wait.until(ExpectedConditions.urlToBe(baseUrl + "/login?logout"));
        assertEquals("Вы вышли из системы.", driver.findElement(By.cssSelector(".notice")).getText());
        driver.get(baseUrl + "/projects");
        wait.until(ExpectedConditions.urlToBe(baseUrl + "/login"));
    }
}
