package appmain.ui.Controllers;

import appmain.ui.model.Model_Join_Group;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.*;

import java.util.List;

public class RequestViewController {
    @FXML
    private Button backButtonRequest;

    @FXML
    private VBox requestListContainer;

    private List<Model_Join_Group> requests;



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
            acceptButton.setOnAction(e -> handleAccept(req));

            Button rejectButton = new Button("Rechazar");
            rejectButton.getStyleClass().add("reject-button");
            rejectButton.setOnAction(e -> handleReject(req));

            buttonsBox.getChildren().addAll(acceptButton, rejectButton);
            requestItem.getChildren().addAll(label, buttonsBox);

            requestListContainer.getChildren().add(requestItem);
        }
    }

    // Métodos manejadores (debes implementar la lógica)
    private void handleAccept(Model_Join_Group req) {
        System.out.println("Aceptado: " + req.getName_group());
        // lógica adicional aquí
    }

    private void handleReject(Model_Join_Group req) {
        System.out.println("Rechazado: " + req.getName_group());
        // lógica adicional aquí
    }


    public void setRequests(List<Model_Join_Group> requests) {
        this.requests = requests;
        renderRequests();
    }
}
