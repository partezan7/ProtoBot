package ru.partezan7.protobot;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;

public class Bot extends TelegramLongPollingBot {
    //создаем две константы, присваиваем им значения токена и имя бота соответсвтенно
    //вместо звездочек подставляйте свои данные
    final private String BOT_TOKEN = System.getProperty("bot.BOT_TOKEN");
    final private String BOT_NAME = System.getProperty("bot.BOT_NAME");
    Storage storage;

    Bot() {
        storage = new Storage();
    }

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

                String responseBody;

                if (message.hasText()) {
                    //Получаем текст сообщения пользователя, отправляем в написанный нами обработчик
                    responseBody = parseMessage(message.getText());
                } else {
                    Document document = message.getDocument();
                    boolean isSuccessful = saveFile(document);
                    responseBody = isSuccessful ? "Успешно сохранено" : "Ошибка сохранения";
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

    private boolean saveFile(Document document) {
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

            } catch (IOException exception) {
                System.out.println(LocalDateTime.now() + " file \"" + fileName + "\" is NOT saved");
                return false;
            }
        }

        return true;
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        super.onUpdatesReceived(updates);
    }

    public String parseMessage(String textMsg) {
        String response;

        //Сравниваем текст пользователя с нашими командами, на основе этого формируем ответ
        if (textMsg.equals("/start"))
            response = "Приветствую, бот знает много цитат. Жми /get, чтобы получить случайную из них";
        else if (textMsg.equals("/get"))
            response = storage.getRandQuote();
        else
            response = "Сообщение не распознано";

        return response;
    }
}