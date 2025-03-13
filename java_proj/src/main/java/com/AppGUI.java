package com;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * JavaFX-based GUI application for a drug interaction checker
 */
public class AppGUI extends Application {
    private static final Logger logger = Logger.getLogger(AppGUI.class.getName());
    private static final DatabaseQuery databaseQuery = new DatabaseQuery();
    private TextField textField;
    private ListView<String> suggestions;
    private final Set<String> drugSelection = new HashSet<>();
    private HBox labelRow;
    private ScrollPane scrollPane;
    private final Map<String, String> wikiQueries = new HashMap<>();
    private MQQTClient mqttServer;
    private boolean localdb=false;
    private boolean mqttdb=false;
    private Queryable querySource;
    private Thread serverThread;
    private boolean serverRunning = false;

    /**
     * Starts the JavaFX application by setting up the primary stage
     *
     * @param primaryStage the main application stage
     */
    @Override
    public void start(Stage primaryStage) {
        VBox mainLayout = createMainLayout();
        Scene scene = new Scene(mainLayout, 1200, 1000);
        primaryStage.setTitle("Twosides");
        primaryStage.setScene(scene);
        primaryStage.show();
        openConnectionSelectionWindow();
        querySource = mqttdb ? mqttServer : localdb ? databaseQuery : null;
        if (querySource == null && !serverRunning) {
            logger.severe("No database connection method selected. Please choose either MQTT or Local DB.");
            alertMessage("Error", "No database connection method selected. Please choose either MQTT or Local DB.");
        }
    }

    /**
     * Opens a window for selecting the database connection type (MQTT or MySQL)
     * The user chooses how to connect to the database
     */
    private void openConnectionSelectionWindow() {
        Stage dialogStage=new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Select Connection Type");

        Label label=new Label("Choose how to connect to the database:");
        label.setStyle("-fx-font-size: 14px;");
        
        Button mqttButton=new Button("MQTT Server");
        mqttButton.setOnAction(e -> {
            logger.info("MQTT connection selected");
            showRoleSelectionDialog();
            dialogStage.close();
        });

        Button mysqlButton=new Button("Local MySQL Database");
        mysqlButton.setOnAction(e -> {
            logger.info("MySQL connection selected");
            openMySQLConnectionWindow();
            dialogStage.close();
        });

        VBox layout=new VBox(10, label, mqttButton, mysqlButton);
        layout.setStyle("-fx-padding: 20px; -fx-alignment: center;");

        Scene scene=new Scene(layout, 300, 150);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    /**
     * Opens a role selection dialog allowing the user to choose between:
     * MQTT Client, Server (Local DB), or Both
     */
    private void showRoleSelectionDialog() {
        Stage roleStage = new Stage();
        roleStage.initModality(Modality.APPLICATION_MODAL);
        roleStage.setTitle("Select Role");

        Label roleLabel = new Label("Choose your mode:");
        ToggleGroup roleGroup = new ToggleGroup();

        RadioButton clientRadio = new RadioButton("MQTT Client");
        clientRadio.setToggleGroup(roleGroup);
        RadioButton serverRadio = new RadioButton("Server (Local DB)");
        serverRadio.setToggleGroup(roleGroup);
        RadioButton bothRadio = new RadioButton("Both (Client & Server)");
        bothRadio.setToggleGroup(roleGroup);

        Button continueButton = new Button("Continue");
        continueButton.setOnAction(e -> {
            if (clientRadio.isSelected()) {
                openMQTTBrokerWindow();
            } else if (serverRadio.isSelected()) {
                openDatabaseConfigWindow(false);
            } else if (bothRadio.isSelected()) {
                openDatabaseConfigWindow(true);
            }
            roleStage.close();
        });

        VBox layout = new VBox(10, roleLabel, clientRadio, serverRadio, bothRadio, continueButton);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-alignment: center;");

        Scene scene = new Scene(layout, 300, 200);
        roleStage.setScene(scene);
        roleStage.showAndWait();
    }

    /**
     * Opens a window for entering the MQTT broker URL
     * The user provides the broker details to establish an MQTT connection
     */
    private void openMQTTBrokerWindow() {
        Stage mqttStage = new Stage();
        mqttStage.initModality(Modality.APPLICATION_MODAL);
        mqttStage.setTitle("Enter MQTT Broker");

        Label instructionLabel = new Label("Enter MQTT Broker URL:");
        TextField brokerField = new TextField();
        brokerField.setPromptText("tcp://your-broker-url:1883");
        brokerField.setText("tcp://broker.emqx.io:1883");

        Button connectButton = new Button("Connect");
        connectButton.setOnAction(e -> {
            String brokerUrl = brokerField.getText().trim();
            if (!brokerUrl.isEmpty()) {
                System.out.println("Connecting to MQTT broker: " + brokerUrl);
                serverConnect(brokerUrl);
                mqttdb = true;
                mqttStage.close();
            } else {
                alertMessage("Invalid Input", "Please enter a valid broker URL");
            }
        });

        VBox layout = new VBox(10, instructionLabel, brokerField, connectButton);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-alignment: center;");

        Scene scene = new Scene(layout, 350, 200);
        mqttStage.setScene(scene);
        mqttStage.showAndWait();
    }

    /**
     * Opens a window for configuring the database connection
     * If 'both' is true, it also initializes the MQTT server
     *
     * @param both whether both MQTT and database connections should be initialized
     */
    private void openDatabaseConfigWindow(boolean both) {
        Stage dbStage = new Stage();
        dbStage.initModality(Modality.APPLICATION_MODAL);
        dbStage.setTitle("Database Configuration");

        Label dbLabel = new Label("Enter Database Connection Details:");
        TextField dbHostField = new TextField();
        dbHostField.setPromptText("Database url");
        dbHostField.setText("jdbc:mysql://localhost:3306/mydatabase");

        TextField dbUserField = new TextField();
        dbUserField.setPromptText("Username");

        PasswordField dbPasswordField = new PasswordField();
        dbPasswordField.setPromptText("Password");

        Label instructionLabel = new Label("Enter MQTT Broker URL:");
        TextField brokerField = new TextField();
        brokerField.setPromptText("tcp://your-broker-url:1883");
        brokerField.setText("tcp://broker.emqx.io:1883");

        Button connectButton = new Button("Connect");

        connectButton.setOnAction(e -> {
            String url = dbHostField.getText().trim();
            String user = dbUserField.getText().trim();
            String password = dbPasswordField.getText().trim();
            String broker = brokerField.getText().trim();

            if (!url.isEmpty() && !user.isEmpty() && !password.isEmpty()) {
                logger.info("Connecting to Database at: " + url);
                if (both) {
                    serverInitAndConnect(broker, url, user, password);
                }
                else {
                    logger.info("Starting Server with Database at: " + url);
                    serverRunning = true;
                    startServerInBackground(broker, url, user, password);
                    openServerStatusWindow();
                }
                mqttdb = true;
                dbStage.close();
            } else {
                alertMessage("Invalid Input", "Please enter all database details");
            }
        });

        VBox layout = new VBox(10, dbLabel, dbHostField, dbUserField, dbPasswordField, instructionLabel, brokerField, connectButton);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-alignment: center;");

        Scene scene = new Scene(layout, 350, 250);
        dbStage.setScene(scene);
        dbStage.showAndWait();
    }

    /**
     * Opens a separate window displaying the server status
     * The user can stop the server by clicking "Stop Server"
     */
    private void openServerStatusWindow() {
        Stage serverStage = new Stage();
        serverStage.setTitle("Server Status");
    
        Label statusLabel = new Label("Server is running...");
        Button stopButton = new Button("Stop Server");
    
        stopButton.setOnAction(e -> {
            stopServer();
            serverStage.close();
            System.exit(0);
        });
    
        VBox layout = new VBox(15, statusLabel, stopButton);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-alignment: center;");
    
        Scene scene = new Scene(layout, 250, 150);
        serverStage.setScene(scene);
        serverStage.show();
    }

    /**
     * Starts the server in the background on a separate thread
     * Keeps the server alive while running
     *
     * @param broker   MQTT broker URL
     * @param url      Database connection URL
     * @param user     Database username
     * @param password Database password
     */
    private void startServerInBackground(String broker, String url, String user, String password) {
        serverThread = new Thread(() -> {
            try {
                logger.info("Server running with DB connection...");
                serverInit(broker, url, user, password);
                while (serverRunning) {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.info("Server interrupted");
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
    }
    
    /**
     * Stops the server safely
     * Interrupts the background thread and stops execution
     */
    private void stopServer() {
        serverRunning = false;
        if (serverThread != null) {
            serverThread.interrupt();
        }
        logger.info("Server stopped");
    }
   
    /**
     * Opens a window for entering MySQL database connection details
     * The user provides the database URL, username, and password
     * Upon successful entry, the connection is established
     */
    private void openMySQLConnectionWindow() {
        Stage mysqlStage = new Stage();
        mysqlStage.initModality(Modality.APPLICATION_MODAL);
        mysqlStage.setTitle("Enter MySQL Database Details");

        Label urlLabel = new Label("Database URL:");
        TextField urlField = new TextField();
        urlField.setPromptText("jdbc:mysql://your-host:3306/database");
        urlField.setText("jdbc:mysql://localhost:3306/mydatabase");

        Label userLabel = new Label("Username:");
        TextField userField = new TextField();
        userField.setPromptText("Enter username");

        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");

        Button connectButton = new Button("Connect");
        connectButton.setOnAction(e -> {
            String url = urlField.getText().trim();
            String user = userField.getText().trim();
            String password = passwordField.getText().trim();

            if (url.isEmpty() || user.isEmpty() || password.isEmpty()) {
                alertMessage("Invalid Input", "Please fill in all fields");
            } else {
                DatabaseQuery.connectInitially(url, user, password);
                mysqlStage.close();
                localdb=true;
            }
        });

        VBox layout = new VBox(10, urlLabel, urlField, userLabel, userField, passwordLabel, passwordField, connectButton);
        layout.setStyle("-fx-padding: 20px; -fx-alignment: center;");

        Scene scene = new Scene(layout, 400, 300);
        mysqlStage.setScene(scene);
        mysqlStage.showAndWait();
    }

    /**
     * Establishes a connection to the MQTT broker as a client
     * 
     * @param broker the MQTT broker URL
     */
    private void serverConnect(String broker) {
        try {
            logger.info("Initializing MQTT Client connection...");
            mqttServer=new MQQTClient(broker);
            logger.info("MQTT Client is running and ready to send requests");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to start connect: {0}", e.getMessage());
            alertMessage("Connection error", "Failed to start MQTT Server");
        }
    }

    
    /**
     * Initializes and starts an MQTT server with database connection
     * 
     * @param broker   the MQTT broker URL
     * @param url      the database connection URL
     * @param username the database username
     * @param password the database password
     */
    private void serverInit(String broker, String url, String username, String password) {
        try {
            logger.info("Starting MQTT Server...");
            MQQTServer mqttHandler = new MQQTServer(broker, url, username, password);
            logger.info("MQTT Server is running and ready to accept requests");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to start MQTT Server: {0}", e.getMessage());
            alertMessage("Connection error", "Failed to start MQTT Server");
        }
    }

    /**
     * Initializes both an MQTT server and a client
     * The server is responsible for handling requests, and the client can send messages
     * 
     * @param broker   the MQTT broker URL
     * @param url      the database connection URL
     * @param username the database username
     * @param password the database password
     */
    private void serverInitAndConnect(String broker, String url, String username, String password) {
        try {
            logger.info("Starting MQTT Server...");
            MQQTServer mqttHandler = new MQQTServer(broker, url, username, password);
            logger.info("MQTT Server is running and ready to accept requests");
            mqttServer=new MQQTClient(broker);
            logger.info("MQTT Client is running and ready to send requests");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to start MQTT Server: {0}", e.getMessage());
            alertMessage("Connection error", "Failed to start MQTT Server");
        }
    }

    /**
     * Creates the main layout for the application
     *
     * @return a VBox representing the main layout
     */
    private VBox createMainLayout() {
        VBox vbox = new VBox(10);
        vbox.setStyle("-fx-padding: 20;");
        Label titleSection = createTitleSection();
        VBox.setMargin(titleSection, new Insets(220, 0, 20, 0));
        titleSection.setMaxWidth(Double.MAX_VALUE);
        titleSection.setAlignment(Pos.CENTER);
        ScrollPane scrollPaneSection = createScrollPaneSection();
        VBox inputSection = createInputSection();
        VBox.setMargin(inputSection, new Insets(50, 0, 0, 0));
        inputSection.setAlignment(Pos.CENTER);
        HBox buttonSection = createButtonSection();
        vbox.getChildren().addAll(titleSection, scrollPaneSection, inputSection, buttonSection);
        return vbox;
    }

    /**
     * Creates the title section for the application
     *
     * @return a Label containing the title
     */
    private Label createTitleSection() {
        Label labelTitle = new Label("Drug Interaction Checker");
        labelTitle.setStyle("-fx-font-weight: bold;" +
                            "-fx-font-size: 34px;");
        return labelTitle;
    }

    /**
     * Creates the input section for drug names and suggestions
     *
     * @return a VBox containing the input elements
     */
    private VBox createInputSection() {
        //text field
        textField = new TextField();
        //textField styling
        textField.setStyle("-fx-font-size: 16pt;");
        textField.setPromptText("Enter drug name");
        //suggestions list
        suggestions = new ListView<>();
        //suggestions styling
        suggestions.setMaxHeight(160);
        suggestions.setVisible(false);
        suggestions.setStyle("-fx-font-size: 15pt;");
        //suggestions update
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateSuggestions(newValue);
        });
        //suggestions selection with mouse
        suggestions = selectSuggestionWithMouse(suggestions, textField);

        //key controls for suggestions
        suggestions = keyControlsSuggestion(suggestions, textField);

        //input section
        VBox inputSection = new VBox(textField, suggestions);

        return inputSection;
    }

    /**
     * Updates the suggestions list based on the user input
     *
     * @param newValue the current input from the user
     */
    private void updateSuggestions(String newValue) {
        try {
        if (!newValue.isEmpty()) {
            //get matches from the databasea and filter out the ones that are already selected
            List<String> matches = querySource.queryDrug(newValue, true).stream()
                    .filter(drug -> !drugSelection.contains(drug.toLowerCase()))
                    .collect(Collectors.toList());       
            for (int i = 0; i < matches.size(); i++) {
                matches.set(i, capitalizeEveryFirstLetter(matches.get(i)));
            }
            suggestions.getItems().setAll(matches);

            //suggestions styling
            int rowHeight = 40;
            int maxVisibleRows = 4;
            int numRows = Math.min(matches.size(), maxVisibleRows);
            suggestions.setPrefHeight(numRows*rowHeight);
            suggestions.setVisible(!matches.isEmpty());
        } else {
            suggestions.getItems().clear();
            suggestions.setVisible(false);
        }
    } catch (Exception e) {
        logger.severe("An error occurred while updating the suggestions: " + e.getMessage());
        alertMessage("Error", "Suggestions could not be updated. Check your connection and try again");
    }
    }
    
    /**
     * Enables mouse selection for the suggestions list
     *
     * @param suggestions the ListView containing suggestions
     * @param textField the input field for drug names
     * @return the updated ListView
     */
    private ListView<String> selectSuggestionWithMouse(ListView<String> suggestions, TextField textField) {
        suggestions.setOnMouseClicked(e -> {
            String selectedDrug = suggestions.getSelectionModel().getSelectedItem();
            if (selectedDrug != null) {
                if (drugSelection.contains(selectedDrug.toLowerCase())) {
                    alertMessage("Duplicate Drug", "The drug '" + selectedDrug + "' has already been added");
                }
                else {
                    HBox tag = createTag(capitalizeFirstLetter(selectedDrug));
                    labelRow.getChildren().add(tag);
                    drugSelection.add(selectedDrug.toLowerCase());
                    textField.clear();
                    suggestions.setVisible(false);
                }
            }
        });
        return suggestions;
    }

    /**
     * Applies keyboard controls for the suggestions list
     *
     * @param suggestions the ListView containing suggestions
     * @param textField the input field for drug names
     * @return the updated ListView
     */
    private ListView<String> keyControlsSuggestion( ListView<String> suggestions, TextField textField) {
        textField.setOnKeyPressed(event -> {
            if (suggestions.isVisible()) {
                switch (event.getCode()) {
                    case DOWN:
                        moveToNext(suggestions);
                        break;
                    case UP:
                        moveToPrevious(suggestions);
                        break;
                    case ENTER:
                        selectSuggestion(textField, suggestions);
                        break;
                    default:
                        break;
                }
            }
            else {
                if (!suggestions.isFocused()) {
                    suggestions.setVisible(false);
                    if (event.getCode() == KeyCode.ENTER) {
                        checkManualInput();
                    }
                }
            }
        });
        return suggestions;
    }

    /**
     * Creates the scroll pane section for displaying selected drugs
     *
     * @return a ScrollPane containing the selected drug labels
     */
    private ScrollPane createScrollPaneSection() {
        labelRow = new HBox(10);
        labelRow.setStyle("-fx-padding: 10;" +
                        "-fx-alignment: center-left;");
        scrollPane = new ScrollPane(labelRow);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        return scrollPane;
    }

    /**
     * Capitalizes the first letter of a given string
     *
     * @param input the input string
     * @return the string with its first letter capitalized
     */
    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return Character.toUpperCase(input.charAt(0)) + input.substring(1);
    }

    /**
     * Capitalizes the first letter of every word in the given string
     *
     * @param input the input string
     * @return the string with the first letter of each word capitalized
     */
    private String capitalizeEveryFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }    
        String[] words = input.split("\\s+");    
        StringBuilder capitalized = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                String[] segments = word.split("-");
                for (int i = 0; i < segments.length; i++) {
                    if (!segments[i].isEmpty()) {
                        capitalized.append(Character.toUpperCase(segments[i].charAt(0)))
                                   .append(segments[i].substring(1).toLowerCase());
                    }
                    if (i < segments.length - 1) {
                        capitalized.append("-");
                    }
                }
                capitalized.append(" ");
            }
        }    
        return capitalized.toString().trim();
    }

    /**
     * Creates a tag element for a selected drug
     *
     * @param labelText the text to display in the tag
     * @return an HBox representing the tag
     */
    private HBox createTag(String labelText) {
        //local vars
        Label label = new Label(labelText);
        Button closeButton = new Button("x");
        HBox tagContainer = new HBox(label, closeButton);

        //styling
        label.setStyle("-fx-text-fill: white;" + 
                    "-fx-font-size: 12px;");
        closeButton.setStyle("-fx-background-color: transparent;"+ 
                            "-fx-text-fill: white;"+ 
                            "-fx-font-size: 12px;"+ 
                            "-fx-cursor: hand;");
        tagContainer.setStyle("-fx-background-color: #0078d7;"+ 
                            "-fx-padding: 5 10;" +
                            "-fx-background-radius: 15;"+ 
                            "-fx-alignment: center;");
        tagContainer.setSpacing(5);
                            
        //close button and delet from the drugSelection colelction
        closeButton.setOnMouseClicked(e -> {
            ((HBox) tagContainer.getParent()).getChildren().remove(tagContainer);
            drugSelection.remove(labelText.toLowerCase());
            updateSuggestions(textField.getText());
        });

        return tagContainer;
    }

    /**
     * Moves the selection to the next item in the suggestions list
     *
     * @param suggestionListView the ListView containing suggestions
     */
    private void moveToNext(ListView<String> suggestionListView) {
        int currentIndex = suggestionListView.getSelectionModel().getSelectedIndex();
        if (currentIndex==(suggestionListView.getItems().size() - 1)) {
            suggestionListView.getSelectionModel().select(suggestionListView.getItems().size() - 1);
        }
        else {
            suggestionListView.getSelectionModel().select(currentIndex + 1);
        }
        if (currentIndex > 2) {
        suggestionListView.scrollTo(suggestionListView.getSelectionModel().getSelectedIndex()-3);
        }
    }

    /**
     * Moves the selection to the previous item in the suggestions list
     *
     * @param suggestionListView the ListView containing suggestions
     */
    private void moveToPrevious(ListView<String> suggestionListView) {
        
        int currentIndex = suggestionListView.getSelectionModel().getSelectedIndex();
        if (currentIndex==0 || currentIndex==-1) {
            suggestionListView.getSelectionModel().clearSelection();
        } else {
            suggestionListView.getSelectionModel().select(currentIndex - 1);
        }
        if (currentIndex > 2) {
            suggestionListView.scrollTo(suggestionListView.getSelectionModel().getSelectedIndex()-3);
            }
    }

    /**
     * Selects the currently highlighted suggestion and adds it to the selection
     *
     * @param textField the input field for drug names
     * @param suggestionListView the ListView containing suggestions
     */
    private void selectSuggestion(TextField textField, ListView<String> suggestionListView) {
        String selectedSuggestion = suggestionListView.getSelectionModel().getSelectedItem();
        if (selectedSuggestion != null) {
            if (drugSelection.contains(selectedSuggestion.toLowerCase())) {
                alertMessage("Duplicate Drug", "The drug '" + selectedSuggestion + "' has already been added");
            }
            else {
                HBox tag = createTag(capitalizeFirstLetter(selectedSuggestion));
                labelRow.getChildren().add(tag);
                drugSelection.add(selectedSuggestion.toLowerCase());
                textField.clear();
                suggestionListView.setVisible(false);
            }
        }
        else {
            checkManualInput();
        }
    }

    /**
     * Checks the input manually typed by the user and processes it
     */
    private void checkManualInput() {
        try {
        String in = textField.getText().trim();
        List<String> match = querySource.queryDrug(in, false);
        if (!match.isEmpty()) {
            if (drugSelection.contains(in.toLowerCase())) {
                alertMessage("Duplicate Drug", "The drug '" + in + "' has already been added");
            }
            else {
                HBox tag = createTag(capitalizeEveryFirstLetter(in));
                labelRow.getChildren().add(tag);
                drugSelection.add(in.toLowerCase());
                textField.clear();
            }
        } else {
            alertMessage("Drug Not Found", "The drug '" + in + "' was not found in the database");
        }
    } catch (Exception e) {
        logger.severe("An error occurred while trying to add the drug: " + e.getMessage());
        alertMessage("Error", "An error occurred while trying to add the drug");
    }
    }

    /**
     * Displays an alert message in a pop-up window
     *
     * @param title the title of the alert
     * @param message the message content
     */
    private void alertMessage(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Creates the button section with the "Check Interactions" button
     *
     * @return an HBox containing the button
     */
    private HBox createButtonSection() {
        Button checkButton = new Button("Check Interactions");
        Button testButton = new Button("Test Interactions");
        String DesignStr =  "-fx-text-fill: white; " +         
                            "-fx-font-size: 22px; " +            
                            "-fx-background-radius: 20; " +         
                            "-fx-padding: 8 16;"+
                            "-fx-min-width: 300px; ";
        checkButton.setStyle(DesignStr + "-fx-background-color:rgb(199, 21, 95); ");
        testButton.setStyle(DesignStr + "-fx-background-color:rgb(134, 22, 138);");
        checkButton.setOnMouseEntered(event->checkButton.setStyle(DesignStr + "-fx-background-color:rgb(172, 27, 87); "));
        checkButton.setOnMouseExited(event->checkButton.setStyle(DesignStr + "-fx-background-color:rgb(199, 21, 95); "));
        testButton.setOnMouseEntered(event->testButton.setStyle(DesignStr + "-fx-background-color:rgb(106, 18, 109); "));
        testButton.setOnMouseExited(event->testButton.setStyle(DesignStr + "-fx-background-color:rgb(134, 22, 138); "));

        //create new window with drug interactions
        checkButton.setOnAction(e -> {
            try {
                if (drugSelection.size() < 2) {
                    alertMessage("Insufficient Drugs", "Please select at least two drugs to check for interactions");
                } else {
                    List<String> drugList = new ArrayList<>(drugSelection); //Convert Set to List
                    TwosidesCol twosidesCol = new TwosidesCol();
                    for (int i = 0; i < drugList.size(); i++) {
                        for (int j = i + 1; j < drugList.size(); j++) {
                            TwosidesCol tempTwosidesCol = querySource.queryTwosides(drugList.get(i), drugList.get(j), true);  
                            twosidesCol.addTwosides(tempTwosidesCol.getCol().keySet().iterator().next(), tempTwosidesCol.getCol().values().iterator().next());
                        }
                    }
                    showInteractionWindow(twosidesCol);
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "An error occurred while trying to show the interaction window: {0}", ex.getMessage());
                alertMessage("Error", "An error occurred while trying to show the interaction window");
            }
        });

        testButton.setOnAction(e -> {showInteractionWindow(mockDb.getDb());});

        //button row
        HBox buttonRow = new HBox(10, checkButton, testButton);
        //button row styling
        buttonRow.setStyle("-fx-padding: 20;" +
                        "-fx-alignment: center;");
        return buttonRow;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Displays a new window to show drug interaction details
     *
     * @param twosidesCol the collection of drug interactions to display
     */
    private void showInteractionWindow(TwosidesCol twosidescol) {
        Stage interactionStage = new Stage();
        interactionStage.setTitle("Drug Interactions"); 
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(10));
        mainLayout.setStyle("-fx-background-color: #f9f9f9;");
        Label interactionTitle = new Label("Drug Interactions");
        interactionTitle.setStyle("-fx-font-weight: bold;" +
                                "-fx-font-size: 24px;");
        interactionTitle.setId("DI");
        VBox summarySection = createSummarySection(twosidescol, scrollPane);
        VBox interactionDetails = createInteractionDetails(twosidescol, scrollPane);
        mainLayout.getChildren().addAll(interactionTitle, summarySection, interactionDetails);
        scrollPane.setContent(mainLayout);
        Scene scene = new Scene(scrollPane, 1000, 1200);
        interactionStage.setScene(scene);
        interactionStage.show();
    }

    /**
     * Creates the summary section listing drug interactions
     *
     * @param twosidesCol the collection of drug interactions
     * @param scrollPane the scrollable pane for interaction content
     * @return a VBox containing the summary section
     */
    private VBox createSummarySection(TwosidesCol twosidescol, ScrollPane scrollPane) {
        //local vars
        Set<VBox> safeDrugs = new HashSet<>();

        //summary section
        VBox summarySection = new VBox(10);
        for (Map.Entry<String, Map<String, Twosides>> entry : twosidescol.getCol().entrySet()) {
            if (entry.getValue().isEmpty()) {safeDrugs.add(createInteractionBox(entry, summarySection, true, scrollPane));}
            else {createInteractionBox(entry, summarySection, false, scrollPane);}
        }
        
        //safe drugs section
        if (!safeDrugs.isEmpty()) {
            safeDrugsHeaderSection(summarySection, safeDrugs, twosidescol);
        }
        
        //styling
        summarySection.setStyle("-fx-background-color: #ffffff;" +
                                "-fx-border-color: #cccccc;" +
                                "-fx-border-radius: 5;" + 
                                "-fx-padding: 10;");

        return summarySection;
    }

    /**
     * Generates a header for the "safe drugs" section and allows toggling its visibility
     *
     * @param summarySection the parent VBox where the header will be added
     * @param safeDrugsBoxes the set of safe drug interaction boxes
     * @param twosidesCol the collection of all drug interactions
     * @return the updated summary section with the safe drugs header
     */
    private VBox safeDrugsHeaderSection(VBox summarySection, Set<VBox> safeDrugsBoxes, TwosidesCol twosidescol) {
        //header to add seperator; title for toggle action
        HBox header = new HBox(5);
        Label title = new Label(
            safeDrugsBoxes.size()==1
                ? "No interactions were found for 1 drug pair. Expand this section to view details"
                : "No interactions were found for " + safeDrugsBoxes.size() + " drug pairs. Expand this section to view details."
        );

        //icon creation and styling
        String path = "icons/step-forward.png";
        Image image = new Image(getClass().getResource(path).toExternalForm());
        ImageView arrow = new ImageView(image);
        arrow.setFitWidth(24);
        arrow.setFitHeight(24);
        
        //header styling; title styling
        header.setStyle("-fx-padding: 5;" + 
                        "-fx-border-color: black;" + 
                        "-fx-border-width: 4 0 0 0;" +
                        "-fx-cursor: hand;");
        title.setStyle("-fx-font-size: 16px;");

        //content section
        VBox content = new VBox();
        content.setVisible(false);
        content.setManaged(false);
        
        //Toggle action
        header.setOnMouseClicked(event -> {
            boolean isCollapsed = content.isVisible();
            content.setVisible(!isCollapsed);
            content.setManaged(!isCollapsed);
            content.getChildren().setAll(safeDrugsBoxes);
            
            if (isCollapsed) {
                arrow.setRotate(0);
            } else {
                arrow.setRotate(90);
            }
        });

        //add to summary section
        header.getChildren().addAll(arrow, title);
        summarySection.getChildren().addAll(header, content);

        return summarySection;
    }

    /**
     * Creates a header showing the interaction between two drugs
     *
     * @param drug1 the name of the first drug
     * @param drug2 the name of the second drug
     * @param frameHeader true if the header is styled as a frame, false otherwise
     * @param scrollPane the ScrollPane to enable navigation
     * @return an HBox representing the interaction header
     */
    private HBox createInteractionContent(String drug1, String drug2, boolean frameHeader, ScrollPane scrollPane) {
        //label content creation
        HBox header = new HBox(5);
        Label drug1Label = new Label(drug1);
        String path = "icons/repeat.png";
        Image image = new Image(getClass().getResource(path).toExternalForm());        
        ImageView arrowIcon = new ImageView(image);        arrowIcon.setFitWidth(20);
        arrowIcon.setFitHeight(20);
        Label drug2Label = new Label(drug2);
        header.getChildren().addAll(drug1Label, arrowIcon, drug2Label);
        
        //header styling twopath
        String headerCommonStyle =  "-fx-padding: 5 20;" +
                                    "-fx-font-weight: bold;";
        header.setAlignment(Pos.CENTER);
        if (frameHeader) {
            String frameHederStdStyle = "-fx-background-radius: 20;" + 
                                        "-fx-font-size: 20px;";
            header.setStyle(headerCommonStyle + frameHederStdStyle+"-fx-background-color: rgba(27, 113, 243, 0.61);");
            header.setOnMouseEntered(event->header.setStyle(headerCommonStyle+frameHederStdStyle+"-fx-background-color: rgba(7, 74, 175, 0.64);"));
            header.setOnMouseExited(event->header.setStyle(headerCommonStyle+frameHederStdStyle+"-fx-background-color: rgba(27, 113, 243, 0.61);"));
            // header.setOnMouseClicked(event->scrollToSection("sfi", scrollPane));
            header.setOnMouseClicked(event->scrollToSection("DI", scrollPane));
        }
        else {
            header.setStyle(headerCommonStyle + "-fx-background-color: rgba(228, 236, 243, 0.51);"+
                                                "-fx-background-radius: 12;" + 
                                                "-fx-font-size: 17px;" +
                                                "-fx-max-width: 450px;" +
                                                "-fx-min-width: 450px;");
        }
        header.setId("sfi");
        return header;
    }

    /**
     * Creates a counter label for an interaction entry
     *
     * @param value the count value to display
     * @return a styled Label showing the count
     */
    private Label createCounterLabel(int value) {
        //label content creation
        Label countLabel = new Label(String.valueOf(value));

        //label stylign
        countLabel.setStyle("-fx-background-color: #e57373; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-font-size: 15px; " +
                            "-fx-padding: 5 15; " +
                            "-fx-background-radius: 20; " +
                            "-fx-alignment: center;" +
                            "-fx-min-width: 60px; " +
                            "-fx-max-width: 60px; ");
        countLabel.setAlignment(Pos.CENTER);

        return countLabel;
    }
 
    /**
     * Creates the header for an interaction entry with a count of interactions
     *
     * @param twoSideInteraction the drugs involved in the interaction
     * @param value the interaction details
     * @param key the identifier for the interaction
     * @param safeDrug whether the interaction is marked as safe
     * @param scrollPane the ScrollPane to enable scrolling to the section
     * @return an HBox containing the interaction header
     */
    private HBox createInteractionHeader(List<String> twoSideInteraction, Map<String, Twosides> value, String key, boolean safeDrug, ScrollPane scrollPane) {
        //interactionHeader content creation
        HBox interactionHeader = new HBox(10);
        HBox headerContent = createInteractionContent(twoSideInteraction.get(0), twoSideInteraction.get(1), false, scrollPane);
        Label countLabel = createCounterLabel(value.size());
        Region rowSpacer = new Region();
        interactionHeader.getChildren().addAll(headerContent, rowSpacer, countLabel);

        //interactionHeader styling
        String stdStyle="-fx-padding: 10 50;" +
                        "-fx-border-color: #dddddd;" +
                        "-fx-border-radius: 5;";
        interactionHeader.setStyle(stdStyle + "-fx-background-color: #f9f9f9;");
        interactionHeader.setAlignment(Pos.CENTER);
        HBox.setHgrow(rowSpacer, Priority.ALWAYS);
        rowSpacer.setMaxWidth(200);

        //interactionHeader actions
        if (!safeDrug) {
            interactionHeader.setOnMouseClicked(event -> scrollToSection(key, scrollPane));
            interactionHeader.setOnMouseEntered(event -> interactionHeader.setStyle(stdStyle + "-fx-background-color:rgba(233, 230, 230, 0.57);")); //#e6f7ff;"
            interactionHeader.setOnMouseExited(event -> interactionHeader.setStyle(stdStyle + "-fx-background-color: #f9f9f9;"));
        }

        //interactionHeader tooltip
        String tooltipText;
        if (safeDrug) {
            tooltipText = "No interactions were found for " + twoSideInteraction.get(0) + " ↔ " + twoSideInteraction.get(1);
        } else {
            tooltipText = value.size() + " interactions were found. Click to view details for " + 
                        twoSideInteraction.get(0) + " ↔ " + twoSideInteraction.get(1);
        }
        Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setShowDelay(Duration.millis(200));
        tooltip.setHideDelay(Duration.millis(2));
        tooltip.setStyle("-fx-font-size: 11px; ");
        Tooltip.install(interactionHeader, tooltip);

        return interactionHeader;
    }

    /**
     * Creates an interaction box containing the header and optional details
     *
     * @param entry the interaction entry
     * @param summarySection the VBox to add the interaction box to
     * @param safeDrug whether the interaction is marked as safe
     * @param scrollPane the ScrollPane to enable scrolling
     * @return a VBox containing the interaction box
     */
    private VBox createInteractionBox(Map.Entry<String, Map<String, Twosides>> entry, VBox summarySection, boolean safeDrug, ScrollPane scrollPane) {
        //local vars
        String key = entry.getKey();
        Map<String, Twosides> value = entry.getValue();
        List<String> twoSideInteraction = TwosidesCol.getUnmapedStrId(key);
        for (int i = 0; i < twoSideInteraction.size(); i++) {
            twoSideInteraction.set(i, capitalizeEveryFirstLetter(twoSideInteraction.get(i)));
        }

        //interactionBox content creation
        HBox interactionHeader = createInteractionHeader(twoSideInteraction, value, key, safeDrug, scrollPane);
        VBox interactionBox = new VBox(5, interactionHeader);
        if (!safeDrug) {
            summarySection.getChildren().add(interactionBox);
        }

        //interactionBox styling
        interactionBox.setStyle("-fx-padding: 5;" +
                                "-fx-background-color: #ffffff;" +
                                "-fx-border-color: #dddddd;" + 
                                "-fx-border-radius: 5;");

        return interactionBox;
    }

    /**
     * Creates a detailed interaction section with information about symptoms and severity
     *
     * @param twosidesCol the collection of drug interactions
     * @param scrollPane the scrollable pane for interaction content
     * @return a VBox containing interaction details
     */
    private VBox createInteractionDetails(TwosidesCol twosidescol, ScrollPane scrollPane) {
        //interactionDetails content creation
        VBox detailsContainer = new VBox(20);
        detailsContainer.setStyle("-fx-background-color: #ffffff;" + 
                                "-fx-border-color: #cccccc;" + 
                                "-fx-border-radius: 5;" + 
                                "-fx-padding: 10;");

        for (Map.Entry<String, Map<String, Twosides>> twosidesInteraction : twosidescol.getCol().entrySet()) {
            //safe drug skip
            if (twosidesInteraction.getValue().isEmpty()) {
                continue;
            }
            //otehrwise create an interaction section
            GridPane table = new GridPane();
            VBox interactionSection = createInteractionSection(twosidesInteraction, table);
            List<String> twoSideInteraction = TwosidesCol.getUnmapedStrId(twosidesInteraction.getKey());
            for (int i=0; i<twoSideInteraction.size(); i++) {
                twoSideInteraction.set(i, capitalizeEveryFirstLetter(twoSideInteraction.get(i)));
            }            
            HBox header = createInteractionContent(twoSideInteraction.get(0), twoSideInteraction.get(1), true, scrollPane);
            table = initTable(table);
            table = createInteractionRows(twosidesInteraction, table);
            interactionSection.getChildren().addAll(header, table);
            detailsContainer.getChildren().add(interactionSection);
        }

        return detailsContainer;
    }

    /**
     * Creates a VBox to display an interaction section with a header and table of details
     *
     * @param twosidesInteraction the interaction data
     * @param table the GridPane table to populate with interaction rows
     * @return a VBox containing the interaction section
     */
    private VBox createInteractionSection(Map.Entry<String, Map<String, Twosides>> twosidesInteraction, GridPane table) {
        //interactionSection content craetion
        VBox interactionSection = new VBox(10);
        interactionSection.setId(twosidesInteraction.getKey());

        //interactionSection styling
        interactionSection.setStyle("-fx-border-color:rgb(228, 222, 222);" +
                                    "-fx-border-width: 1;" +
                                    "-fx-background-color: #f9f9f9;" + //#f9f9f9
                                    "-fx-padding: 10;" +
                                    "-fx-border-radius: 5;");

        return interactionSection;
    }

    /**
     * Initializes a table for displaying interaction details
     *
     * @param table the GridPane to initialize
     * @return the initialized GridPane
     */
    private GridPane initTable(GridPane table) {
        //table styling
        table.setHgap(20);
        table.setVgap(10);
        table.setStyle("-fx-border-color: #dddddd;" +
                    "-fx-background-color: #ffffff;" +
                    "-fx-padding: 10;");
        table.setAlignment(Pos.CENTER);
        table.setPrefWidth(1000);

        //table labels creation
        String stdFont="-fx-font-weight: bold; -fx-font-size: 16px;";
        Label symptomHeader = new Label(String.format("%-26s %s", " ", "Symptom"));
        Label severityHeader = new Label(String.format("%-5s %s", " ", "Severity"));
        Label descriptionHeader = new Label(String.format("%-29s %s", " ", "Description"));

        //table lables styling
        symptomHeader.setStyle(stdFont);
        severityHeader.setStyle(stdFont);
        descriptionHeader.setStyle(stdFont);

        //table labels add
        table.add(symptomHeader, 0, 0);
        table.add(severityHeader, 1, 0);
        table.add(descriptionHeader, 2, 0);

        return table;
    }

    /**
     * Creates rows in the interaction table with details about symptoms and severity
     *
     * @param twosidesInteraction the interaction data
     * @param table the table to populate
     * @return the populated GridPane
     */
    private GridPane createInteractionRows(Map.Entry<String, Map<String, Twosides>> twosidesInteraction, GridPane table) {
        int row = 1;
        for (Map.Entry<String, Twosides> twosides : twosidesInteraction.getValue().entrySet()) {
            //local vars
            Twosides value = twosides.getValue();
            Label symptomLabel = new Label(value.getConditionName());
            Label severityLabel = setSeverityStyle(value.getSeverityClass());
            TextFlow descriptionInfo = twosidesInteractionDescription(value);
            
            //symptom label styling
            String symptomLabelStyle="-fx-text-fill: black; " +
                                    "-fx-font-size: 15px; " +
                                    "-fx-font-weight: bold; " +
                                    "-fx-padding: 5 20; " +
                                    "-fx-background-radius: 15; " +
                                    "-fx-alignment: center; " +
                                    "-fx-min-width: 300px; " +
                                    "-fx-max-width: 300px; ";
            symptomLabel.setStyle(symptomLabelStyle+"-fx-background-color: rgba(228, 236, 243, 0.51);");
            
            //descirption label styling
            descriptionInfo.setMaxWidth(350);
            descriptionInfo.setStyle("-fx-padding: 5 10;" +
                                    "-fx-background-color:rgba(228, 236, 243, 0.51);" + 
                                    "-fx-background-radius: 15;");

            //symptom label tooltip
            symptomLabel = symptomInfoTooltip(symptomLabel, value, symptomLabelStyle);

            //add ot table
            table.add(symptomLabel, 0, row);
            table.add(severityLabel, 1, row);
            table.add(descriptionInfo, 2, row);

            //seperators
            row++;
            if (row <= 2*twosidesInteraction.getValue().size()-1) {
                Separator separator = new Separator();
                table.add(separator, 0, row, 3, 1);
                row++;
            }
        }

        return table;
    }

    /**
     * Adds a tooltip to a symptom label with delayed content loading
     *
     * @param symptomLabel the Label for the symptom
     * @param value the Twosides object containing condition details
     * @param symptomLabelStyle the base style for the label
     * @return the updated Label with tooltip functionality
     */
    private Label symptomInfoTooltip(Label symptomLabel, Twosides value, String symptomLabelStyle) {
        //tooltip creation
        String loadStr = "Loading condition description...";
        Tooltip tooltip = new Tooltip(loadStr);

        //tooltrip props
        tooltip.setShowDelay(Duration.millis(200));
        tooltip.setHideDelay(Duration.INDEFINITE);
        tooltip.setMaxWidth(400);
        tooltip.wrapTextProperty().setValue(true);
        tooltip.setStyle("-fx-font-size: 11px;");
        tooltip.setTextAlignment(TextAlignment.JUSTIFY);
        PauseTransition delay = new PauseTransition(Duration.seconds(0.2));

        //prompts when mouse enters the label
        symptomLabel.setOnMouseEntered(event -> {
            symptomLabel.setStyle(symptomLabelStyle+"-fx-background-color: rgba(223, 221, 221, 0.66);");
            tooltip.setText(loadStr);
            delay.setOnFinished(e -> {
                if (tooltip.getText().equals(loadStr)) {
                    new Thread(() -> {
                        String wikiInfo = fetchWikiInfo(value.getConditionName());
                        Platform.runLater(() -> tooltip.setText(wikiInfo));
                        }).start();
                        tooltip.show(symptomLabel, 
                        event.getScreenX() + 10,
                        event.getScreenY() + 10);
                }
            });
            delay.playFromStart();
        });
        symptomLabel.setOnMouseExited(event -> {
            symptomLabel.setStyle(symptomLabelStyle+"-fx-background-color: rgba(228, 236, 243, 0.51);");
            delay.stop();
            tooltip.hide();
        });

        return symptomLabel;
    }

    /**
     * Sets the styling for a severity label based on its value
     *
     * @param severityStr the severity string
     * @return a styled Label representing the severity
     */
    private Label setSeverityStyle(String severityStr) {
        Label severityLabel = new Label(severityStr);
        String baseStyle = "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 17px; " +
                        "-fx-padding: 5 15; " +
                        "-fx-background-radius: 14; " +
                        "-fx-alignment: center; " +
                        "-fx-min-width: 110px; " + 
                        "-fx-max-width: 110px; ";
        String backgroundColor = switch (severityStr) {
            case "Critical" -> "rgb(119, 32, 32)";
            case "Severe" -> "rgb(199, 21, 95)";
            case "Medium" -> "rgb(255, 152, 0)";
            case "Minor" -> "rgb(0, 150, 136)";
            case "Negligible" -> "rgb(76, 175, 80)";
            case "Unknown" -> "rgb(245, 243, 227)";
            default -> "rgb(200, 200, 200)";
        };
        severityLabel.setStyle(baseStyle + "-fx-background-color: " + backgroundColor + ";");

        return severityLabel;
    }

    /**
     * Creates a description for a Twosides interaction
     *
     * @param twosides the Twosides object containing interaction details
     * @return a TextFlow containing the interaction description
     */
    private TextFlow twosidesInteractionDescription(Twosides twosides) {
        //declare local vars and text alignment
        TextFlow descriptionTextFlow = new TextFlow();
        descriptionTextFlow.setTextAlignment(javafx.scene.text.TextAlignment.JUSTIFY);
        String stdFont="-fx-font-size: 14px;";
        String boldFont="-fx-font-weight: bold; -fx-font-size: 14px;";

        //create text parts
        Text part1 = new Text("Based on the sample of ");
        part1.setStyle(stdFont);
        Text part2 = new Text(String.valueOf(twosides.getA() + twosides.getB()));
        part2.setStyle(boldFont);
        Text part3 = new Text(" reports for the pair of drugs, the mean reporting frequency of " + twosides.getConditionName() + " is ");
        part3.setStyle(stdFont);
        Text part4 = new Text(String.format("%.2f%%", twosides.getMeanReportingFrequency() * 100));
        part4.setStyle(boldFont);
        Text part5 = new Text(". " + "The Proportional Reporting Ratio (PRR)" + " is ");
        part5.setStyle(stdFont);
        Text part6 = new Text(String.format("%.2f", twosides.getPrr()));
        part6.setStyle(boldFont);
        Text part7 = new Text(" with an error estimate of ");
        part7.setStyle(stdFont);
        Text part8 = new Text(String.format("%.2f", twosides.getPrrError()));
        part8.setStyle(boldFont);

        descriptionTextFlow.getChildren().addAll(part1, part2, part3, part4, part5, part6, part7, part8);
        return descriptionTextFlow;
    }

    /**
     * Scrolls the view to the specified section within the ScrollPane
     *
     * @param sectionId the ID of the target section
     * @param scrollPane the ScrollPane containing the content
     */
    private void scrollToSection(String sectionId, ScrollPane scrollPane) {
        Platform.runLater(() -> {
            Node targetNode = scrollPane.getContent().lookup("#" + sectionId);

            if (targetNode != null) {
                Bounds contentBounds = scrollPane.getContent().localToScene(scrollPane.getContent().getBoundsInLocal());
                Bounds nodeBounds = targetNode.localToScene(targetNode.getBoundsInLocal());
                double nodePosition = nodeBounds.getMinY() - contentBounds.getMinY();
                double viewportHeight = scrollPane.getViewportBounds().getHeight();
                double contentHeight = scrollPane.getContent().getLayoutBounds().getHeight();
                double scrollValue = nodePosition / (contentHeight - viewportHeight);
                scrollValue = Math.max(0, Math.min(scrollValue, 1));
                scrollPane.setVvalue(scrollValue);
            } else {
                logger.info("Failed to find the target node");
            }
        });
    }

    /**
     * Fetches information about a condition from Wikipedia
     *
     * @param conditionName the name of the condition
     * @return a string containing the fetched information or "No data found" if unavailable
     */
    private String fetchWikiInfo(String info) {
        if (wikiQueries.containsKey(info)) {
            return wikiQueries.get(info);
        }
        String wikiInfo = WikiAPI.queryWiki(info);
        wikiQueries.put(info, wikiInfo);
        if (wikiInfo == null) {
            return "No data found";
        }
        return wikiInfo;
    }

    public static void main(String[] args) {
        launch(args);
    }

}

interface Queryable {
    TwosidesCol queryTwosides(String drugA, String drugB, boolean someFlag);
    List<String> queryDrug(String drugName, boolean like);
}