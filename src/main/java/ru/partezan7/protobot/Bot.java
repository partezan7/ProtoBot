package ru.partezan7.protobot;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.env.Environment;
//import ru.partezan7.protobot.service.ChatGPTService;

import java.io.*;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class Bot extends TelegramLongPollingBot {
    //создаем две константы, присваиваем им значения токена и имя бота соответсвтенно
    //вместо звездочек подставляйте свои данные
    final private String BOT_TOKEN = System.getProperty("bot.BOT_TOKEN");
    final private String BOT_NAME = System.getProperty("bot.BOT_NAME");

    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @Override
    public void onRegister() {
        super.onRegister();
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) {
                //Извлекаем из объекта сообщение пользователя
                Message message = update.getMessage();
                //Достаем из inMess id чата пользователя
                String chatId = message.getChatId().toString();

                String responseBody = "Ошибка сохранения: формат сообщения не определён";
                if (message.hasText()) {
                    //Получаем текст сообщения пользователя, отправляем в написанный нами обработчик
                    responseBody = saveText(message.getText());
                }
                if (message.hasDocument()) {
                    responseBody = saveDocument(message.getDocument());
                }
                if (message.hasPhoto()) {
                    List<PhotoSize> photos = message.getPhoto();
                    if (photos != null && photos.size() > 0) {
                        List<String> responses = message.getPhoto().stream().map(this::savePhoto).toList();
                        StringBuilder stringBuilder = new StringBuilder();
                        responses.forEach(resp -> stringBuilder.append(resp + "\n"));
                        responseBody = stringBuilder.toString();
                    }
                }

                //Создаем объект класса SendMessage - наш будущий ответ пользователю
                SendMessage response = new SendMessage();
                //Добавляем в наше сообщение id чата а также наш ответ
                response.setChatId(chatId);
                response.setText(responseBody);

                //Отправка в чат
                execute(response);
            }

        } catch (TelegramApiException exception) {
            exception.printStackTrace();
        }
    }

    private String savePhoto(PhotoSize photo) {
        String response = "";
        String fileId = photo.getFileId();
        String fileUniqueId = photo.getFileUniqueId();
        try {
            URL url = new URL("https://api.telegram.org/bot" + BOT_TOKEN + "/getFile?file_id=" + fileId);

            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            String getFileResponse = br.readLine();

            JSONObject json = new JSONObject(getFileResponse);
            JSONObject result = json.getJSONObject("result");
            String filePath = result.getString("file_path");

            File localFile = new File("src/main/resources/saved_files/" + fileUniqueId);
            InputStream is = new URL("https://api.telegram.org/file/bot" + BOT_TOKEN + "/" + filePath).openStream();
            FileUtils.copyInputStreamToFile(is, localFile);
            System.out.println(LocalDateTime.now() + " file \"" + fileUniqueId + "\" is saved");
            response = "Файла: \"" + fileUniqueId + "\",  сохранён";
        } catch (IOException exception) {
            System.out.println(LocalDateTime.now() + " file \"" + fileUniqueId + "\" is NOT saved");
            response = "Ошибка при сохранении файла: " + fileUniqueId;
        }
        return response;
    }

    private String saveDocument(Document document) {
        String response = "";
        if (document != null) {
            final String fileId = document.getFileId();
            final String fileName = document.getFileName();

            try {
                URL url = new URL("https://api.telegram.org/bot" + BOT_TOKEN + "/getFile?file_id=" + fileId);

                BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
                String getFileResponse = br.readLine();

                JSONObject json = new JSONObject(getFileResponse);
                JSONObject result = json.getJSONObject("result");
                String filePath = result.getString("file_path");

                File localFile = new File("src/main/resources/saved_files/" + fileName);
                InputStream is = new URL("https://api.telegram.org/file/bot" + BOT_TOKEN + "/" + filePath).openStream();
                FileUtils.copyInputStreamToFile(is, localFile);
                System.out.println(LocalDateTime.now() + " file \"" + fileName + "\" is saved");
                response = "Файла: \"" + fileName + "\",  сохранён";
            } catch (IOException exception) {
                System.out.println(LocalDateTime.now() + " file \"" + fileName + "\" is NOT saved");
                response = "Ошибка при сохранении файла: " + fileName;
            }
        }

        return response;
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        super.onUpdatesReceived(updates);
    }

    public String saveText(String textMsg) {
        String response;

        //Сравниваем текст пользователя с нашими командами, на основе этого формируем ответ
        if (textMsg.equals("/start"))
            response = "Жми /get, чтобы получить случайное число от 1 до 100";
        else if (textMsg.equals("/get"))
            response = Double.toString(Math.random() * 100);
        else
            response = "Сообщение не сохранено";

        return response;
    }

    //    private static ChatGPTService service;
//    private static Environment env;

//    @Override
//    /**
//     * Action invoked when the user sends a message to the chat.
//     */
//    public void onUpdateReceived(Update update) {
//        SendMessage sendMessage = new SendMessage();
//        String text = update.getMessage().getText();
//        String chatId = String.valueOf(update.getMessage().getChatId());
//
//        pong(sendMessage, chatId, text);
//
//        askGpt(sendMessage, chatId, text);
//    }
//
//    /**
//     * Sends your request to chatGPT using #ChatGPTService.askChatGPT
//     *
//     * @param sendMessage
//     * @param chatId
//     * @param text
//     */
//    private void askGpt(SendMessage sendMessage, String chatId, String text) {
//
//        String gptResponse = service.askChatGPTText(text);
//        try {
//            sendMessage.setChatId(chatId);
//            sendMessage.setText("GPT answers: " + gptResponse);
//            execute(sendMessage);
//        } catch (TelegramApiException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    /**
//     * Gives a feedback about what you are requesting to chatGPT
//     *
//     * @param sendMessage
//     * @param chatId
//     * @param text
//     */
//    private void pong(SendMessage sendMessage, String chatId, String text) {
//        sendMessage.setText("Hello! I'm sending this message to chatGPT: " + text);
//        try {
//            sendMessage.setChatId(chatId);
//            execute(sendMessage);
//        } catch (TelegramApiException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public String getBotUsername() {
//        return BOT_NAME;
//    }
//
//    @Override
//    public String getBotToken() {
//        return BOT_TOKEN;
//    }
//
//    @Autowired
//    public void setService(ChatGPTService service) {
//        this.service = service;
//    }
//
//    @Autowired
//    public void setEnv(Environment env) {
//        Bot.env = env;
//    }
}