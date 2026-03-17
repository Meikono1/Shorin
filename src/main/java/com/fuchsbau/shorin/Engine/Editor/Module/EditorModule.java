package com.fuchsbau.shorin.Engine.Editor.Module;

import javafx.scene.Node;
import javafx.scene.control.Menu;

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
}