package com.frankriccobono.github;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RelativeLinks {
  public String prev;
  public String next;
  public String last;

  RelativeLinks(String header){
    Arrays
      .stream(header.split(",\\s*"))
      .forEach(this::parseLink);
  }

  private void parseLink(String header) {
    try {
      Pattern compile = Pattern.compile("<([^>]*)>; rel=\"(.*)\"");
      Matcher matcher = compile.matcher(header);
      matcher.find();
      String linkUrl = matcher.group(1);
      String rel = matcher.group(2);
      switch (rel){
        case "next":
          this.next = linkUrl;
          break;
        case "prev":
          this.prev = linkUrl;
          break;
        case "last":
          this.last = linkUrl;
          break;
      }
    } catch (Exception e){
      e.printStackTrace();
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
