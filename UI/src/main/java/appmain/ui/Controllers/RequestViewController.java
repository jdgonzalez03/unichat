package appmain.ui.Controllers;

import appmain.ui.model.Model_Join_Group;
import appmain.ui.model.Model_Join_Group_Response;
import appmain.ui.services.GroupService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.*;
import utils.Logger;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class RequestViewController {
    @FXML
    private Button backButtonRequest;

    @FXML
    private VBox requestListContainer;

    private List<Model_Join_Group> requests;
    private String invitedEmail = "";

    private Logger logger = Logger.getInstance();
    private GroupService groupService = new GroupService();


    @FXML
    public void initialize() {
        setupButtons();
    }

    public void setupButtons() {
        backButtonRequest.setOnAction(event -> closeWindow());
    }

    private void closeWindow() {
        Stage stage = (Stage) backButtonRequest.getScene().getWindow();
        stage.close();
    }

    private void renderRequests() {
        requestListContainer.getChildren().clear();
        for (Model_Join_Group req : requests) {
            VBox requestItem = new VBox(5);
            requestItem.getStyleClass().add("request-item");

            Label label = new Label(req.getName_group() + " - " + req.getDescription_group());
            label.getStyleClass().add("request-label");

            HBox buttonsBox = new HBox(10);
            buttonsBox.setAlignment(Pos.CENTER_LEFT);

            Button acceptButton = new Button("Aceptar");
            acceptButton.getStyleClass().add("accept-button");
            acceptButton.setOnAction(e -> handleAcceptOrReject(req, true));

            Button rejectButton = new Button("Rechazar");
            rejectButton.getStyleClass().add("reject-button");
            rejectButton.setOnAction(e -> handleAcceptOrReject(req, false));

            buttonsBox.getChildren().addAll(acceptButton, rejectButton);
            requestItem.getChildren().addAll(label, buttonsBox);

            requestListContainer.getChildren().add(requestItem);
        }
    }

    private void handleAcceptOrReject(Model_Join_Group req, boolean accept) {

        if (accept) {
            logger.log("Aceptado: " + req.getName_group());
        }else{
            logger.log("Rechazado: " + req.getName_group());
        }
        Model_Join_Group_Response data = new Model_Join_Group_Response();

        data.setAccepted(accept);
        data.setGroupName(req.getName_group());
        data.setInvitedEmail(getInvitedEmail());

        AtomicBoolean success = new AtomicBoolean(false);
        AtomicReference<String> message = new AtomicReference<>("Algo salió mal.");

        groupService.respondRequestToJoinGroup(data, (args) -> {
            if (args.length >= 2) {
                success.set((Boolean) args[0]);
                message.set((String) args[1]);

                logger.log("Éxito: " + success);
                logger.log("Mensaje: " + message);
            }
            Platform.runLater(() -> {
                if (success.get()) {
                    showAlert("Invitaciones de grupo", message.get(), Alert.AlertType.INFORMATION);
                    requests.remove(req);
                    renderRequests();
                }else{
                    showAlert("Error", message.get(), Alert.AlertType.WARNING);
                }
            });
        });
    }



    public void setRequests(List<Model_Join_Group> requests) {
        this.requests = requests;
        renderRequests();
    }

    public String getInvitedEmail() {
        return invitedEmail;
    }

    public void setInvitedEmail(String invitedEmail) {
        this.invitedEmail = invitedEmail;
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
