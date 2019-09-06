package com.fuchsbau.shorin.Charakters.Humans;


import com.fuchsbau.shorin.SceneBuilder;
import javafx.scene.text.Text;


public class Player {

    int kitsune = 0;

    /*
    0 = keine erfahrungen
    1 = buch gelesen in Bibliothek

     */
    private Text name = SceneBuilder.makeText();


    public Player() {

    }

    public Text getName() {
        return name;
    }

    public void setName(String name) {
        this.name.setText(name);

    }
}
