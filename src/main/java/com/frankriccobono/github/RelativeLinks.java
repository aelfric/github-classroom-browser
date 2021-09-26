package com.frankriccobono.github;

import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RelativeLinks {
  private static final String LINK = "link";
  private static final String NEXT = "next";
  private static final String PREV = "prev";
  private static final String LAST = "last";

  public String prev;
  public String next;
  public String last;

  public RelativeLinks(String header) {
    Arrays
      .stream(header.split(",\\s*"))
      .forEach(this::parseLink);
  }

  public RelativeLinks(HttpResponse<?> response){
    this(response
      .headers()
      .firstValue(LINK)
      .orElse(""));
  }

  private void parseLink(String header) {
    Pattern compile = Pattern.compile("<([^>]*)>; rel=\"(.*)\"");
    Matcher matcher = compile.matcher(header);
    if (matcher.find()) {
      String linkUrl = matcher.group(1);
      String rel = matcher.group(2);
      switch (rel) {
        case NEXT:
          this.next = linkUrl;
          break;
        case PREV:
          this.prev = linkUrl;
          break;
        case LAST:
          this.last = linkUrl;
          break;
      }
    }
  }

  @Override
  public String toString() {
    return "RelativeLinks{" +
      "prev='" + prev + '\'' +
      ", next='" + next + '\'' +
      ", last='" + last + '\'' +
      '}';
  }
}
