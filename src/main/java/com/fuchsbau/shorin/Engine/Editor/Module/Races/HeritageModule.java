package com.fuchsbau.shorin.Engine.Editor.Module.Races;

import com.fuchsbau.shorin.Engine.Editor.Module.EditorModule;
import javafx.scene.Node;
import javafx.scene.control.Menu;

import java.util.List;

public class HeritageModule implements EditorModule {
    @Override
    public String getTitle() {
        return "Heritages";
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
