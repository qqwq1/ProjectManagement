package ru.leshchev.NauJava.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.leshchev.NauJava.dal.entity.Project;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class AppConfig
{
    @Value("${app.name}")
    private String appName;

    @Value("${app.version}")
    private String appVersion;

    @Bean
    public List<Project> projectContainer()
    {
        return new ArrayList<>();
    }

    public String getAppName()
    {
        return appName;
    }

    public String getAppVersion()
    {
        return appVersion;
    }
}
