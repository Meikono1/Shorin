package com.fuchsbau.shorin.Engine.Editor.Module;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;

import java.util.List;

public interface EditorModule {

    String getTitle();

    Node buildContent();

    Node buildToolbar();

    Node buildSidePanel();

    List<Menu> getMenus();


    default void onActivate() {
    }

    default void onDeactivate() {
    }

    default VBox buildSection(String title, Node... content) {
        Label header = new Label(title);
        VBox section = new VBox(6);
        section.getChildren().add(header);
        section.getChildren().add(new Separator());
        section.getChildren().addAll(content);
        section.setMaxWidth(Double.MAX_VALUE);
        section.setPadding(new Insets(8));
        section.setStyle("-fx-border-color: #333350; -fx-border-width: 1; -fx-border-radius: 4;");
        return section;
    }
}