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
                if (message.hasText()) {
                    //Достаем из inMess id чата пользователя
                    String chatId = message.getChatId().toString();
                    //Получаем текст сообщения пользователя, отправляем в написанный нами обработчик
                    String response = parseMessage(message.getText());
                    //Создаем объект класса SendMessage - наш будущий ответ пользователю
                    SendMessage outMess = new SendMessage();

                    //Добавляем в наше сообщение id чата а также наш ответ
                    outMess.setChatId(chatId);
                    outMess.setText(response);

                    //Отправка в чат
                    execute(outMess);
                } else {
                    Document document = message.getDocument();
                    saveFile(document);
                }
            }


        } catch (TelegramApiException | IOException e) {
            e.printStackTrace();
        }
    }

    private boolean saveFile(Document document) throws IOException {
        if (document != null) {
            final String fileId = document.getFileId();
            final String fileName = document.getFileName();

            URL url = new URL("https://api.telegram.org/bot" + BOT_TOKEN + "/getFile?file_id=" + fileId);

            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            String getFileResponse = br.readLine();

            JSONObject json = new JSONObject(getFileResponse);
            JSONObject result = json.getJSONObject("result");
            String filePath = result.getString("file_path");

            File localFile = new File("src/main/resources/saved_files/" + fileName);
            InputStream is = new URL("https://api.telegram.org/file/bot" + BOT_TOKEN + "/" + filePath).openStream();
            FileUtils.copyInputStreamToFile(is, localFile);
        }
        return false;
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