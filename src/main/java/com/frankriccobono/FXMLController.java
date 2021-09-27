package com.frankriccobono;

import com.frankriccobono.github.Repository;
import com.frankriccobono.github.RepositoryService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

public class FXMLController {
  File destinationDir;

  private final RepositoryService service = new RepositoryService();

  @FXML
  ListView<Repository> repoListView;

  private ObservableList<Repository> repositories = FXCollections.observableArrayList();

  private FilteredList<Repository> filteredList;

  public void initialize(){
    service.stateProperty().addListener(
        observable -> {
          Worker.State now = service.getState();
          System.out.println("State of service = " + now);
          if (now == Worker.State.SUCCEEDED) {
            repositories.setAll(service.getValue());
          }
        }
    );
    service.start();

    filteredList = new FilteredList<>(repositories, s -> true);
    repoListView.setItems(filteredList);
    repoListView.setCellFactory(view -> new App.RepositoryCell());
  }

  @FXML
  protected void handleUpdateFilter(KeyEvent event) {
    final TextField source = (TextField) event.getSource();
    final String filter = source.getText();
    if (filter == null || filter.length() == 0) {
      filteredList.setPredicate(s -> true);
    } else {
      filteredList.setPredicate(s -> s.full_name.contains(filter));
    };
  }

  @FXML
  protected void handleDirectoryChange(ActionEvent event) {
    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    DirectoryChooser directoryChooser = new DirectoryChooser();
    destinationDir = directoryChooser.showDialog(stage);
  }

  @FXML
  protected void handleRefresh(ActionEvent event) {
    final Repository repo1 = new Repository();
    repo1.full_name = "Refresh " + System.currentTimeMillis();
    repositories.add(repo1);

  }

  @FXML
  protected void handleClone(ActionEvent event) {

  }

  @FXML
  protected void handleDelete(ActionEvent event) {

  }
}
