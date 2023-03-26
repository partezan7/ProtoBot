package ru.partezan7.protobot.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.partezan7.protobot.BotConstants;

import java.net.URI;

@Service
public class ChatGPTService {

    public static final String CHOICES = "choices";
    public static final String TEXT = "text";

    Double temperature = 0.7;

    String textModel = "text-davinci-003";

    Double topP = 1.;

    Double freqPenalty = 0.;

    Double presPenalty = 0.;

    private String apiToken;

    Integer maxTokens = 500;

    private String urlCompletions;


    /**
     * Sends a request to chatGPT's text generator
     *
     * @param msg
     * @return
     */
    public String askChatGPTText(String msg) {
        urlCompletions = System.getProperty("api.url");

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = setHeaders();

        JSONObject request = new JSONObject();
        request.put(BotConstants.MODEL, textModel);
        request.put(BotConstants.PROMPT, msg);
        request.put(BotConstants.TEMPERATURE, temperature);
        request.put(BotConstants.MAX_TOKENS, maxTokens);
        request.put(BotConstants.TOP_P, topP);
        request.put(BotConstants.FREQUENCY_PENALTY, freqPenalty);
        request.put(BotConstants.PRESENCE_PENALTY, presPenalty);

        HttpEntity<String> requestEntity = new HttpEntity<>(request.toString(), headers);
        System.out.println(urlCompletions);
        URI chatGptUrl = URI.create(urlCompletions);
        ResponseEntity<String> responseEntity = restTemplate.
                postForEntity(chatGptUrl, requestEntity, String.class);

        JSONObject responseJson = new JSONObject(responseEntity.getBody());
        JSONArray choices = (JSONArray) responseJson.get(CHOICES);

        JSONObject firstChoice = (JSONObject) choices.get(0);
        return (String) firstChoice.get(TEXT);
    }

    private HttpHeaders setHeaders() {
        apiToken = System.getProperty("api.token");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(BotConstants.AUTHORIZATION, apiToken);
        return headers;
    }
}