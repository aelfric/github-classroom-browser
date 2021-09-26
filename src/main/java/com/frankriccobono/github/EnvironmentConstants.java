package com.frankriccobono.github;

public class EnvironmentConstants {
  private static final String GITHUB_ACCESS_TOKEN = "GITHUB_ACCESS_TOKEN";
  private static final String SSH_PRIVATE_KEY_FILE = "SSH_PRIVATE_KEY_FILE";
  private static final String SSH_PASSPHRASE = "SSH_PASSPHRASE";

  public static final String PRIVATE_KEY = System.getenv(SSH_PRIVATE_KEY_FILE);
  public static final String PASSPHRASE = System.getenv(SSH_PASSPHRASE);
  public static final String ACCESS_TOKEN = System.getenv(GITHUB_ACCESS_TOKEN);

  private EnvironmentConstants() {

  }


}
