package me.myles.weatherbot.commands;

import com.github.dvdme.ForecastIOLib.*;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Weather extends Command {

    private String forecastApiToken;
    private String mapsApiToken;
    private String locJson;
    private String name;
    private double lat;
    private double lng;

    public Weather(String forecastApiToken, String mapsApiToken){
        super.name = "weather";
        super.aliases = new String[]{"w"};
        super.arguments = "[location name]";
        super.help = "Retrieves Weather Info";
        super.cooldown = 10;
        this.forecastApiToken = forecastApiToken;
        this.mapsApiToken = mapsApiToken;
    }

    protected void execute(CommandEvent e) {
        String args = e.getArgs();
        args = args.replace(' ', '-');
        lat = 0; lng = 0;

        /// GET Lat & Long of location

        var client1 = HttpClient.newHttpClient();
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(String.format("https://us1.locationiq.com/v1/search.php?key=%s&q=%s&format=json", mapsApiToken, args)))
                .GET()
                .build();

        try{
            var response = client1.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            String a = response.body();
            locJson = a;
            name = a.substring(a.indexOf("\"display_name\"") + 16, a.substring(a.indexOf("\"display_name\"") + 16).indexOf("\"") + a.indexOf("\"display_name\"") + 16);
            lat = Double.parseDouble(a.substring(a.indexOf("\"lat") + 7, a.substring(a.indexOf("\"lat") + 7).indexOf("\"") + a.indexOf("\"lat") + 7));
            lng = Double.parseDouble(a.substring(a.indexOf("\"lon") + 7, a.substring(a.indexOf("\"lon") + 7).indexOf("\"") + a.indexOf("\"lon") + 7));
        }catch(Exception ex){
            System.out.println(ex);
        }

        ///

        /// GET / FORMAT weather info

        if(locJson.contains("\"error\"")){
            e.reply("Invalid Location");
        }
        else{
            ForecastIO fio = new ForecastIO(forecastApiToken);
            fio.setUnits(ForecastIO.UNITS_US);
            fio.setExcludeURL("hourly,minutely");
            fio.getForecast(lat + "", lng + "");

            FIOCurrently currently = new FIOCurrently(fio);
            FIODaily daily = new FIODaily(fio);
            FIOFlags flags = new FIOFlags(fio);
            FIOAlerts alerts = new FIOAlerts(fio);

            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(Color.BLUE);

            String url = "";
            switch(currently.get().icon()){
                case "\"clear-day\"" : url = "https://img.icons8.com/ios/2x/sun.png"; break;
                case "\"clear-night\"" : url = "https://img.icons8.com/ios/2x/bright-moon.png"; break;
                case "\"partly-cloudy-day\"" : url = "https://img.icons8.com/ios/2x/partly-cloudy-day.png"; break;
                case "\"partly-cloudy-night\"" : url = "https://img.icons8.com/android/2x/partly-cloudy-night.png"; break;
                case "\"cloudy\"" : url = "https://img.icons8.com/ios/2x/cloud.png"; break;
                case "\"rain\"" : url = "https://img.icons8.com/ios/2x/rain.png"; break;
                case "\"sleet\"" : url = "https://img.icons8.com/ios/2x/sleet.png"; break;
                case "\"snow\"" : url = "https://img.icons8.com/ios/2x/light-snow.png"; break;
                case "\"wind\"" : url = "https://img.icons8.com/ios/2x/wind.png"; break;
                case "\"fog\"" : url = "https://img.icons8.com/ios/2x/fog-night.png"; break;
                default : url = "https://developer.apple.com/design/human-interface-guidelines/watchos/images/icon-and-image-large-icon-weather_2x.png";
            }
            eb.setAuthor(name, "https://darksky.net/forecast/"+lat+","+lng+"/us12/en", url);
            eb.setFooter("Weather Bot | " + currently.get().time(), url);

            eb.addField("Temp",currently.get().temperature() + "F", true);
            eb.addField("Max", daily.getDay(0).temperatureMax() + "F", true);
            eb.addField("Min", daily.getDay(0).temperatureMin() + "F", true);

            eb.addField("Summary",currently.get().summary(), true);
            eb.addField("Precip Prop", String.format("%.5s", currently.get().precipProbability()*100) + "%", true);
            eb.addField("Wind", currently.get().windSpeed() + "mph", true);

            int num = alerts.NumberOfAlerts();

            if(num <= 0) {
                eb.addField("Alerts", "none", true);
            }
            else {
                eb.addField("Alert - " + alerts.getAlertTitle(0), "From " + alerts.getAlertTime(0) + " To " + alerts.getAlertExpireTime(0), false);
                if (num >= 2) {
                    eb.addField("Alert - " + alerts.getAlertTitle(1), "From " + alerts.getAlertTime(1) + " To " + alerts.getAlertExpireTime(1), false);
                    if (num > 2) {
                        eb.addField("More Alerts", "https://darksky.net/forecast/" + lat + "," + lng + "/us12/en", false);
                    }
                }
            }

            eb.addBlankField(false);

            String time = currently.get().time();
            LocalDate dt = LocalDate.parse(time.substring(6, 10) + "-" + time.substring(3, 5) + "-" + time.substring(0, 2));

            Map<String, String[]> days = new HashMap<>();
            days.put("THURSDAY", new String[]{"FRIDAY", "SATURDAY", "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY"});
            days.put("FRIDAY", new String[]{"SATURDAY", "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"});
            days.put("SATURDAY", new String[]{"SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"});
            days.put("SUNDAY", new String[]{"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"});
            days.put("MONDAY", new String[]{"TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY", "MONDAY"});
            days.put("TUESDAY", new String[]{"WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY", "MONDAY", "TUESDAY"});
            days.put("WEDNESDAY", new String[]{"THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY"});

            String[] h = null;
            h = days.get(dt.getDayOfWeek().toString());

            for(int i = 0; i < 7; i++){
                eb.addField(h[i] + "", daily.getDay(i + 1).summary() + " Max: " + daily.getDay(1).temperatureMax() + " Min: " + daily.getDay(i + 1).temperatureMin(), false);
            }

            e.getChannel().sendMessage(eb.build()).queue();
        }

        ///

    }

}
