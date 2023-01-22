package com.fuchsbau.shorin.Characters;

import com.fuchsbau.shorin.Optionen.GameOption;
import com.fuchsbau.shorin.RPG.Saveble;
import com.fuchsbau.shorin.RPG.SceneBuilder;
import javafx.scene.Scene;
import javafx.scene.text.TextFlow;


public class Player extends Character {

    private Saveble aktuell;
    private int savedstage;
    private int maxhealth = 100;
    private final int money;

    public int kitsune = 0;

    /*
    0 = keine erfahrungen
    1 = buch gelesen in Bibliothek

     */
    private String name;


    public Player() {
        super(100, 18, "Noname", 175,80,75,50, GameOption.player);
        money = 100;
    }


    public void setName(String name) {
        this.name = name;
        super.setText(name);

        setBeschreibung(getplayerinfo());
    }

    private String getplayerinfo() {
        StringBuilder builder = new StringBuilder();
        builder.append("Your name is: " + name + "\n");

        builder.append(getBody());


        return builder.toString();
    }

    @Override
    public TextFlow makeBeschreibung(TextFlow pane) {
        setBeschreibung(getplayerinfo());
        pane.getChildren().add(SceneBuilder.getSceneBuilder().makeText(getplayerinfo()));
        return pane;
    }

    public void setAktuell(Saveble aktuell, int stage) {
        if (this.aktuell != null && aktuell != this.aktuell) {
            this.aktuell.reset();
        }
        savedstage = stage;
        this.aktuell = aktuell;
    }

    public Scene getAktuell() {
        return aktuell.getScene(savedstage);
    }


    public int maxHealth() {
        return maxhealth;
    }

    public void increasemaxHealth(int health) {
        maxhealth += health;
        heal(health);
    }

    public String getMoney() {
        return String.valueOf(money);
    }
}
