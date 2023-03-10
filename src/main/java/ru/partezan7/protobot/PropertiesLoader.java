package ru.partezan7.protobot;

import java.io.*;
import java.util.Properties;

public class PropertiesLoader {

    private static final String pathToConfig = System.getProperty("bot.config.path");

    public void load() {
        Properties botProperties = new Properties(System.getProperties());
        try {
            if (pathToConfig != null) {
                final FileInputStream in = new FileInputStream(pathToConfig);
                botProperties.load(in);
            } else {
                String userDir = System.getProperty("user.dir");

                try {
                    File defaultConfigFile = new File(userDir + "/bot.properties");
                    final FileInputStream in = new FileInputStream(defaultConfigFile);
                    botProperties.load(in);
                } catch (FileNotFoundException exception) {
                    try (InputStream inputStream = PropertiesLoader.class.getResourceAsStream("/bot.properties")) {
                        botProperties.load(inputStream);
                    }
                }
            }
        } catch (IOException exception) {
            System.out.println("File " + "bot.properties" + " not found");
            exception.printStackTrace();
        }
        System.setProperties(botProperties);
    }

}
