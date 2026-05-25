package com.fuchsbau.shorin.Engine.RPG.ViewModules;

import com.fuchsbau.shorin.Engine.Options.GameOptions;
import com.fuchsbau.shorin.Engine.RPG.ViewModules.Interfaces.Hideable;
import com.fuchsbau.shorin.Engine.RPG.ViewModules.Interfaces.Renderable;
import com.fuchsbau.shorin.Engine.SceneBuilder;
import com.fuchsbau.shorin.Engine.System.PlayerCharacter;
import com.fuchsbau.shorin.Logger.FileLogger;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Linkes Panel — 1:1 aus PlayerScreen.buildLeft() extrahiert.
 * Aussehen bleibt identisch zum Original.
 * <p>
 * Party-Stellen sind mit @TODO markiert — werden ersetzt sobald Party existiert.
 */
public class LeftPanelView implements Renderable, Hideable {

    private static final double WIDTH = 350;

    private final SceneBuilder sb = SceneBuilder.getSceneBuilder();
    private final Logger logger = FileLogger.getLogger();

    // @TODO durch Party ersetzen
    private List<PlayerCharacter> members = new ArrayList<>();
    private PlayerCharacter active = null;
    private Runnable onCharSelected;

    private VBox root;

    // Vom Controller gesetzt wenn Party geladen wird
    // @TODO Party-Klasse → setParty(Party party)
    public void setMembers(List<PlayerCharacter> members, PlayerCharacter active) {
        this.members = members;
        this.active = active;
        if (root != null) refresh();
        logger.fine("Party gesetzt | " + members.size() + " Mitglieder | aktiv: "
                + (active != null ? active.name : "—"));
    }

    public void setOnCharSelected(Runnable callback) {
        this.onCharSelected = callback;
    }

    @Override
    public Node build() {
        root = new VBox(6);
        root.setPrefWidth(WIDTH);
        root.setMaxWidth(WIDTH);
        root.setPadding(new Insets(8));
        root.setBackground(GameOptions.rowHintergrundTrans40);

        root.getChildren().addAll(
                buildCharSwitcher(),
                buildQuickStats(),
                buildCross(),
                buildPaperdoll()
        );

        VBox.setVgrow(root.getChildren().get(1), Priority.ALWAYS); // quickStats wächst

        logger.fine("LeftPanelView gebaut");
        return root;
    }

    @Override
    public void refresh() {
        root.getChildren().clear();
        root.getChildren().addAll(
                buildCharSwitcher(),
                buildQuickStats(),
                buildCross(),
                buildPaperdoll()
        );
        VBox.setVgrow(root.getChildren().get(1), Priority.ALWAYS);
        logger.fine("LeftPanelView refresh | aktiv: " + (active != null ? active.name : "—"));
    }

    // CHAR SWITCHER — ◀ Name ▶
    private Node buildCharSwitcher() {
        HBox charSwitcher = new HBox(8);
        charSwitcher.setAlignment(Pos.CENTER);
        charSwitcher.setPadding(new Insets(4));
        charSwitcher.setBackground(new Background(new BackgroundFill(
                Color.rgb(40, 40, 70), new CornerRadii(4), Insets.EMPTY)));

        Button prevChar = new Button("◀");
        prevChar.getStyleClass().add("menu-button");
        prevChar.setOnAction(e -> selectPrev());

        // @TODO active.name aus Party
        String displayName = active != null ? active.name : "— keine Party —";
        Label charName = sb.makeWhiteLabel(displayName);
        charName.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        charName.setMaxWidth(Double.MAX_VALUE);
        charName.setAlignment(Pos.CENTER);
        HBox.setHgrow(charName, Priority.ALWAYS);

        Button nextChar = new Button("▶");
        nextChar.getStyleClass().add("menu-button");
        nextChar.setOnAction(e -> selectNext());

        charSwitcher.getChildren().addAll(prevChar, charName, nextChar);
        return charSwitcher;
    }

    // QUICK STATS — HP / AC / Shield / Gold / Hero
    private Node buildQuickStats() {
        VBox quickStats = new VBox(4);
        quickStats.setPadding(new Insets(6));
        quickStats.setBackground(new Background(new BackgroundFill(
                Color.rgb(30, 50, 40), new CornerRadii(4), Insets.EMPTY)));

        Label statsHeader = sb.makeWhiteLabel("── Status ──");
        statsHeader.setMaxWidth(Double.MAX_VALUE);
        statsHeader.setAlignment(Pos.CENTER);

        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(8);
        statsGrid.setVgap(3);

        // @TODO Werte aus Party.activeChar.currentHp etc.
        statsGrid.add(statLabel("HP"), 0, 0);
        statsGrid.add(statValue(active != null ? active.hp + " / " + active.hp : "__ / __"), 1, 0);
        statsGrid.add(statLabel("AC"), 0, 1);
        statsGrid.add(statValue(active != null ? String.valueOf(active.ac) : "__"), 1, 1);
        statsGrid.add(statLabel("Shield"), 0, 2);
        statsGrid.add(statValue("__"), 1, 2); // @TODO Shield aus Inventory
        statsGrid.add(statLabel("Gold"), 0, 3);
        statsGrid.add(statValue("__"), 1, 3); // @TODO Party.gold
        statsGrid.add(statLabel("Hero"), 0, 4);
        statsGrid.add(statValue("__ / 3"), 1, 4); // @TODO HeroPoints

        Label condHeader = sb.makeWhiteLabel("Conditions:");
        condHeader.setStyle("-fx-font-size: 11px;");
        Label condValue = new Label("keine");
        condValue.setTextFill(Color.GRAY);
        condValue.setStyle("-fx-font-size: 11px;");
        condValue.setWrapText(true);
        // @TODO Conditions aus aktiven Statuseffekten

        quickStats.getChildren().addAll(statsHeader, statsGrid, new Separator(), condHeader, condValue);
        return quickStats;
    }

    // 4-WEGE KREUZ — Char / Spells / Inv / Feats
    private Node buildCross() {
        GridPane cross = new GridPane();
        cross.setHgap(4);
        cross.setVgap(4);
        cross.setPadding(new Insets(4));
        cross.setAlignment(Pos.CENTER);

        Button invBtn = crossBtn("🎒 Inv", Color.rgb(60, 40, 20));
        Button charBtn = crossBtn("👤 Char", Color.rgb(20, 40, 60));
        Button spellBtn = crossBtn("✨ Spells", Color.rgb(40, 20, 60));
        Button featBtn = crossBtn("⭐ Feats", Color.rgb(20, 55, 35));

        // @TODO je einen Screen/View öffnen
        invBtn.setOnAction(e -> logger.fine("@TODO InventoryView"));
        charBtn.setOnAction(e -> logger.fine("@TODO CharSheetView"));
        spellBtn.setOnAction(e -> logger.fine("@TODO SpellView"));
        featBtn.setOnAction(e -> logger.fine("@TODO FeatView"));

        cross.add(charBtn, 0, 0);
        cross.add(spellBtn, 1, 0);
        cross.add(invBtn, 0, 1);
        cross.add(featBtn, 1, 1);

        for (var node : cross.getChildren()) {
            GridPane.setFillWidth(node, true);
            GridPane.setFillHeight(node, true);
        }

        return cross;
    }

    // PAPERDOLL-BUTTON
    private Node buildPaperdoll() {
        VBox paperdollArea = new VBox(4);
        paperdollArea.setPadding(new Insets(6));
        paperdollArea.setBackground(new Background(new BackgroundFill(
                Color.rgb(50, 30, 50), new CornerRadii(4), Insets.EMPTY)));

        Button paperdoll = sb.createMenuButton("⧉ Paperdoll");
        paperdoll.setMaxWidth(Double.MAX_VALUE);
        paperdoll.setOnAction(e -> {
            com.fuchsbau.shorin.Engine.Paperdoll.PaperDollModel.toggle();
            logger.fine("Paperdoll toggle | Char: " + (active != null ? active.name : "—"));
        });

        paperdollArea.getChildren().add(paperdoll);
        return paperdollArea;
    }

    // Party-Navigation
    private void selectNext() {
        if (members.isEmpty()) return;
        int idx = members.indexOf(active);
        active = members.get((idx + 1) % members.size());
        logger.info("Char → " + active.name);
        refresh();
        if (onCharSelected != null) onCharSelected.run();
    }

    private void selectPrev() {
        if (members.isEmpty()) return;
        int idx = members.indexOf(active);
        active = members.get((idx - 1 + members.size()) % members.size());
        logger.info("Char → " + active.name);
        refresh();
        if (onCharSelected != null) onCharSelected.run();
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

    private Label statLabel(String text) {
        Label l = new Label(text);
        l.setTextFill(Color.LIGHTGRAY);
        l.setStyle("-fx-font-size: 11px;");
        return l;
    }

    private Label statValue(String text) {
        Label l = new Label(text);
        l.setTextFill(Color.WHITE);
        l.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
        return l;
    }

    private Button crossBtn(String label, Color bg) {
        Button btn = new Button(label);
        btn.setPrefSize((WIDTH - 28) / 2, 60);
        btn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        btn.setBackground(new Background(new BackgroundFill(bg, new CornerRadii(4), Insets.EMPTY)));
        btn.setTextFill(Color.WHITE);
        btn.setStyle("-fx-font-size: 12px;");
        return btn;
    }
}