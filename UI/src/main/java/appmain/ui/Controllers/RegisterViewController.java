package appmain.ui.Controllers;

import appmain.ui.model.Model_Response;
import appmain.ui.model.Model_User;
import appmain.ui.model.Model_User_Register;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import appmain.ui.MainViewController;
import appmain.ui.services.AuthService;
import utils.Logger;

import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


public class RegisterViewController {

    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Button registerButton;
    @FXML
    private Button backButton;
    @FXML

    private MainViewController mainController;
    private final AuthService authService = new AuthService();
    Logger logger = Logger.getInstance();

    @FXML
    public void initialize() {
        // Configurar eventos
        setupButtons();


    }

    private void setupButtons() {
        registerButton.setOnAction(event -> handleRegister());
        backButton.setOnAction(event -> closeWindow());
    }

    public static String getLocalIpAddress() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            return inetAddress.getHostAddress(); // Devuelve la IP en formato String
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "No se pudo obtener la dirección IP";
        }
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("Error", "Todos los campos son obligatorios.", Alert.AlertType.ERROR);
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert("Error", "Las contraseñas no coinciden.", Alert.AlertType.ERROR);
            return;
        }

        Model_User_Register data = new Model_User_Register(email, username, password);
        AtomicBoolean success = new AtomicBoolean(false);
        AtomicReference<String> message = new AtomicReference<>("Algo salió mal.");

        authService.registerUser(data, (args) -> {
            logger.log("Solicitud de registro enviada al servidor");

            if (args.length >= 2) {
                success.set((Boolean) args[0]);
                message.set((String) args[1]);
                Object rawData = args.length >= 3 ? args[2] : null;

                logger.log("Éxito: " + success);
                logger.log("Mensaje: " + message);

                if (success.get() && rawData != null) {
                    Gson gson = new Gson();
                    Model_User user = gson.fromJson(rawData.toString(), Model_User.class);
                    logger.log("Usuario registrado: " + user.getUsername());
                }

                // Mostrar alerta y cerrar ventana dentro del callback
                Platform.runLater(() -> {
                    if (success.get()) {
                        showAlert("Registrado", message.get() + " .Inicia sesión ahora.", Alert.AlertType.INFORMATION);
                        closeWindow();
                    } else {
                        showAlert("Error", message.get(), Alert.AlertType.WARNING);
                    }

                });
            } else {
                logger.log("Respuesta inesperada del servidor.");
                Platform.runLater(() -> {
                    showAlert("Error", "Respuesta inesperada del servidor.", Alert.AlertType.ERROR);
                });
            }
        });

    }

    private void openLoginForm() {
        closeWindow();
        mainController.openLoginForm();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) registerButton.getScene().getWindow();
        stage.close();
    }

    public void setMainController(MainViewController controller) {
        this.mainController = controller;
    }
}
