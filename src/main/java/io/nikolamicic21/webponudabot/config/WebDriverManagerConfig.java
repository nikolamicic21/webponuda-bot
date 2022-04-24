package io.nikolamicic21.webponudabot.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration(proxyBeanMethods = false)
class WebDriverManagerConfig {

    @Bean(destroyMethod = "quit")
    @Lazy
    WebDriver chromeWebDriver() {
        WebDriverManager.chromedriver().setup();

        return new ChromeDriver(new ChromeOptions().setHeadless(true));
    }
}
