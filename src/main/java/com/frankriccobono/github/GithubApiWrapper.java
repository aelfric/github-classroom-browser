package com.frankriccobono.github;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;


public class GithubApiWrapper {

  static Logger logger = LoggerFactory.getLogger(GithubApiWrapper.class);
  private final HttpClient client = HttpClient.newHttpClient();
  private final ObjectMapper mapper = new ObjectMapper();

  private static String basicAuth(String username, String password) {
    return "Basic " + Base64.getEncoder()
      .encodeToString((username + ":" + password).getBytes());
  }

  public List<Repository> getAllRepos(final String orgName) throws InterruptedException {
    try {
      HttpResponse<String> response = doGet(
        String.format("https://api.github.com/orgs/%s/repos?per_page=100&sort=full_name",
          orgName));

      RelativeLinks links = new RelativeLinks(response);

      ArrayList<Repository> repositories = new ArrayList<>(parseRepos(response));

      while (links.next != null) {
        logger.info("Retrieving page...");
        HttpResponse<String> nextResponse = doGet(links.next);
        repositories.addAll(parseRepos(nextResponse));
        links = new RelativeLinks(nextResponse);
      }

      logger.info("Loaded repositories {}", repositories.size());
      return repositories;
    } catch (IOException e) {
      throw new IllegalStateException("Could not load repositories", e);
    }
  }

  public void deleteRepository(Repository repository) throws InterruptedException {
    try {
      HttpResponse<String> response = doDelete(repository.url());
      String body = response.body();
      logger.info("{}\n{}", response.statusCode(), body);
    } catch (IOException e) {
      throw new IllegalStateException("Could not delete repository " + repository, e);
    }
  }

  private List<Repository> parseRepos(HttpResponse<String> response) {
    try {
      return mapper.readValue(
          response.body(),
          new TypeReference<>(){}
      );
    } catch (JsonProcessingException e) {
      logger.error("Could not parse repository", e);
      return Collections.emptyList();
    }
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
