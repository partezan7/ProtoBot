package ru.partezan7.protobot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.partezan7.protobot.repository.MessageRepository;

@SpringBootApplication
public class ProtoBotApplication {
    private static MessageRepository repository = null;

    public ProtoBotApplication(MessageRepository repository) {
        ProtoBotApplication.repository = repository;
    }

    public static void main(String[] args) {
        SpringApplication.run(ProtoBotApplication.class, args);
        PropertiesLoader propertiesLoader = new PropertiesLoader();
        propertiesLoader.load();
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(Bot.getBot(repository));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
