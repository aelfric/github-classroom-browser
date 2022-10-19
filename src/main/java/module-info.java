module com.frankriccobono {
  requires javafx.controls;
  requires javafx.base;
  requires javafx.fxml;
  requires java.net.http;
  requires com.google.gson;
  requires org.eclipse.jgit;
  requires org.eclipse.jgit.ssh.jsch;
  requires jsch;
  exports com.frankriccobono;
  exports com.frankriccobono.github;
  opens com.frankriccobono;
}