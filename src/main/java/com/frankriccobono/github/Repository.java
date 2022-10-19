package com.frankriccobono.github;

import com.google.gson.annotations.SerializedName;

public class Repository {
  public long id;
  public String name;
  public String full_name;
  @SerializedName("private")
  public boolean isPrivate;
  @SerializedName("ssh_url")
  public String sshUrl;
  public String  url;

  @Override
  public String toString() {
    return "Repository{" +
      "id=" + id +
      ", name='" + name + '\'' +
      ", url='" + url + '\'' +
      '}';
  }
}
