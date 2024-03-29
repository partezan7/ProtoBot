package ru.partezan7.protobot;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.partezan7.protobot.repository.MessageRepository;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class Bot extends TelegramLongPollingBot {
    private final MessageRepository repository;
    final private String BOT_TOKEN = System.getProperty("bot.BOT_TOKEN");
    final private String BOT_NAME = System.getProperty("bot.BOT_NAME");
    private static Bot bot;

    public Bot(MessageRepository repository) {
        this.repository = repository;
    }

    public static Bot getBot(MessageRepository repository) {
        if (bot == null) bot = new Bot(repository);
        return bot;
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
                processMessage(update.getMessage());
            }

        } catch (TelegramApiException exception) {
            exception.printStackTrace();
        }
    }

    private void processMessage(Message message) throws TelegramApiException {
        StringBuilder responseBody = new StringBuilder();

        if (message.hasText()) {
            //Получаем текст сообщения пользователя, отправляем в написанный нами обработчик
            responseBody.append(processText(message.getText())) ;
        }

        if (message.hasDocument()) {
            if (!responseBody.isEmpty()) responseBody.append("\n");
            responseBody.append(saveDocument(message.getDocument()));
        }

        if (message.hasPhoto()) {
            PhotoSize photoSize = message.getPhoto().get(3); // Max size
            if (!responseBody.isEmpty()) responseBody.append("\n");
            responseBody.append(savePhoto(photoSize));
        }

        //Создаем объект класса SendMessage - наш будущий ответ пользователю
        SendMessage response = new SendMessage();
        //Достаем из inMess id чата пользователя
        String chatId = message.getChatId().toString();
        //Добавляем в наше сообщение id чата а также наш ответ
        response.setChatId(chatId);
        if (responseBody.isEmpty()) responseBody.append("Ошибка сохранения: формат сообщения не определён");
        response.setText(responseBody.toString());

        //Отправка в чат
        execute(response);
    }

    private static int count = 1;

    private String savePhoto(PhotoSize photo) {
        String response = "";
        GetFile getFile = new GetFile(photo.getFileId());
        String fileId = photo.getFileUniqueId();
        String fileName = fileId + count + ".png";

        try {
            org.telegram.telegrambots.meta.api.objects.File file = this.execute(getFile);//tg file obj
            this.downloadFile(file, new java.io.File("photos/" + fileName));
            System.out.println(LocalDateTime.now() + " file \"" + fileName + "\" is saved");
            response = "Файла: \"" + fileName + "\",  сохранён";
            count++;
        } catch (TelegramApiException e) {
            System.out.println(LocalDateTime.now() + " file \"" + fileName + "\" is NOT saved");
            response = "Ошибка при сохранении файла: " + fileName;
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

    public String processText(String textMsg) {
        String response;
        try {
            URL url = new URL(textMsg);
            response = saveLink(url);
        } catch (IOException exception) {
            //Сравниваем текст пользователя с нашими командами, на основе этого формируем ответ
            if (textMsg.equals("/start"))
                response = "Жми /get, чтобы получить случайное число от 1 до 100";
            else if (textMsg.equals("/get"))
                response = Double.toString(Math.random() * 100);
            else
                response = "Сообщение не сохранено";
        }
        return response;
    }

    private String saveLink(URL url) throws IOException {
        String response;
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer resp = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                resp.append(inputLine);
            }
            in.close();
            org.jsoup.nodes.Document html = Jsoup.parse(resp.toString());
            String title = html.title();
            ru.partezan7.protobot.entity.Message newMessage = new ru.partezan7.protobot.entity.Message();
            newMessage.setLink(url.toString());
            newMessage.setHeader(title);
            newMessage.setTime(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            repository.save(newMessage);
            response = "Ссылка сохранена. Заголовок: " + title;
        } else {
            response = "Ссылка не сохранена. Ошибка: " + responseCode;
        }
        System.out.println(response);
        return response;
    }
}