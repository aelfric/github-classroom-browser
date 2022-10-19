package com.frankriccobono;

import com.frankriccobono.github.Repository;
import javafx.scene.control.ListCell;

public class RepositoryCell extends ListCell<Repository> {
    @Override
    protected void updateItem(Repository repository, boolean empty) {
        super.updateItem(repository, empty);
        if (repository == null || empty) {
            setText(null);
        } else {
            setText(repository.full_name() + (repository.isPrivate() ? " (private)" : ""));
            setItem(repository);
        }
    }
}
