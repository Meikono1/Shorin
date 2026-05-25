package com.fuchsbau.shorin.Engine.Paperdoll;

import com.fuchsbau.shorin.Engine.Images.ImagePaths;
import com.fuchsbau.shorin.Engine.Images.ImagePreLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public final class DetailWindow {
    private final Stage stage = new Stage();
    private final Label title = new Label();
    private final TextArea text = new TextArea();

    public DetailWindow(Window owner) {
        stage.initOwner(owner);
        stage.initModality(Modality.NONE);

        text.setEditable(false);
        text.setWrapText(true);

        VBox root = new VBox(10, title, text);
        root.setPadding(new Insets(10));

        stage.setScene(new Scene(root, 520, 420));
        stage.setTitle("Details");
        stage.getIcons().add(ImagePreLoader.getCached(ImagePaths.SHORIN_LOGO));
    }
}