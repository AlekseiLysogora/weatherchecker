import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.InputStream;
import java.sql.SQLException;

public class MainApp extends Application {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        // создание базы данных, если не существует
        DBC.create();
        // запуск приложения
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        // загрузка файла src/main/resources/sample.fxml с настройками окна
        Parent root = FXMLLoader.load(getClass().getResource("/sample.fxml"));

        // название окна
        stage.setTitle("Погодник");

        // картинка в заголовке и на панели управления, расположение src/main/resources/image/drop.png
        InputStream iconStream = getClass().getResourceAsStream("/image/drop.png");
        Image image = new Image(iconStream);
        stage.getIcons().add(image);

        // запрет изменения размера окна
        stage.setResizable(false);

        stage.setScene(new Scene(root));
        stage.show();
    }
}
