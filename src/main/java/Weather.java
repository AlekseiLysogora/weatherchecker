import lombok.Getter;
import org.json.JSONObject;

@Getter
public class Weather {
    private final Long cityId;
    private final double temperature;
    private final String description;
    private final double pressure;
    private final int humidity;
    private final int cloudiness;
    private final String cite;

    public Weather(long id, JSONObject object, String cite) {
        this.cityId = id == -1 ? null : id;
        if (cite.equals(Request.OPENWEATHER)) {
            this.temperature = object.getJSONObject("main").getDouble("temp");
            this.description = object.getJSONArray("weather").
                    getJSONObject(0).getString("description");
            this.pressure = object.getJSONObject("main").getDouble("pressure");
            this.humidity = object.getJSONObject("main").getInt("humidity");
            this.cloudiness = object.getJSONObject("clouds").getInt("all");
        } else {
            this.temperature = object.getJSONObject("current").getDouble("temperature");
            this.description = " ";
            this.pressure = object.getJSONObject("current").getDouble("pressure");
            this.humidity = object.getJSONObject("current").getInt("humidity");
            this.cloudiness = object.getJSONObject("current").getInt("cloudcover");
        }
        this.cite = cite;
    }
}
