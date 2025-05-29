package appmain.ui;

import appmain.ui.Controllers.*;
import appmain.ui.model.*;
import appmain.ui.services.MessageService;
import appmain.ui.services.Services;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import utils.Logger;

import java.io.IOException;
import java.util.*;


public class MainViewController {

    @FXML private BorderPane mainContainer;
    @FXML private VBox sidebarContainer;
    @FXML private TabPane contactsTabPane;
    @FXML private VBox contactsContainer;
    @FXML private VBox groupsContainer;
    @FXML private StackPane contentArea;
    @FXML private VBox welcomeScreen;
    @FXML private BorderPane chatScreen;
    @FXML private VBox loginScreen;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Button settingsButton;
    @FXML private Button requestButton;
    @FXML private Button profileButton;
    @FXML private Label usernameLabel;
    @FXML private Circle profilePic;
    @FXML private TextField searchField;
    @FXML private Button addGroupButton;
    @FXML private TextField messageField;
    @FXML private Button sendButton;
    @FXML private Button attachButton;

    private RegisterViewController registerViewController;
    Services services = Services.getInstance();
    Logger logger = Logger.getInstance();

    //usuarios
    private List<Model_User_With_Status> users;
    private List<Model_Join_Group> requests = new ArrayList<>();
    private List<Model_My_Groups> groups = new ArrayList<>();


    // Variables para los datos del perfil y estado de autenticación
    private boolean isLogin = false;

    //TODO: Add image later
    private String username = "";
    private int userId;
    private String email = "";
    private String status = "online";

    //For send messages
    private String receiverEmail = "";
    private MessageService messageService;

    private final Map<String, Model_ChatData> chatMap = new HashMap<>();

    @FXML
    public void initialize() {
        registerViewController = new RegisterViewController();
        users = new ArrayList<>();
        services.setMainViewController(this);
        messageService = new MessageService();

        // Configurar la interfaz según el estado de autenticación
        updateAuthenticationState();

        // Configurar listeners para los botones de autenticación
        setupAuthButtons();

        // Configurar listener para el botón de perfil
        setupProfileButton();

        // Cargar contactos y grupos de ejemplo
        loadSampleGroups();

        // Configurar el campo de mensaje y el botón de enviar
        setupChatControls();
    }


    private void updateAuthenticationState() {
        if (!isLogin) {
            // Mostrar pantalla de login y ocultar otras vistas
            loginScreen.setVisible(true);
            welcomeScreen.setVisible(false);
            chatScreen.setVisible(false);
            sidebarContainer.setVisible(false);

            // Ocultar controles de la barra superior
            profileButton.setVisible(false);
            settingsButton.setVisible(false);
            requestButton.setVisible(false);
        } else {
            // Ocultar pantalla de login y mostrar interfaz principal
            loginScreen.setVisible(false);
            welcomeScreen.setVisible(true);
            sidebarContainer.setVisible(true);

            // Mostrar controles de la barra superior
            profileButton.setVisible(true);
            settingsButton.setVisible(true);
            requestButton.setVisible(true);

            // Actualizar información del usuario
            usernameLabel.setText(username.isEmpty() ? "Usuario" : username);
        }
    }

    private void setupAuthButtons() {
        loginButton.setOnAction(event -> openLoginForm());
        registerButton.setOnAction(event -> openRegisterForm());
        addGroupButton.setOnAction(event -> openNewGroupForm());
        settingsButton.setOnAction(event -> openSettingsView());
        attachButton.setOnAction(event -> openSubmitFile() );
        requestButton.setOnAction(event -> openRequestForm());
    }

    private void setupProfileButton() {
        profileButton.setOnAction(event -> openProfileForm());
    }

    private void setupChatControls() {
        sendButton.setOnAction(event -> {
            String message = messageField.getText().trim();
            if (!message.isEmpty()) {
                sendMessage(message);
                messageField.clear();
            }
        });

        messageField.setOnAction(event -> {
            String message = messageField.getText().trim();
            if (!message.isEmpty()) {
                sendMessage(message);
                messageField.clear();
            }
        });
    }

    public void openLoginForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/appmain/ui/Registration/login-view.fxml"));
            Scene loginScene = new Scene(loader.load(), 400, 500);

            LoginViewController controller = loader.getController();
            controller.setMainController(this);

            Stage loginStage = new Stage();
            loginStage.initStyle(StageStyle.UNDECORATED);
            loginStage.initModality(Modality.APPLICATION_MODAL);
            loginStage.setScene(loginScene);
            loginStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openRegisterForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/appmain/ui/Registration/register-view.fxml"));
            Scene registerScene = new Scene(loader.load(), 450, 650);

            RegisterViewController controller = loader.getController();
            controller.setMainController(this);

            Stage registerStage = new Stage();
            registerStage.initStyle(StageStyle.UNDECORATED);
            registerStage.initModality(Modality.APPLICATION_MODAL);
            registerStage.setScene(registerScene);
            registerStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openProfileForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/appmain/ui/Profile/profile-view.fxml"));
            Scene profileScene = new Scene(loader.load(), 500, 700);

            ProfileViewController controller = loader.getController();
            controller.setUserData(username, email, status);
            controller.setMainController(this);

            Stage profileStage = new Stage();
            profileStage.initStyle(StageStyle.UNDECORATED);
            profileStage.initModality(Modality.APPLICATION_MODAL);
            profileStage.setScene(profileScene);
            profileStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openRequestForm(){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/appmain/ui/Registration/request-view.fxml"));
            Scene requestScene = new Scene(loader.load(), 500, 700);

            RequestViewController controller = loader.getController();
            controller.setRequests(getRequests());
            controller.setInvitedEmail(email);

            Stage requestStage= new Stage();
            requestStage.initStyle(StageStyle.UNDECORATED);
            requestStage.initModality(Modality.APPLICATION_MODAL);
            requestStage.setScene(requestScene);
            requestStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openNewGroupForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/appmain/ui/Registration/group-view.fxml"));
            Scene groupScene = new Scene(loader.load(), 400, 500);

            GroupViewController controller = loader.getController();
            controller.setMainController(this);

            Stage groupStage= new Stage();
            groupStage.initStyle(StageStyle.UNDECORATED);
            groupStage.initModality(Modality.APPLICATION_MODAL);
            groupStage.setScene(groupScene);
            groupStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openSettingsView() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Configuración del usuario");
        alert.setHeaderText(null);
        alert.setContentText("Funcionalidad no implementada todavía.");
        alert.showAndWait();
    }

    private void openSubmitFile() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Adjuntar documento");
        alert.setHeaderText(null);
        alert.setContentText("Funcionalidad no implementada todavía.");
        alert.showAndWait();
    }

    public void onLoginSuccess(Model_User user) {
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.userId = user.getId();
        this.isLogin = true;
        updateAuthenticationState();
    }

    // Método llamado desde ProfileViewController cuando se actualizan los datos del perfil
    public void updateProfileInfo(String username, String email, String fullName, String status) {
        this.username = username;
        this.email = email;
        this.status = status;
        usernameLabel.setText(username);
    }


    //TODO: Implementar logica para traer los ejemplso reales
    private void loadSampleGroups() {
        // Este método cargará grupos de ejemplo en el sidebar
        // En una implementación real, estos vendrían de una base de datos o servicio
        String[] sampleGroups = {"Proyecto Final", "Estudio Matemáticas", "Club de Debate", "Taller de Programación"};

        for (String name : sampleGroups) {
            HBox groupItem = createContactItem(name, "3 miembros", true);
            groupsContainer.getChildren().add(groupItem);
        }
    }

    private HBox createContactItem(String name, String status, boolean isGroup) {
        HBox itemContainer = new HBox();
        itemContainer.getStyleClass().add("contact-item");

        Circle profileCircle = new Circle(20);
        profileCircle.getStyleClass().add("contact-pic");

        VBox infoContainer = new VBox();
        infoContainer.getStyleClass().add("contact-info");

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("contact-name");

        Label statusLabel = new Label(status);
        statusLabel.getStyleClass().add("contact-status");

        infoContainer.getChildren().addAll(nameLabel, statusLabel);

        Label unreadIndicator = new Label("●");
        unreadIndicator.getStyleClass().add("unread-indicator");
        unreadIndicator.setVisible(false);

        itemContainer.getChildren().addAll(profileCircle, infoContainer, unreadIndicator);


        // Agregar evento de clic para abrir el chat
        itemContainer.setOnMouseClicked(event -> openChat(name, status, isGroup));

        return itemContainer;
    }

    public void updateUserList(List<Model_User_With_Status> users) {
        Platform.runLater(() -> {
            contactsContainer.getChildren().clear();
            for (Model_User_With_Status user : users) {
                String online = user.isOnline() ? "online" : "offline";
                HBox userBox = createContactItem(user.getUser().getUsername(), online, false);
                contactsContainer.getChildren().add(userBox);
            }
            this.users = users;
        });
    }

    public void updateGroupList(List<Model_My_Groups> groups) {
        Platform.runLater(() -> {
            groupsContainer.getChildren().clear();
            for (Model_My_Groups group : groups) {
                int totalMembers = group.getTotalMembers();
                HBox groupBox = createContactItem(group.getName() + " by " + group.getCreator(), Integer.toString(totalMembers), true);
                groupsContainer.getChildren().add(groupBox);
            }
            this.groups = groups;
        });
    }


    private void openChat(String name, String status, boolean isGroup) {
        // Cambiar a la vista de chat y actualizar la información
        welcomeScreen.setVisible(false);
        chatScreen.setVisible(true);

        // Actualizar la información del encabezado del chat
        Label chatNameLabel = (Label) chatScreen.lookup("#chatNameLabel");
        Label chatStatusLabel = (Label) chatScreen.lookup("#chatStatusLabel");

        if (chatNameLabel != null) chatNameLabel.setText(name);
        if (chatStatusLabel != null) {
            if (isGroup) {
                chatStatusLabel.setText(status);
            } else {
                // Buscar el estado real del usuario
                String realStatus = "offline"; // por defecto
                for (Model_User_With_Status user : users) {
                    if (user.getUser().getUsername().equals(name)) {
                        realStatus = user.isOnline() ? "online" : "offline";
                        setReceiverEmail(user.getUser().getEmail());
                        break;
                    }
                }
                chatStatusLabel.setText(realStatus);
            }
        }

        String chatReference = isGroup ? name : getReceiverEmail();
        Model_ChatData chatData = chatMap.computeIfAbsent(chatReference, Model_ChatData::new);

        VBox messagesContainer = (VBox) chatScreen.lookup("#messagesContainer");
        messagesContainer.getChildren().clear();

        for (ChatMessage msg : chatData.getMessages()) {
            displayMessageInChat(msg.getMessage(), msg.isSent());
        }

        chatData.setHasUnreadMessages(false);
        //actualizar indicador de mensajes
        updateContactUnreadIndicator(name, false);
    }

    private void sendMessage(String message) {
        if (message == null || message.trim().isEmpty()) return;

        //TODO: Add logic for groups
        Model_Message msg = new Model_Message();
        msg.setMessage(message);
        msg.setSenderEmail(email);
        msg.setReceiverEmail(getReceiverEmail());

        Model_ChatData chat = chatMap.computeIfAbsent(getReceiverEmail(), Model_ChatData::new);
        chat.addMessage(msg);

        displayMessageInChat(message, msg.isSent());

        messageService.sendMessage(msg, (args) -> {
            logger.log("Mensaje enviado a:" + msg.getReceiverEmail());
        });

    }

    private void displayMessageInChat(String message, boolean isSent) {
        VBox messagesContainer = (VBox) chatScreen.lookup("#messagesContainer");

        HBox messageBox = new HBox();
        messageBox.setMaxWidth(Double.MAX_VALUE);

        Label messageText = new Label(message);

        messageText.setWrapText(true);
        messageText.setMaxWidth(Double.MAX_VALUE);

        HBox.setHgrow(messageText, Priority.ALWAYS);

        if (isSent) {
            messageText.getStyleClass().addAll("message-bubble", "sent-message");
            messageBox.setAlignment(Pos.CENTER_RIGHT);
        } else {
            messageText.getStyleClass().addAll("message-bubble", "received-message");
            messageBox.setAlignment(Pos.CENTER_LEFT);
        }

        messageBox.getChildren().add(messageText);
        messagesContainer.getChildren().add(messageBox);
    }

    public void receiveMessage(Model_Receive_Message message) {
        if (message == null || message.getMessage().trim().isEmpty()) return;
        //TODO: Add logic for groups
        String senderEmail = message.getSenderEmail();
        Model_ChatData chat = chatMap.computeIfAbsent(senderEmail, Model_ChatData::new);
        chat.addMessage(message);


        if(!senderEmail.equals(getReceiverEmail())) {
            chat.setHasUnreadMessages(true);
            //actualizar contador de mensajes
            for (Model_User_With_Status user : users) {
                if (user.getUser().getEmail().equals(senderEmail)) {
                    updateContactUnreadIndicator(user.getUser().getUsername(), true);
                }
            }
        }

        if(senderEmail.equals(getReceiverEmail())) {
            displayMessageInChat(message.getMessage(), message.isSent());
        }
    }

    private void updateContactUnreadIndicator(String chatId, boolean hasUnread) {
        for (Node node : contactsContainer.getChildren()) {
            if (node instanceof HBox hbox) {
                Label nameLabel = (Label) hbox.lookup(".contact-name");
                if (nameLabel != null && nameLabel.getText().equals(chatId)) {
                    Label unreadIndicator = (Label) hbox.lookup(".unread-indicator");
                    if (unreadIndicator != null) {
                        unreadIndicator.setVisible(hasUnread);
                    }
                    break;
                }
            }
        }
    }

    //Some getters and setters
    public List<Model_User_With_Status> getUsers() {
        return users;
    }

    public void setUsers(List<Model_User_With_Status> users) {
        this.users = users;
    }

    public String getReceiverEmail() {
        return receiverEmail;
    }

    public void setReceiverEmail(String receiverEmail) {
        this.receiverEmail = receiverEmail;
    }

    public String getUserEmail() {
        return email;
    }

    public List<Model_Join_Group> getRequests() {
        return requests;
    }

    public void setRequests(List<Model_Join_Group> requests) {
        this.requests = requests;
    }
}