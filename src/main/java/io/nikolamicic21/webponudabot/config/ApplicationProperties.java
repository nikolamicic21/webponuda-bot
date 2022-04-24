package io.nikolamicic21.webponudabot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app")
public record ApplicationProperties(TelegramBot telegramBot) {

    public record TelegramBot(String secret) {}

}
