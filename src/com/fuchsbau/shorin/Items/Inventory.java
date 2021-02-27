package com.fuchsbau.shorin.Items;

import com.fuchsbau.shorin.Items.Gear.Arms.Freearms;
import com.fuchsbau.shorin.Items.Gear.Armor;
import com.fuchsbau.shorin.Items.Gear.Head.Clothhelm;
import com.fuchsbau.shorin.Items.Gear.UpperBody.Clothchest;
import com.fuchsbau.shorin.Items.Gear.Boots.Clothboots;
import com.fuchsbau.shorin.Items.Gear.LowerBody.Clothpants;
import com.fuchsbau.shorin.Items.Waffen.Faust;
import com.fuchsbau.shorin.Items.Waffen.Waffe;
import com.fuchsbau.shorin.Optionen.GameOptionen;
import com.fuchsbau.shorin.Spiel.Game;
import com.fuchsbau.shorin.Spiel.Main;
import com.fuchsbau.shorin.Spiel.SceneBuilder;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;

public class Inventory {

    private Scene scene;
    private List<Item> items;

    private Armor head;
    private Armor chest;
    private Waffe weapon;
    private Armor boots;
    private Armor arms;
    private Armor pants;


    public Inventory() {
        items = new ArrayList<>();

        head = new Clothhelm();
        chest = new Clothchest();
        weapon = new Faust();
        boots = new Clothboots();
        arms = new Freearms();
        pants = new Clothpants();

        //TODO Mehr Items hinzufügen.
        //TODO Standart Melee weapon hinzufügen.
        //TODO ausziehen hinnzufügen

    }

    private void makeScene() {

        HBox erste = SceneBuilder.makeButtonrow();
        Button back = SceneBuilder.makeButton(erste);
        back.setText("Back");
        back.setOnMouseClicked(event -> {
            GameOptionen.delete = false;
            Main.getStage().setScene(Game.getInstance().spieler.getAktuell());
        });

        Button delete = SceneBuilder.makeButton(erste);
        delete.setText("Toggle delete");
        delete.setOnMouseClicked(event -> {
            GameOptionen.toggleDelete();
            Main.getStage().setScene(getScene());
        });

        //TODO Informationen button, Bücher, Rassen, etc

        erste.getChildren().addAll(back, delete);

        scene = new Scene(SceneBuilder.makePlayerInventory(erste, items, head, chest, arms, pants, boots, weapon));
    }

    public void addItem(Item item) {
        items.add(item);
    }

    public Scene getScene() {
        makeScene();
        return scene;
    }

    public void equip(Waffe waffe) {
        if (!weapon.toString().equals(new Faust().toString())) {
            items.add(weapon);
        }

        items.remove(waffe);
        weapon = waffe;

    }

    public void dequip(Waffe waffe) {
        if (!waffe.toString().equals(new Faust().toString())) {
            weapon = new Faust();
            items.add(waffe);
        }
    }

    public void remove(Item item) {
        items.remove(item);
    }
}
