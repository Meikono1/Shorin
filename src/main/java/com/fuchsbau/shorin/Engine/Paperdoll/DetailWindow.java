package com.fuchsbau.shorin.Engine.Paperdoll;

import com.fuchsbau.shorin.Engine.Images.ImagePaths;
import com.fuchsbau.shorin.Engine.Images.ImagePreLoader;
import com.fuchsbau.shorin.Races.Base.Race;
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

    public void showRace(Race race) {
        title.setText(race.displayName());
        text.setText(buildRaceText(race));
        if (!stage.isShowing()) stage.show();
        stage.toFront();
    }

    public String buildRaceText(Race r) {
        var ls = r.getLifeStage();
        return "ID: " + r.getRaceName() + "\n"
                + "Size: " + r.getSize() + "\n"
                + "Speed: " + r.getSpeed() + "ft\n"
                + "Health: " + r.getMaxHealth() + "\n\n"
                + "AdultAge: " + ls.ageAdult() + "\n"
                + "OldAge: " + ls.ageOld() + "\n"
                + "MaxAge: " + ls.ageMax() + "\n";
    }
}