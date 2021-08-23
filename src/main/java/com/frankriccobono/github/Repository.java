package com.frankriccobono.github;

public class Repository {
  public long id;
  public String node_id;
  public String name;
  public String full_name;
  public boolean isPrivate;
  public String  clone_url;
  public String  ssh_url;
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
