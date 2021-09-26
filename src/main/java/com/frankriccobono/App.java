package com.frankriccobono;

import com.frankriccobono.github.EnvironmentConstants;
import com.frankriccobono.github.GithubApiWrapper;
import com.frankriccobono.github.Repository;
import com.frankriccobono.github.RepositoryService;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.util.FS;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * JavaFX App
 */
public class App extends Application {
  private final RepositoryService service = new RepositoryService();
  File destinationDir;

  @Override
  public void start(Stage stage) {

    DirectoryChooser directoryChooser = new DirectoryChooser();
    destinationDir = directoryChooser.showDialog(stage);

    stage.setTitle("Repositories ");

    Button btnClone = new Button();
    btnClone.setText("Clone");

    Button btnDelete = new Button();
    btnDelete.setText("Delete");

    Button btnRefresh = new Button();
    btnRefresh.setText("Refresh");

    ToolBar toolBar = new ToolBar();
    toolBar.getItems().add(btnRefresh);
    toolBar.getItems().add(btnClone);
    toolBar.getItems().add(btnDelete);
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

    btnRefresh.setOnMouseClicked((event) -> service.restart());

    btnClone.setOnMouseClicked((event) -> {
      ObservableList<Repository> selectedItems =
        listView.getSelectionModel().getSelectedItems();
      for (Repository repo : selectedItems) {
        this.cloneOrPullRepo(repo.sshUrl, repo.name);
      }
    });
    btnDelete.setOnMouseClicked((event) -> {
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

        if (result.isPresent() && result.get() == delete) {
          System.out.println("WARNING - DELETING " + repo);
          if(cloneOrPullRepo(repo.sshUrl, repo.name)) {
            GithubApiWrapper githubApiWrapper = new GithubApiWrapper();
            githubApiWrapper.deleteRepository(repo);
          }
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
        setText(repository.full_name + (repository.isPrivate ? " (private)" : ""));
        setItem(repository);
      }
    }
  }

  public boolean cloneOrPullRepo(String cloneUrl, String repoName) {
    if(destinationDir == null) return false;

    File destination = destinationDir.toPath().resolve(repoName).toFile();

    try {
      if (!destination.exists()) {

        Git.cloneRepository()
          .setURI(cloneUrl)
          .setDirectory(
            destination
          )
          .setTransportConfigCallback(new SshCallback())
          .call();
      } else {
        Git repo = Git.open(destination);
        repo
          .pull()
          .setTransportConfigCallback(new SshCallback())
          .call();
      }
      return true;
    } catch (GitAPIException | IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  static class SshCallback implements TransportConfigCallback {
    @Override
    public void configure(Transport transport) {
      SshTransport sshTransport = (SshTransport) transport;
      sshTransport.setSshSessionFactory(new JschConfigSessionFactory() {
        @Override
        protected void configure(OpenSshConfig.Host host, Session session) {
        }

        @Override
        protected JSch getJSch(OpenSshConfig.Host hostConfig,
                               FS filesystem) throws JSchException {
          JSch jSch = super.getJSch(hostConfig, filesystem);
          jSch.addIdentity(
            EnvironmentConstants.PRIVATE_KEY,
            EnvironmentConstants.PASSPHRASE
          );
          return jSch;
        }
      });
    }
  }
}