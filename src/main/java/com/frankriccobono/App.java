package com.frankriccobono;

import com.frankriccobono.github.Repository;
import com.frankriccobono.github.RepositoryService;
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

import java.util.Base64;

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

    ToolBar toolBar = new ToolBar();
    toolBar.getItems().add(button);
    FilteredList<Repository> filteredData = new FilteredList<>(getRepositories(), s -> true);

    TextField filterInput = new TextField();
    filterInput.textProperty().addListener(obs->{
      String filter = filterInput.getText();
      if(filter == null || filter.length() == 0) {
        filteredData.setPredicate(s -> true);
      }
      else {
        filteredData.setPredicate(s -> s.full_name.contains(filter));
      }
    });

    ListView<Repository> listView = new ListView<>(filteredData);
    listView.setCellFactory(view -> new RepositoryCell());

    listView.getSelectionModel().selectedItemProperty().addListener(
      new ChangeListener<Repository>() {
        @Override
        public void changed(ObservableValue<? extends Repository> observableValue,
                            Repository repository,
                            Repository t1) {

          Repository selectedItem = listView.getSelectionModel().getSelectedItem();
          System.out.println("Selected " + selectedItem);
        }
      }
    );

    listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    listView.setOnMouseClicked(new EventHandler<MouseEvent>() {

                                 @Override
                                 public void handle(MouseEvent event) {
                                   System.out.println("clicked on " + listView.getSelectionModel().getSelectedItem());
                                 }
                               });

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
      if(now == Worker.State.SUCCEEDED){
        repositories.setAll(service.getValue());
      }
    });


    return repositories;
  }
  public static class RepositoryCell extends ListCell<Repository>{
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
}