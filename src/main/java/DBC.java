import org.apache.ibatis.jdbc.ScriptRunner;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

public class DBC {
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

    // создает базу данных из скрипта script.sql
    public static void create() throws ClassNotFoundException, SQLException {
        try (Reader reader = new BufferedReader(
                        new FileReader("src/main/resources/script.sql", StandardCharsets.UTF_8));
             Connection connection = DriverManager.getConnection(properties.getProperty("db.address"),
                     properties.getProperty("db.user"), properties.getProperty("db.password"))) {
            Class.forName(properties.getProperty("db.driver"));

            ScriptRunner scriptRunner = new ScriptRunner(connection);
            scriptRunner.setLogWriter(new PrintWriter("src/log/err.txt"));
            scriptRunner.setErrorLogWriter(new PrintWriter("src/log/err.txt"));
            scriptRunner.runScript(reader);
            scriptRunner.closeConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // выгружает из базы из таблицы city города, возвращает список
    public static List<City> getAllCities() {
        List<City> cities = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(properties.getProperty("db.host"),
                properties.getProperty("db.user"), properties.getProperty("db.password"));
             Statement statement = connection.createStatement()) {
            Class.forName(properties.getProperty("db.driver"));
            ResultSet resultSet = statement.executeQuery(
                    "select id, city, region, country, lat, lng from city;");
            while (resultSet.next()) {
                City city = new City(resultSet);
                cities.add(city);
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return cities;
    }

    // получает объект Weather, который был создан в результате запроса на сайт
    // сохраняет в базу в таблицу weather данные
    public static void saveRequest(Weather weather) {
        try (Connection connection = DriverManager.getConnection(properties.getProperty("db.host"),
                properties.getProperty("db.user"), properties.getProperty("db.password"));
             Statement statement = connection.createStatement()) {
            Class.forName(properties.getProperty("db.driver"));
            String query = String.format(Locale.US, "insert into weather " +
                            "(city_id, temperature, description, pressure," +
                            " humidity, cloudiness)" +
                            " VALUE (%d, %5.2f, '%s', %5.2f, %d, %d);",
                            weather.getCityId(), weather.getTemperature(), weather.getDescription(),
                            weather.getPressure(), weather.getHumidity(), weather.getCloudiness());
            statement.executeUpdate(query);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // ищет в базе город по названию, возвращает объект City
    public static City findCityByNames(String s) {
        try (Connection connection = DriverManager.getConnection(properties.getProperty("db.host"),
                properties.getProperty("db.user"), properties.getProperty("db.password"));
             Statement statement = connection.createStatement()) {
            Class.forName(properties.getProperty("db.driver"));
            // по умолчанию, если город из базы, строка будет выглядеть как
            // "Город, Регион, Страна"
            // но есть возможность искать города не из базы
            String[] names = s.replaceAll("\"", "'").split(", ");
            if (names.length == 3) {
                ResultSet resultSet = statement.executeQuery(
                        "select * from city where city=\"" + names[0].trim()
                                + "\" and region=\"" + names[1].trim()
                                + "\" and country=\"" + names[2].trim() + "\";");
                return resultSet.next() ? new City(resultSet) :
                        new City(-1, names[0].isEmpty() ? "" : names[0].substring(0, 1).toUpperCase() +
                        names[0].substring(1), "", "", 0, 0);
            } else if (names.length == 1) {
                ResultSet resultSet = statement.executeQuery(
                        "select * from city where city=\"" + names[0].trim() + "\";");
                if (resultSet.next())
                    return new City(resultSet);
                else return new City(-1, names[0].isEmpty() ? "" : names[0].substring(0, 1).toUpperCase() +
                        names[0].substring(1), "", "", 0, 0);
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
