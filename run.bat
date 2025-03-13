@echo off
cd java_proj
java --module-path dependency-jars --add-modules javafx.controls,javafx.fxml,com.google.gson,org.eclipse.paho.client.mqttv3 -jar target/java_proj-1.0-SNAPSHOT.jar
pause
