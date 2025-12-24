package com.fuchsbau.shorin.Characters;

import com.fuchsbau.shorin.Engine.Optionen.GameOption;
import com.fuchsbau.shorin.RPG.Saveble;
import com.fuchsbau.shorin.Engine.SceneBuilder;
import javafx.scene.Scene;
import javafx.scene.text.TextFlow;


public class Player extends Character {

    private Saveble currentScene;
    private int savedStage;
    private int maxHealth = 100;
    private final int money;

    public int kitsune = 0;

    /*
    0 = keine erfahrungen
    1 = buch gelesen in Bibliothek

     */
    private String name;


    public Player() {
        super(100, 18, "Noname", 175, 80, 75, 50, GameOption.player);
        money = 100;
    }


    public void setName(String name) {
        this.name = name;
        super.setText(name);

        setBeschreibung(getplayerinfo());
    }

    private String getplayerinfo() {
        return "Your name is: " + name + "\n" + getBody();
    }

    @Override
    public TextFlow makeBeschreibung(TextFlow pane) {
        setBeschreibung(getplayerinfo());
        pane.getChildren().add(SceneBuilder.getSceneBuilder().makeText(getplayerinfo()));
        return pane;
    }

    public void setCurrentScene(Saveble CurrentScene, int stage) {
        if (this.currentScene != null && CurrentScene != this.currentScene) {
            this.currentScene.reset();
        }
        savedStage = stage;
        this.currentScene = CurrentScene;
    }

    public Scene getCurrentScene() {
        return currentScene.getScene(savedStage);
    }


    public int maxHealth() {
        return maxHealth;
    }

    public void increasemaxHealth(int health) {
        maxHealth += health;
        heal(health);
    }

    public String getMoney() {
        return String.valueOf(money);
    }
}
