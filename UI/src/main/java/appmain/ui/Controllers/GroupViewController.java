package appmain.ui.Controllers;

import appmain.ui.model.Model_Create_Group;
import appmain.ui.services.GroupService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import appmain.ui.MainViewController;
import utils.Logger;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


public class GroupViewController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField descriptionfield;
    @FXML
    private Button createButton;
    @FXML
    private Button backButton;
    @FXML

    private MainViewController mainController;
    private GroupService groupService = new GroupService();
    private Logger logger = Logger.getInstance();

    @FXML
    public void initialize() {
        // Configurar eventos para los botones y enlaces
        setupButtons();


    }

    private void setupButtons() {
        createButton.setOnAction(event -> handleCreateGroup());
        backButton.setOnAction(event -> closeWindow());
    }

    private void handleCreateGroup() {
        String name = nameField.getText().trim();
        String description = descriptionfield.getText().trim();
        logger.log("Validando los campos de crear un grupo");

        if (name.isEmpty() || description.isEmpty()) {
            showAlert("Error", "Los campos no deben de estar vacíos", Alert.AlertType.INFORMATION);
            return;
        }

        Model_Create_Group data = new Model_Create_Group();
        data.setName(name);
        data.setDescription(description);
        data.setCreator_email(mainController.getUserEmail());
        data.setMembers(mainController.getUsers()); //TODO: Buscar manera de obtener mediante un checkbox los usuarios

        AtomicBoolean success = new AtomicBoolean(false);
        AtomicReference<String> message = new AtomicReference<>("Algo salió mal.");

        groupService.createGroup(data, (args) -> {
            logger.log("Creando un grupo, callback ejecutandose");
            if (args.length >= 2) {
                success.set((Boolean) args[0]);
                message.set((String) args[1]);
                Object rawData = args.length >= 3 ? args[2] : null;

                logger.log("Éxito: " + success);
                logger.log("Mensaje: " + message);
            }
            Platform.runLater(() -> {
                if (success.get()) {
                    showAlert("Creación de grupo", message.get(), Alert.AlertType.INFORMATION);
                    closeWindow();
                }else{
                    showAlert("Error", message.get(), Alert.AlertType.WARNING);
                }
            });
        });
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) createButton.getScene().getWindow();
        stage.close();
    }

    public void setMainController(MainViewController controller) {
        this.mainController = controller;
    }
}
