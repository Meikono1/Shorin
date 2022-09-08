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
    private int size;
    private int masculinity;
    private int muscles;
    private int weight;
    private boolean penis;
    private boolean vagina;
    private String beschreibung;
    Item Weapon = new Unarmed();
    private Text text = SceneBuilder.makeText();


    public Character(int health, int age, String name, int size, int masculinity, int muscles, int weight, Paint paint) {
        this.health = health;
        this.age = age;
        this.size = size;
        this.masculinity = masculinity;
        this.muscles = muscles;
        this.weight = weight;
        this.text.setText(name);
        this.text.setFill(paint);
        beschreibung = "None";
    }

    public int getSize() {
        return size;
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

    public String getBody() {
        int cm = getSize();
        int m = cm / 100;
        StringBuilder builder = new StringBuilder("You are " + m + "." + (cm - (m * 100)) + " meter tall, " + getPronoun() + " witch a " + getBodytype() +" body.\n");
        builder.append("");

        return builder.toString();
    }

    private String getBodytype() {
        String back = "";

        if (weight < 15) {
            back = "Thin";
        } else if (weight < 30) {
            back = "Slender";
        } else if (weight < 45) {
            back = "Average";
        } else if (weight < 70) {
            if (muscles > 50 && masculinity < 35) {
                back = "Thick";
            } else {
                back = "Bulky";
            }
        } else if (weight < 85) {
            if (muscles > 80) {
                back = "Hulkish";
            } else if (muscles > 50) {
                back = "Heavy";
            } else {
                back = "Fat";
            }
        }

        return back;
    }

    private String getPronoun() {
        String back = "";
        if (masculinity < 15) {
            back = "Woman";
        } else if (masculinity < 30) {
            if (penis) {
                back = "Trap";
            } else {
                back = "Girl";
            }
        } else if (masculinity < 45) {
            if (penis) {
                back = "Tomboy";
            } else if (vagina) {
                back = "Femboy";
            } else {
                back = "Undefined";
            }
        } else if (masculinity < 70) {
            if (vagina) {
                back = "Reverse Tomboy";
            } else {
                back = "Boy";
            }
        } else if (masculinity < 85) {
            back = "Man";
        }

        return back;
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
