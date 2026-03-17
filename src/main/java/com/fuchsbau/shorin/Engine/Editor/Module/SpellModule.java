package com.fuchsbau.shorin.Engine.Editor.Module;

import javafx.scene.Node;
import javafx.scene.control.Menu;

import java.util.List;

public class SpellModule implements EditorModule {
    @Override
    public String getTitle() {
        return "Spells";
    }

    @Override
    public Node buildContent() {
        return null;
    }

    @Override
    public Node buildToolbar() {
        return null;
    }

    @Override
    public Node buildSidePanel() {
        return null;
    }

    @Override
    public List<Menu> getMenus() {
        return List.of();
    }
}
