package ru.leshchev.NauJava;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.console.enabled=false")
class NauJavaApplicationTests {

	@Test
	void contextLoads() {
	}

}
