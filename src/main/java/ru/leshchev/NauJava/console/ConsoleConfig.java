package ru.leshchev.NauJava.console;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Scanner;

@Configuration
@RequiredArgsConstructor
public class ConsoleConfig {
    private final CommandProcessor commandProcessor;
    private final ConfigurableApplicationContext applicationContext;

    @Bean
    @ConditionalOnProperty(name = "app.console.enabled", havingValue = "true", matchIfMissing = true)
    public CommandLineRunner commandScanner() {
        return args -> {
            try (Scanner scanner = new Scanner(System.in)) {
                System.out.println("Enter command. Type 'help' to see available commands or 'exit' to quit.");

                while (true) {
                    System.out.print("> ");

                    if (!scanner.hasNextLine()) {
                        break;
                    }

                    String input = scanner.nextLine();

                    if ("exit".equalsIgnoreCase(input.trim())) {
                        System.out.println("Exiting application...");
                        applicationContext.close();
                        break;
                    }

                    commandProcessor.processCommand(input);
                }
            }
        };
    }
}
