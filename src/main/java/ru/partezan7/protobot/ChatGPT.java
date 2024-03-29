package ru.partezan7.protobot;

import org.springframework.core.env.Environment;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.partezan7.protobot.service.ChatGPTService;

public class ChatGPT {
    private Bot bot;
    private ChatGPTService service;
    private Environment env;

    public ChatGPT(Bot bot, ChatGPTService service, Environment env) {
        this.bot = bot;
        this.service = service;
        this.env = env;
    }

    public void onUpdateReceived(Update update) {
        SendMessage sendMessage = new SendMessage();
        String text = update.getMessage().getText();
        String chatId = String.valueOf(update.getMessage().getChatId());

        pong(sendMessage, chatId, text);

        askGpt(sendMessage, chatId, text);
    }

    /**
     * Sends your request to chatGPT using #ChatGPTService.askChatGPT
     *
     * @param sendMessage
     * @param chatId
     * @param text
     */
    private void askGpt(SendMessage sendMessage, String chatId, String text) {

        String gptResponse = service.askChatGPTText(text);
        try {
            sendMessage.setChatId(chatId);
            sendMessage.setText("GPT answers: " + gptResponse);
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gives a feedback about what you are requesting to chatGPT
     *
     * @param sendMessage
     * @param chatId
     * @param text
     */
    private void pong(SendMessage sendMessage, String chatId, String text) {
        sendMessage.setText("Hello! I'm sending this message to chatGPT: " + text);
        try {
            sendMessage.setChatId(chatId);
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


}
