package com.frankriccobono;

import com.frankriccobono.github.GithubApiWrapper;
import com.frankriccobono.github.Repository;
import com.frankriccobono.github.RepositoryService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class FXMLController {
    static Logger logger = LoggerFactory.getLogger(FXMLController.class);

    private final RepositoryService service = new RepositoryService();
    File destinationDir;
    @FXML
    ListView<Repository> repoListView;

    @FXML
    Text selectedDirectory;

    @FXML
    ProgressBar progressBar;

    private final ObservableList<Repository> repositories = FXCollections.observableArrayList();

    private FilteredList<Repository> filteredList;

    public void initialize() {
        service.stateProperty().addListener(
            observable -> {
                Worker.State now = service.getState();
                logger.info("State of service = {}", now);
                if (now == Worker.State.SUCCEEDED) {
                    repositories.setAll(service.getValue());
                }
            }
        );
        service.start();

        filteredList = new FilteredList<>(repositories, s -> true);
        repoListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        repoListView.setItems(filteredList);
        repoListView.setCellFactory(view -> new RepositoryCell());
    }

    @FXML
    protected void handleUpdateFilter(KeyEvent event) {
        final TextField source = (TextField) event.getSource();
        final String filter = source.getText();
        filteredList.setPredicate(
            s -> filter == null ||
                filter.length() == 0 ||
                s.full_name().contains(filter));
    }

    @FXML
    protected void handleDirectoryChange(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(destinationDir);
        destinationDir = directoryChooser.showDialog(stage);
        this.selectedDirectory.setText(destinationDir.getAbsolutePath());
    }

    @FXML
    protected void handleRefresh(ActionEvent event) {
        service.restart();
    }

    @FXML
    protected void handleClone(ActionEvent event) {
        ObservableList<Repository> selectedItems =
            repoListView.getSelectionModel().getSelectedItems();
        int total = selectedItems.size();
        double count = 0.0;
        for (Repository repo : selectedItems) {
            cloneOrPullRepo(repo.sshUrl(), repo.name());
            count++;
            progressBar.setProgress(count / total);
        }
        progressBar.setProgress(1.0);
    }

    @FXML
    protected void handleDelete(ActionEvent event) throws InterruptedException {
        ObservableList<Repository> selectedItems =
            repoListView.getSelectionModel().getSelectedItems();
        int total = selectedItems.size();
        double count = 0.0;
        for (Repository repo : selectedItems) {

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation Dialog");
            alert.setHeaderText("Deleting a Repository");
            alert.setContentText("Are you sure you want to delete " + repo.full_name() + "?");

            ButtonType delete = new ButtonType("Delete");
            ButtonType cancel = new ButtonType("Cancel");

            alert.getButtonTypes().setAll(delete, cancel);

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == delete) {
                logger.warn("DELETING {}", repo);
                if (cloneOrPullRepo(repo.sshUrl(), repo.name())) {
                    GithubApiWrapper githubApiWrapper = new GithubApiWrapper();
                    githubApiWrapper.deleteRepository(repo);
                    count++;
                    progressBar.setProgress(count / total);
                }
            }

        }
        progressBar.setProgress(1.0);   
    }

    public boolean cloneOrPullRepo(String cloneUrl, String repoName) {
        if (destinationDir == null) return false;

        File destination = destinationDir.toPath().resolve(repoName).toFile();

        try {
            if (!destination.exists()) {
                logger.info("Cloning {}", repoName);
                Git call = Git.cloneRepository()
                    .setURI(cloneUrl)
                    .setDirectory(
                        destination
                    )
                    .setTransportConfigCallback(new SshCallback())
                    .call();
                call.close();
                logger.info("Finished cloning {}", repoName);
            } else {
                logger.info("Already cloned {} trying to stash and pull", repoName);
                try(Git repo = Git.open(destination)) {
                    repo
                        .stashCreate()
                        .call();
                    repo
                        .pull()
                        .setTransportConfigCallback(new SshCallback())
                        .call();
                    logger.info("Finished updating {}", repoName);
                }
            }
            return true;
        } catch (GitAPIException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
