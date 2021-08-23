package com.frankriccobono.github;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.frankriccobono.App.basicAuth;

public class RepositoryService extends Service<ObservableList<Repository>> {
  String ACCESS_TOKEN = System.getenv("GITHUB_ACCESS_TOKEN");

  public RepositoryService() {
  }

  @Override
  protected Task<ObservableList<Repository>> createTask() {
    return new SingleRetrieverTask(ACCESS_TOKEN);
  }

  private static class SingleRetrieverTask extends Task<ObservableList<Repository>>{

    private final String accessToken;

    public SingleRetrieverTask(String accessToken) {
      this.accessToken = accessToken;
    }

    HttpClient client = HttpClient.newHttpClient();
    Gson gson = new Gson();

    @Override
    protected ObservableList<Repository> call() throws Exception {
      HttpResponse<String> response = getResponse(
        "https://api.github.com/orgs/SIT-EE552-WS/repos");

      HttpHeaders headers = response.headers();
      Map<String, List<String>> map = headers.map();

      RelativeLinks links = new RelativeLinks(map.get("link").get(0));

      ArrayList<Repository> repositories1 = new ArrayList<>(parseRepos(response));

      while (links.next != null){
        System.out.println("Retrieving page...");
        HttpResponse<String> nextResponse = getResponse(links.next);
        repositories1.addAll(parseRepos(nextResponse));
        links = new RelativeLinks(nextResponse.headers().firstValue("link").get());
      }

      System.out.println("Loaded repositories " + repositories1.size());
      return FXCollections.observableArrayList(repositories1);
    }

    private List<Repository> parseRepos(HttpResponse<String> response) {
      Type type = new TypeToken<List<Repository>>() {}.getType();
      return gson.fromJson(response.body(), type);
    }

    private HttpResponse<String> getResponse(String url) throws java.io.IOException, InterruptedException {
      HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .header("Authorization", basicAuth("aelfric", accessToken))
        .build();

      return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
  }
}
