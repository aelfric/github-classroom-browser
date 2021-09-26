package com.frankriccobono.github;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class GithubApiWrapper {
  private final HttpClient client = HttpClient.newHttpClient();
  private final Gson gson = new Gson();

  public GithubApiWrapper() {
  }

  private static String basicAuth(String username, String password) {
    return "Basic " + Base64.getEncoder()
      .encodeToString((username + ":" + password).getBytes());
  }

  public List<Repository> getAllRepos(final String orgName) {
    try {
      HttpResponse<String> response = doGet(
        String.format("https://api.github.com/orgs/%s/repos?per_page=100&sort=full_name",
          orgName));

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
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException("Could not load repositories", e);
    }
  }

  public void deleteRepository(Repository repository) {
    try {
      HttpResponse<String> response = doDelete(repository.url);
      System.out.println(response.statusCode());
      System.out.println(response.body());
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException("Could not delete repository " + repository, e);
    }
  }

  private List<Repository> parseRepos(HttpResponse<String> response) {
    Type type = new TypeToken<List<Repository>>() {
    }.getType();
    return gson.fromJson(response.body(), type);
  }

  private HttpResponse<String> doGet(String url) throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create(url))
      .header("Authorization", basicAuth("aelfric", EnvironmentConstants.ACCESS_TOKEN))
      .build();

    return client.send(request, BodyHandlers.ofString());
  }

  private HttpResponse<String> doDelete(String url) throws IOException,
    InterruptedException {
    HttpRequest request = HttpRequest.newBuilder()
      .DELETE()
      .uri(URI.create(url))
      .header("Authorization", basicAuth("aelfric", EnvironmentConstants.ACCESS_TOKEN))
      .build();

    return client.send(request, BodyHandlers.ofString());
  }
}
