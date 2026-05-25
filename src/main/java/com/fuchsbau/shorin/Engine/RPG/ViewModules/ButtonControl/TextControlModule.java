package com.fuchsbau.shorin.Engine.RPG.ViewModules.ButtonControl;

import com.fuchsbau.shorin.Engine.RPG.UI.Actionable;
import com.fuchsbau.shorin.Engine.RPG.ViewModules.Interfaces.Hideable;
import com.fuchsbau.shorin.Engine.RPG.ViewModules.Interfaces.Renderable;
import com.fuchsbau.shorin.Logger.FileLogger;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Button-Leiste für das TextAdventure.
 * Bekommt List<Actionable> vom Controller — weiß nicht was die Aktionen bedeuten.
 * Baut die Leiste neu wenn setActions() aufgerufen wird.
 */
public class TextControlModule implements Renderable, Hideable {

    private final Logger logger = FileLogger.getLogger();

    private VBox      root;
    private FlowPane  buttonRow;
    private Text      hintText;

    private List<Actionable> actions = new ArrayList<>();

    // Controller setzt neue Aktionen — Leiste wird sofort neu gebaut
    public void setActions(List<Actionable> actions) {
        this.actions = actions;
        if (buttonRow != null) rebuildButtons();
        logger.fine("Aktionen gesetzt | " + actions.size() + " Buttons");
    }

    // Hint-Text über den Buttons — z.B. "Was tust du?"
    public void setHint(String hint) {
        if (hintText != null) hintText.setText(hint);
        logger.fine("Hint: " + hint);
    }

    public void clearHint() {
        if (hintText != null) hintText.setText("");
    }

    @Override
    public Node build() {
        hintText = new Text("");
        hintText.setFill(Color.LIGHTGRAY);
        hintText.setStyle("-fx-font-size: 12px;");

        buttonRow = new FlowPane();
        buttonRow.setHgap(8);
        buttonRow.setVgap(6);
        buttonRow.setAlignment(Pos.CENTER_LEFT);

        root = new VBox(4, hintText, buttonRow);
        root.setPadding(new Insets(8, 12, 8, 12));
        root.setStyle("-fx-background-color: rgba(12, 12, 20, 0.75);");

        rebuildButtons();

        logger.fine("TextControlModule gebaut");
        return root;
    }

    @Override
    public void refresh() {
        rebuildButtons();
    }

    private void rebuildButtons() {
        buttonRow.getChildren().clear();
        for (Actionable action : actions) {
            buttonRow.getChildren().add(action.buildButton());
        }
        logger.fine("Buttons neu gebaut | " + actions.size());
    }

    @Override
    public void show() {
        root.setVisible(true);
        root.setManaged(true);
    }

    @Override
    public void hide() {
        root.setVisible(false);
        root.setManaged(false);
    }

    @Override
    public boolean isVisible() {
        return root != null && root.isVisible();
    }
}