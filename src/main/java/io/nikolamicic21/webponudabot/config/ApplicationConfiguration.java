package io.nikolamicic21.webponudabot.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({
        ApplicationProperties.class
})
public class ApplicationConfiguration {
}
