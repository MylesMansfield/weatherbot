package me.myles.weatherbot;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GetConfigValues {

    private String[] token;
    InputStream inputStream;

    public String[] getToken() throws IOException {

        try {
            token = new String[3];
            Properties prop = new Properties();
            String propFileName = "config.properties";

            inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }

            token[0] = prop.getProperty("token");
            token[1] = prop.getProperty("forecast");
            token[2] = prop.getProperty("maps");


        } catch (Exception e) {
            System.out.println("Exception: " + e);
        } finally {
            inputStream.close();
        }

        return token;
    }

}
