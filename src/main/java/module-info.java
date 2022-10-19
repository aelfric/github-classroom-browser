module com.frankriccobono {
  requires javafx.controls;
  requires javafx.base;
  requires javafx.fxml;
  requires java.net.http;
  requires com.fasterxml.jackson.annotation;
  requires com.fasterxml.jackson.core;
  requires com.fasterxml.jackson.databind;
  requires org.eclipse.jgit;
  requires org.eclipse.jgit.ssh.jsch;
  requires jsch;
  requires java.logging;
  exports com.frankriccobono;
  exports com.frankriccobono.github;
  opens com.frankriccobono;
  opens com.frankriccobono.github;
}