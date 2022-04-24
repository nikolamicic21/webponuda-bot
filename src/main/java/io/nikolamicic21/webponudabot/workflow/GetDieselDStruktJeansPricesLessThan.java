package io.nikolamicic21.webponudabot.workflow;

import io.nikolamicic21.webponudabot.config.ApplicationProperties;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;

import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfAllElements;

@Component
@Slf4j
public class GetDieselDStruktJeansPricesLessThan implements ApplicationRunner {

    private final ObjectProvider<WebDriver> webDriverProvider;
    private final WebClient webClient;

    public GetDieselDStruktJeansPricesLessThan(
            ObjectProvider<WebDriver> webDriverProvider,
            WebClient.Builder webClientBuilder,
            ApplicationProperties applicationProperties
    ) {
        this.webDriverProvider = webDriverProvider;
        this.webClient = webClientBuilder
                .baseUrl("https://api.telegram.org/bot" + applicationProperties.telegramBot().secret() + "/sendMessage")
                .build();
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        final var lastCheckedAtPath = Path.of("last-checked-at.txt");
        if (!Files.exists(lastCheckedAtPath)) {
            Files.createFile(lastCheckedAtPath);
        }
        final var lastCheckedAtOptional = Files.readAllLines(lastCheckedAtPath).stream().findFirst();
        if (lastCheckedAtOptional.isEmpty() || LocalDate.now().isAfter(LocalDate.parse(lastCheckedAtOptional.get()))) {
            final var jeansPath = Path.of("jeans.txt");
            if (!Files.exists(jeansPath)) {
                Files.createFile(jeansPath);
            }
            final var jeans = Files.readAllLines(jeansPath);
            this.webDriverProvider.ifAvailable(webDriver -> {
                webDriver.get("https://www.fashionandfriends.com/rs/muskarci/odeca/farmerke/filter/velicina:34-034/brend:diesel/price:5000.00-12500.00/");
                webDriver.findElement(By.cssSelector(".allow-all-cookies")).click();

                new WebDriverWait(webDriver, 10)
                        .until(visibilityOfAllElements(webDriver.findElements(By.cssSelector(".product-item"))))
                        .stream()
                        .map(productElement ->
                                productElement
                                        .findElement(By.cssSelector(".product-item-details"))
                                        .findElement(By.cssSelector(".product-item-link"))
                        )
                        .filter(productItemLink -> {
                            final var productTitleUppercase = productItemLink
                                    .getText().toUpperCase();

                            return productTitleUppercase.contains("STRUKT") || productTitleUppercase.contains("LUSTER");
                        })
                        .forEach(productItemElement -> {
                            final var productLink = productItemElement.getAttribute("href");
                            if (!jeans.contains(productLink)) {
                                log.info(">>> {}", productLink);
                                this.webClient.get()
                                        .uri(uriBuilder -> uriBuilder
                                                .queryParam("chat_id", "@webponuda")
                                                .queryParam("text", productLink)
                                                .build()
                                        )
                                        .retrieve()
                                        .toBodilessEntity()
                                        .block();
                                try {
                                    Files.writeString(jeansPath, productLink + System.lineSeparator(), StandardOpenOption.APPEND);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

            });
            Files.writeString(lastCheckedAtPath, LocalDate.now().toString(), StandardOpenOption.WRITE);
        }
    }
}
