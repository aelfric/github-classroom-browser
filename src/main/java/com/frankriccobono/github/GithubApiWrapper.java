package com.frankriccobono.github;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import static com.frankriccobono.App.basicAuth;

public class GithubApiWrapper {
  String ACCESS_TOKEN = System.getenv("GITHUB_ACCESS_TOKEN");
  HttpClient client = HttpClient.newHttpClient();
  Gson gson = new Gson();

  public List<Repository> getAllRepos(final String orgName){
    try {
      HttpResponse<String> response = doGet(
        String.format("https://api.github.com/orgs/%s/repos", orgName));

      RelativeLinks links = new RelativeLinks(response);

      ArrayList<Repository> repositories = new ArrayList<>(parseRepos(response));

      while (links.next != null) {
        System.out.println("Retrieving page...");
        HttpResponse<String> nextResponse = doGet(links.next);
        repositories.addAll(parseRepos(nextResponse));
        links = new RelativeLinks(nextResponse);
      }

      System.out.println("Loaded repositories " + repositories.size());
      return repositories;
    } catch (IOException | InterruptedException e){
      throw new RuntimeException("Could not load repositories", e);
    }
  }

  public void deleteRepository(Repository repository){
    try {
      HttpResponse<String> stringHttpResponse = doDelete(repository.url);
      System.out.println(stringHttpResponse.statusCode());
      System.out.println(stringHttpResponse.body());
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException("Could not delete repository " + repository, e);
    }
  }

  private List<Repository> parseRepos(HttpResponse<String> response) {
    Type type = new TypeToken<List<Repository>>() {}.getType();
    return gson.fromJson(response.body(), type);
  }

  private HttpResponse<String> doGet(String url) throws java.io.IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create(url))
      .header("Authorization", basicAuth("aelfric", ACCESS_TOKEN))
      .build();

    return client.send(request, HttpResponse.BodyHandlers.ofString());
  }

  private HttpResponse<String> doDelete(String url) throws java.io.IOException,
    InterruptedException {
    HttpRequest request = HttpRequest.newBuilder()
      .DELETE()
      .uri(URI.create(url))
      .header("Authorization", basicAuth("aelfric", ACCESS_TOKEN))
      .build();

    return client.send(request, HttpResponse.BodyHandlers.ofString());
  }
}
