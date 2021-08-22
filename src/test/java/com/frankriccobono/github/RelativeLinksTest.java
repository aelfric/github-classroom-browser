package com.frankriccobono.github;


class RelativeLinksTest {
  public static void main(String[] args) {
    RelativeLinks
      relativeLinks =
      new RelativeLinks(
        "<https://api.github.com/organizations/74933497/repos?page=2>; rel=\"next\", <https://api.github.com/organizations/74933497/repos?page=21>; rel=\"last\"");
    System.out.println(relativeLinks);
  }

}