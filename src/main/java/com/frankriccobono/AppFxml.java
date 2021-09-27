package com.frankriccobono;

import com.frankriccobono.github.RepositoryService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * JavaFX App
 */

public class AppFxml extends Application {
  private final RepositoryService service = new RepositoryService();

  public static void main(String[] args) {
    launch();
  }

  @Override
  public void start(Stage stage) throws IOException {
    final URL resource = getClass().getResource("/browser.fxml");
    Parent root = FXMLLoader.load(Objects.requireNonNull(resource));

    Scene scene = new Scene(root, 640, 480);

    stage.setTitle("FXML Welcome");
    stage.setScene(scene);
    stage.show();
  }
}