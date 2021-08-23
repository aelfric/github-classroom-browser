package com.frankriccobono.github;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class RepositoryService extends Service<ObservableList<Repository>> {

  public RepositoryService() {
  }

  @Override
  protected Task<ObservableList<Repository>> createTask() {
    return new SingleRetrieverTask();
  }

  private static class SingleRetrieverTask extends Task<ObservableList<Repository>>{

    public SingleRetrieverTask() {
    }

    @Override
    protected ObservableList<Repository> call() {
      GithubApiWrapper api = new GithubApiWrapper();
      return FXCollections.observableArrayList(api.getAllRepos("SIT-EE552-WS"));
    }

  }
}
