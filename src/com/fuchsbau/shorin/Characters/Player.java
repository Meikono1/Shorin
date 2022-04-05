package com.fuchsbau.shorin.Characters;

import com.fuchsbau.shorin.Optionen.GameOption;
import com.fuchsbau.shorin.Spiel.Saveble;
import com.fuchsbau.shorin.Spiel.SceneBuilder;
import javafx.scene.Scene;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;


public class Player extends Character {

    private Saveble aktuell;
    private int savedstage;
    private int maxhealth = 100;
    private int fuchs;

    public int kitsune = 0;

    /*
    0 = keine erfahrungen
    1 = buch gelesen in Bibliothek

     */
    private String name;


    public Player() {
        super(100);
        fuchs = 100;
    }

    public Text getName() {
        Text back = SceneBuilder.makeText(name);
        back.setFill(Paint.valueOf("#ACB069"));
        return back;
    }


    public void setName(String name) {
        this.name = name;

        setBeschreibung("Your name is: " + name + ".\nYou are an average Human with a height of 1.8 Meters. Due to your training you have a masculine Body.\n");
    }

    @Override
    public TextFlow makeBeschreibung(TextFlow pane) {
        setBeschreibung("Your name is: " + name + ".\nYou are an average Human with a height of 1.8 Meters. Due to your training you have a masculine Body.\n");

        //@TODO neue Beschreibung
        pane.getChildren().add(SceneBuilder.makeText("Your name is: "));

        Text text = SceneBuilder.makeText(name);
        text.setFill(GameOption.player);
        pane.getChildren().add(text);

        pane.getChildren().add(SceneBuilder.makeText(".\nYou are an average Human with a height of 1.8 Meters. Due to your training you have a masculine Body.\n"));


        return pane;
    }

    public void setAktuell(Saveble aktuell, int stage) {
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

    public String getFuchs() {
        return String.valueOf(fuchs);
    }
}
