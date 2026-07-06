package com.moodflix.admin;

import com.moodflix.model.Content;
import com.moodflix.service.PostgreSQLContentService;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AdminUploadPage {
    public void show() {
        Stage stage = new Stage();
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        TextField titleField = new TextField();
        titleField.setPromptText("Enter Title");

        ComboBox<String> moodBox = new ComboBox<>();
        moodBox.getItems().addAll("Happy", "Sad", "Excited", "Relaxed");

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Movie", "Series", "Song", "Short");

        TextField linkField = new TextField();
        linkField.setPromptText("Enter YouTube/Stream Link");

        Button uploadBtn = new Button("Upload Content");
        Label statusLabel = new Label();

        uploadBtn.setOnAction(e -> {
            String title = titleField.getText();
            String mood = moodBox.getValue();
            String type = typeBox.getValue();
            String link = linkField.getText();

            if (title.isEmpty() || mood == null || type == null || link.isEmpty()) {
                statusLabel.setText("Please fill all fields!");
                return;
            }

            Content content = new Content(title, mood, type, link, "", "");
            PostgreSQLContentService service = new PostgreSQLContentService();
            try {
                service.uploadContent(content);
                statusLabel.setText("Upload Successful ");
            } catch (Exception ex) {
                statusLabel.setText("Upload Failed ");
                ex.printStackTrace();
            }
        });

        root.getChildren().addAll(
                new Label("Title:"), titleField,
                new Label("Mood:"), moodBox,
                new Label("Type:"), typeBox,
                new Label("Link:"), linkField,
                uploadBtn, statusLabel
        );

        stage.setScene(new Scene(root, 400, 400));
        stage.setTitle("Admin: Upload Content");
        stage.show();
    }
}
