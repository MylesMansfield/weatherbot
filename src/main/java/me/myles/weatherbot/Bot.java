package me.myles.weatherbot;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import me.myles.weatherbot.commands.Weather;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

public class Bot {

    public static void main(String[] args) throws Exception {
        GetConfigValues properties = new GetConfigValues();
        String discordApiToken = properties.getToken()[0];
        String forecastApiToken = properties.getToken()[1];
        String mapsApiToken = properties.getToken()[2];

        JDA jda = new JDABuilder(discordApiToken).build();
        CommandClientBuilder builder = new CommandClientBuilder()
                .setOwnerId("657468101188911104")
                .setPrefix("?")
                .setHelpWord("help")
                .addCommand(new Weather(forecastApiToken, mapsApiToken));

        CommandClient client = builder.build();

        jda.addEventListener(client);
    }

}
