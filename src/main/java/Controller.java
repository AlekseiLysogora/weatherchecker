import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;

public class Controller {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button getData;

    @FXML
    private ComboBox<City> box;

    @FXML
    private ImageView image;

    @FXML
    private Text name;

    @FXML
    private Text pressure;

    @FXML
    private Text temperature;

    @FXML
    private Text description;

    @FXML
    private Text humidity;

    @FXML
    private Text clouds;

    @FXML
    void initialize() {
        List<City> cities = DBC.getAllCities();
        Platform.runLater(() -> box.requestFocus());
        box.setVisibleRowCount(7);

        ComboBoxListViewSkin<City> comboBoxListViewSkin = new ComboBoxListViewSkin<>(box);
        comboBoxListViewSkin.getPopupContent().addEventFilter(KeyEvent.ANY, (event) -> {
            if (event.getCode() == KeyCode.SPACE) {
                event.consume();
            }
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.TAB) {
                box.hide();
                Platform.runLater(() -> getData.requestFocus());
            }
        });
        box.setSkin(comboBoxListViewSkin);

        box.getEditor().textProperty().addListener((observableValue, oldValue, newValue) -> {
            box.show();
            if (!newValue.isEmpty()) {
                List<City> list = cities.stream().
                        filter(city -> city.getName().toLowerCase(Locale.ROOT).
                                startsWith(newValue.strip().toLowerCase(Locale.ROOT))).
                        distinct().
                        sorted(Comparator.comparing(City::getName)).
                        collect(Collectors.toList());
                if (!list.isEmpty()) {
                    box.setConverter(City.cityConverter);
                    box.getItems().setAll(list);
                    box.getSelectionModel().clearSelection();
                    box.getEditor().setText(newValue);
                }
            }
        });

        getData.setOnAction(event -> {
            Platform.runLater(() -> box.requestFocus());
            City city = box.getValue();
            box.getEditor().clear();
            box.getSelectionModel().clearSelection();
            box.hide();
            if (city != null &&
                    !(city.getName() == null ||
                            city.getName().trim().isEmpty())) {
                name.setVisible(true);
                setTextVisible(false);
                image.setVisible(false);
                Request request = new Request(city);
                if (!request.getOutput().isEmpty()) {
                    if (!request.isOk()) {
                        name.setText(request.getOutput());
                    } else {
                        Weather w = request.getWeather();
                        name.setText(city.getName());
                        temperature.setText("Температура воздуха: " + w.getTemperature() + "\u00B0C");
                        pressure.setText("Давление: " + w.getPressure() + " мм рт.ст.");
                        description.setText(w.getDescription().substring(0, 1).toUpperCase() +
                                w.getDescription().substring(1));
                        humidity.setText("Влажность: " + w.getHumidity() + "%");
                        clouds.setText("Облачность: " + w.getCloudiness() + "%");
                        setTextVisible(true);
                        DBC.saveRequest(w);
                    }
                }
            }
        });
    }

    private void setTextVisible(boolean value) {
        pressure.setVisible(value);
        temperature.setVisible(value);
        description.setVisible(value);
        humidity.setVisible(value);
        clouds.setVisible(value);
    }
}
