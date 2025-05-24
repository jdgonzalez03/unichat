package appmain.ui.Controllers;

import appmain.ui.model.Model_User;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import utils.Logger;

import appmain.ui.MainViewController;

import appmain.ui.model.Model_User_Login;
import appmain.ui.services.AuthService;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class LoginViewController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Button backButton;
    @FXML
    private Hyperlink forgotPasswordLink;

    private MainViewController mainController;
    private final AuthService authService = new AuthService();
    private Logger logger = Logger.getInstance();


    @FXML
    public void initialize() {
        // Configurar eventos para los botones y enlaces
        setupButtons();
    }

    private void setupButtons() {
        loginButton.setOnAction(event -> handleLogin());
        backButton.setOnAction(event -> closeWindow());
        forgotPasswordLink.setOnAction(event -> showForgotPasswordDialog());
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        // Validación básica (en una app real se haría contra una base de datos)
        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Por favor, ingresa tu correo y contraseña.", Alert.AlertType.INFORMATION);
            return;
        }

        Model_User_Login data = new Model_User_Login(email, password);
        AtomicBoolean success = new AtomicBoolean(false);
        AtomicReference<String> message = new AtomicReference<>("Algo salió mal.");

        authService.loginUser(data,(args) -> {
            if (args.length >= 2) {
                success.set((Boolean) args[0]);
                message.set((String) args[1]);
                Object rawData = args.length >= 3 ? args[2] : null;

                logger.log("Éxito: " + success);
                logger.log("Mensaje: " + message);

                if (success.get() && rawData != null) {
                    Gson gson = new Gson();
                    Model_User user = gson.fromJson(rawData.toString(), Model_User.class);

                    logger.log("Usuario logueado: " + user.getUsername());
                }

                Platform.runLater(() -> {
                    if (success.get()) {
                        showAlert("Login", message.get(), Alert.AlertType.INFORMATION );
                        closeWindow();
                    }else{
                        showAlert("Login",message.get(), Alert.AlertType.WARNING );
                    }
                });
            } else {
                logger.log("Respuesta inesperada del servidor.");
                Platform.runLater(() -> {
                    showAlert("Error", "Respuesta inesperada del servidor.", Alert.AlertType.ERROR);
                });
            }
        });

        //TODO: Probar con el servidor
        //loginController.loguearse(loginRequestDto);
        //System.out.println("He logueado: " + email);

        //TODO: Extracción de datos del response, para continuar ???
        //String username = email.split("@")[0];

        //mainController.onLoginSuccess(username, email);
    }

    private void showForgotPasswordDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Recuperar contraseña");
        alert.setHeaderText(null);
        alert.setContentText("Funcionalidad no implementada todavía.");
        alert.showAndWait();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.close();
    }

    public void setMainController(MainViewController controller) {
        this.mainController = controller;
    }
}
