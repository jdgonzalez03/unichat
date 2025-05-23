package appmain.ui;

import appmain.ui.services.Services;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URISyntaxException;

import utils.Logger;

public class UniChatApplication extends Application {
    Logger logger = Logger.getInstance();
    Services services = Services.getInstance();

    @Override
    public void start(Stage stage) throws IOException, URISyntaxException {
        // Cargar la vista del Splash Screen
        FXMLLoader splashLoader = new FXMLLoader(getClass().getResource("/appmain/ui/SplashScreen/splash-screen.fxml"));
        Scene splashScene = new Scene(splashLoader.load(), 800, 600);

        // Mostrar la escena del Splash
        stage.setTitle("Splash Screen!");
        stage.setScene(splashScene);
        stage.show();
        logger.log("Cliente inicializado");
        services.startServer();
    }

    public static void main(String[] args) {
        launch();
    }
}
