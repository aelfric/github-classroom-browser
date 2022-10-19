package com.frankriccobono.github;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Repository(
  long id,
  String name,
  String full_name,
  @JsonProperty("private")
  boolean isPrivate,
  @JsonProperty("ssh_url")
  String sshUrl,
  String  url
){
  @Override
  public String toString() {
    return "Repository{" +
      "id=" + id +
      ", name='" + name + '\'' +
      ", url='" + url + '\'' +
      '}';
  }
}
