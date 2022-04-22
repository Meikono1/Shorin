package com.fuchsbau.shorin.Characters;

import com.fuchsbau.shorin.Items.Item;
import com.fuchsbau.shorin.Items.Weapons.Unarmed;
import com.fuchsbau.shorin.Spiel.SceneBuilder;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class Character {

    private int age;
    private int health;
    private String beschreibung;
    Item Weapon = new Unarmed();
    private Text text = SceneBuilder.makeText();


    public Character(int health, int age, String name, Paint paint) {
        this.health = health;
        this.age = age;
        this.text.setText(name);
        this.text.setFill(paint);
        beschreibung = "None";
    }

    public void setWeapon(Item weapon) {
        Weapon = weapon;
    }

    public Item getWeapon() {
        return Weapon;
    }

    public int getHealth() {
        return health;
    }

    public TextFlow makeBeschreibung(TextFlow pane) {
        return pane;
    }

    public void setBeschreibung(String beschreibung) {
        this.beschreibung = beschreibung;
    }

    public String getBeschreibung() {
        return beschreibung;
    }

    public void heal(int i) {
        health += i;
    }

    protected void setText(String name) {
        this.text.setText(name);
    }

    public Text getText() {
        Text back = SceneBuilder.makeText(this.text.getText());
        back.setFill(text.getFill());
        return back;
    }

    public int getAge() {
        return age;
    }
}
