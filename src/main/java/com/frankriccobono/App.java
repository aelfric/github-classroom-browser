package com.frankriccobono;

import com.frankriccobono.github.Repository;
import com.frankriccobono.github.RepositoryService;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.util.FS;

import java.nio.file.Paths;
import java.util.Base64;
import java.util.Optional;

/**
 * JavaFX App
 */
public class App extends Application {
  final RepositoryService service = new RepositoryService();

  @Override
  public void start(Stage stage) {
    stage.setTitle("Repositories ");


    Button button = new Button();
    button.setText("Clone");

    Button button2 = new Button();
    button2.setText("Delete");

    ToolBar toolBar = new ToolBar();
    toolBar.getItems().add(button);
    toolBar.getItems().add(button2);
    FilteredList<Repository> filteredData = new FilteredList<>(getRepositories(), s -> true);

    TextField filterInput = new TextField();
    filterInput.textProperty().addListener(obs -> {
      String filter = filterInput.getText();
      if (filter == null || filter.length() == 0) {
        filteredData.setPredicate(s -> true);
      } else {
        filteredData.setPredicate(s -> s.full_name.contains(filter));
      }
    });

    ListView<Repository> listView = new ListView<>(filteredData);
    listView.setCellFactory(view -> new RepositoryCell());

    listView.getSelectionModel().selectedItemProperty().addListener(
      (observableValue, repository, t1) -> {

        Repository selectedItem = listView.getSelectionModel().getSelectedItem();
        System.out.println("Selected " + selectedItem);
      }
    );

    button.setOnMouseClicked((event) -> {
      ObservableList<Repository> selectedItems =
        listView.getSelectionModel().getSelectedItems();
      for (Repository repo : selectedItems) {
        this.cloneRepo(repo.ssh_url, repo.name);
      }
    });
    button2.setOnMouseClicked((event) -> {
      ObservableList<Repository> selectedItems =
       listView.getSelectionModel().getSelectedItems();
      for (Repository repo : selectedItems) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText("Deleting a Repository");
        alert.setContentText("Are you sure you want to delete " + repo.full_name + "?");

        ButtonType delete = new ButtonType("Delete");
        ButtonType cancel = new ButtonType("Cancel");

        alert.getButtonTypes().setAll(delete, cancel);

        Optional<ButtonType> result = alert.showAndWait();

        if(result.isPresent() && result.get() == delete) {
          System.out.println("WARNING - DELETING " + repo);
        }
      }
    });


    listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    BorderPane root = new BorderPane(listView);
    root.setBottom(filterInput);
    root.setTop(toolBar);

    stage.setScene(new Scene(root, 640, 480));
    stage.show();
    System.out.println("Setup complete");
    service.start();
  }

  public static void main(String[] args) {
    launch();

  }

  public static String basicAuth(String username, String password) {
    return "Basic " + Base64.getEncoder()
      .encodeToString((username + ":" + password).getBytes());
  }

  public ObservableList<Repository> getRepositories() {
    ObservableList<Repository> repositories = FXCollections.observableArrayList();
    service.stateProperty().addListener(observable -> {
      Worker.State now = service.getState();
      System.out.println("State of service = " + now);
      if (now == Worker.State.SUCCEEDED) {
        repositories.setAll(service.getValue());
      }
    });


    return repositories;
  }

  public static class RepositoryCell extends ListCell<Repository> {
    @Override
    protected void updateItem(Repository repository, boolean empty) {
      super.updateItem(repository, empty);
      if (repository == null || empty) {
        setText(null);
      } else {
        setText(repository.full_name);
        setItem(repository);
      }
    }
  }

  public void cloneRepo(String cloneUrl, String repoName) {


    try {
      Git.cloneRepository()
        .setURI(cloneUrl)
        .setDirectory(
          Paths.get("E:", "Dropbox", "ECE552", "2021 Spring", repoName).toFile()
        )
        .setTransportConfigCallback(new TransportConfigCallback() {
          @Override
          public void configure(Transport transport) {
            SshTransport sshTransport = (SshTransport) transport;
            sshTransport.setSshSessionFactory(new JschConfigSessionFactory() {
              @Override
              protected void configure(OpenSshConfig.Host host, Session session) {
              }

              @Override
              protected JSch getJSch(OpenSshConfig.Host hc, FS fs) throws JSchException {
                JSch jSch = super.getJSch(hc, fs);
                jSch.addIdentity(
                  System.getenv("SSH_PRIVATE_KEY_FILE"),
                  System.getenv("SSH_PASSPHRASE")
                );
                return jSch;
              }
            });
          }
        })
        .call();
    } catch (GitAPIException e) {
      e.printStackTrace();
    }
  }
}