import lombok.Getter;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Properties;

// Класс, делающий запрос на сайт с погодой и преобразующий ответ в объект Weather
@Getter
public class Request {
    /* https://openweathermap.org
    * Ограничение: 60 Calls per minute (no more than) */
    public static final String OPENWEATHER = "openweathermap";

    /* https://weatherstack.com
    * Ограничение: 1000 Calls/month */
    public static final String WEATHERSTACK = "weatherstack";

    public static final String NO_SUCH_CITY = "Не получилось найти город с таким названием.";
    public static final String CANT_CONNECT = "\n\nНе удалось получить\nданные с сервера.\n\n" +
            "Пожалуйста, проверьте настройки подключения и попробуйте еще раз.";

    private boolean ok = false;
    private final String output;
    private Weather weather;

    // загрузка настроек из config.properties
    static Properties properties;
    static {
        properties = new Properties();
        try {
            properties.load(new FileInputStream("src/main/resources/config.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Request(City city) {
        this.output = getUrlContent(city);
    }

    // получает город, возвращает строку с ответом или строку с текстом ошибки
    public String getUrlContent(City city) {
        String content;
        // запрос на openweather
        try {
            URL url = (city.getId() == -1) ?
                    new URL(String.format(properties.getProperty("openweather.url"),
                            "q=" + city.getName().trim(),
                            properties.getProperty("openweather.appid"))):
                    new URL(String.format(properties.getProperty("openweather.url"),
                            "lat=" + city.getLatitude() + "&lon=" + city.getLongitude(),
                            properties.getProperty("openweather.appid")));
            content = getWeather(url);
        } catch (IOException e) {
            // если произошла какая-то ошибка, делаем запрос на weatherstack
            try {
                URL url = (city.getId() == -1) ?
                        new URL(String.format(properties.getProperty("weatherstack.url"),
                                properties.getProperty("weatherstack.key"),
                                city.getName().trim())):
                        new URL(String.format(properties.getProperty("weatherstack.url"),
                                properties.getProperty("weatherstack.key"),
                                city.getLatitude() + "," + city.getLongitude()));
                content = getWeather(url);
                // попытка преобразовать ответ в Weather
                try {
                    weather = new Weather(city.getId(), new JSONObject(content), WEATHERSTACK);
                } catch (Exception e1) {
                    // если не получилось преобразовать, считаем, что такого города нет
                    return NO_SUCH_CITY;
                }
                ok = true;
                return content;
            }
            catch (NoRouteToHostException | UnknownHostException | ConnectException e1) {
                // если выброшена одна из следующих ошибок - значит какие-то проблемы с подключением
                return CANT_CONNECT;
            } catch (IOException e1) {
                // в остальных случаях считаем, что такой город не был найден
                return NO_SUCH_CITY;
            }
        }
        ok = true;
        // попытка преобразовать ответ в Weather
        try {
            weather = new Weather(city.getId(), new JSONObject(content), OPENWEATHER);
        } catch (Exception e1) {
            // если не получилось преобразовать, считаем, что такого города нет
            return NO_SUCH_CITY;
        }
        return content;
    }

    // делает запрос по URL, возвращает строку с ответом
    private String getWeather(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(5000);
        StringBuilder content = new StringBuilder();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line);
        }
        reader.close();
        return content.toString();
    }
}
