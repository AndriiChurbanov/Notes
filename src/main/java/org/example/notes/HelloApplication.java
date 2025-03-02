package org.example.notes;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class HelloApplication extends Application {
    private static final String DIRECTORY_NAME = "notes";
    private static final String VERSION = " Версія 1.0.5";
    private ListView<File> noteListView;
    private TextField searchField;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        File directory = new File(DIRECTORY_NAME);
        if (!directory.exists()) {
            directory.mkdir();
        }

        noteListView = new ListView<>();
        searchField = new TextField();
        searchField.setPromptText("Пошук нотатки...");
        Button searchButton = new Button("🔍");

        updateNoteList("");

        Button addButton = new Button("Додати нотатку");
        Button deleteButton = new Button("Видалити нотатку");

        addButton.setOnAction(e -> addNote());
        deleteButton.setOnAction(e -> deleteNote());

        searchField.textProperty().addListener((observable, oldValue, newValue) -> updateNoteList(newValue));

        noteListView.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                File selectedNote = noteListView.getSelectionModel().getSelectedItem();
                if (selectedNote != null) {
                    openNote(selectedNote);
                }
            }
        });

        noteListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(File file, boolean empty) {
                super.updateItem(file, empty);
                if (empty || file == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label nameLabel = new Label(file.getName());
                    Label dateLabel = new Label(getFormattedDate(file));

                    nameLabel.setMaxWidth(Double.MAX_VALUE);
                    HBox.setHgrow(nameLabel, Priority.ALWAYS);
                    dateLabel.setStyle("-fx-text-fill: gray;");
                    setStyle("-fx-font-size: 14px;");

                    HBox hBox = new HBox(10, nameLabel, dateLabel);
                    hBox.setAlignment(Pos.CENTER_LEFT);

                    setGraphic(hBox);
                }
            }
        });

        Label versionLabel = new Label(VERSION);
        versionLabel.setStyle("-fx-text-fill: gray; -fx-font-size: 12px;");

        HBox searchBox = new HBox(5, addButton, deleteButton, searchField, searchButton);
        VBox root = new VBox(5, searchBox, noteListView, versionLabel);
        VBox.setVgrow(noteListView, Priority.ALWAYS);

        Scene scene = new Scene(root, 410, 500);
        primaryStage.getIcons().add(new Image("file:icon.png"));
        primaryStage.setTitle("Менеджер нотаток");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void updateNoteList(String filter) {
        File directory = new File(DIRECTORY_NAME);
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".txt"));

        if (files == null) return;

        List<File> filteredFiles = Arrays.stream(files)
                .filter(file -> file.getName().toLowerCase().contains(filter.toLowerCase()))
                .sorted(Comparator.comparingLong(File::lastModified).reversed())
                .collect(Collectors.toList());

        noteListView.getItems().setAll(filteredFiles);

        if (filteredFiles.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Нотатка не знайдена", ButtonType.OK);
            alert.showAndWait();
        }
    }

    private void addNote() {
        TextInputDialog nameDialog = new TextInputDialog();
        nameDialog.setTitle("Нова нотатка");
        nameDialog.setHeaderText("Введіть назву нотатки:");
        nameDialog.setContentText("Назва:");

        nameDialog.showAndWait().ifPresent(name -> {
            File file = new File(DIRECTORY_NAME + "/" + name + ".txt");
            if (file.exists()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Нотатка з вказаним іменем вже існує", ButtonType.OK);
                alert.showAndWait();
                return;
            }
            try {
                if (file.createNewFile()) {
                    TextInputDialog contentDialog = new TextInputDialog();
                    contentDialog.setTitle("Зміст нотатки");
                    contentDialog.setHeaderText("Введіть зміст нотатки:");
                    contentDialog.setContentText("Текст:");

                    contentDialog.showAndWait().ifPresent(content -> {
                        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                            writer.write(content);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

                    updateNoteList(searchField.getText());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void openNote(File file) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteNote() {
        File selectedNote = noteListView.getSelectionModel().getSelectedItem();
        if (selectedNote == null) return;

        if (selectedNote.delete()) {
            updateNoteList(searchField.getText());
        }
    }

    private String getFormattedDate(File file) {
        SimpleDateFormat sdf = new SimpleDateFormat("Створено: dd.MM.yyyy HH:mm:ss");
        try {
            BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            return sdf.format(new Date(attrs.creationTime().toMillis()));
        } catch (IOException e) {
            e.printStackTrace();
            return "Н/Д";
        }
    }
}