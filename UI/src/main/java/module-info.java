module p2p.ui {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires engine.io.client;
    requires socket.io.client;
    requires com.google.gson;
    requires java.desktop;

    opens appmain.ui to javafx.fxml, com.google.gson;
    opens appmain.ui.model to com.google.gson;

    opens appmain.ui.Controllers to javafx.fxml;
    exports appmain.ui;
    exports appmain.ui.Controllers;
}