import javafx.util.StringConverter;
import lombok.Getter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

@Getter
public class City {
    private final long id;
    private final String name;
    private final String region;
    private final String country;
    private final double latitude;
    private final double longitude;

    public static final StringConverter<City> cityConverter = new StringConverter<>() {
        @Override
        public String toString(City city) {
            if (city == null || city.getName() == null || city.getName().isEmpty())
                return " ";
            StringBuilder fullName = new StringBuilder(city.getName());
            if (!(city.getRegion() == null || city.getRegion().isEmpty()))
                fullName.append(", ").append(city.getRegion());
            if (!(city.getCountry() == null || city.getCountry().isEmpty()))
                fullName.append(", ").append(city.getCountry());
            return fullName.toString();
        }

        @Override
        public City fromString(String s) {
            return DBC.findCityByNames(s);
        }
    };

    public City(long id, String name, String region, String country, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.region = region;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public City(ResultSet resultSet) throws SQLException {
        this.id = resultSet.getLong("id");
        this.name = resultSet.getString("city");
        this.region = resultSet.getString("region");
        this.country = resultSet.getString("country");
        this.latitude = resultSet.getDouble("lat");
        this.longitude = resultSet.getDouble("lng");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        City city = (City) o;
        return Objects.equals(name, city.name) &&
                Objects.equals(region, city.region) &&
                Objects.equals(country, city.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, region, country);
    }
}
