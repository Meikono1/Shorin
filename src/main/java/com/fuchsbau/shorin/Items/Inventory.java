package com.fuchsbau.shorin.Items;

import com.fuchsbau.shorin.Items.Gear.Armor;
import com.fuchsbau.shorin.Items.Gear.Arms.NoArms;
import com.fuchsbau.shorin.Items.Gear.Boots.ClothBoots;
import com.fuchsbau.shorin.Items.Gear.Boots.NoShoos;
import com.fuchsbau.shorin.Items.Gear.Head.ClothHelm;
import com.fuchsbau.shorin.Items.Gear.Head.NoHat;
import com.fuchsbau.shorin.Items.Gear.LowerBody.ClothPants;
import com.fuchsbau.shorin.Items.Gear.LowerBody.NoPants;
import com.fuchsbau.shorin.Items.Gear.UpperBody.ClothChest;
import com.fuchsbau.shorin.Items.Gear.UpperBody.NoChest;
import com.fuchsbau.shorin.Items.Weapons.Unarmed;
import com.fuchsbau.shorin.Items.Weapons.Weapon;
import com.fuchsbau.shorin.Optionen.GameOption;
import com.fuchsbau.shorin.RPG.Game;
import com.fuchsbau.shorin.RPG.Main;
import com.fuchsbau.shorin.RPG.SceneBuilder;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

import java.util.TreeMap;

public class Inventory {

    private Scene scene;
    private final TreeMap<Item, Integer> items;

    private Armor head;
    private Armor chest;
    private Armor boots;
    private Armor arms;
    private Armor pants;
    private Weapon weapon;


    public Inventory() {
        items = new TreeMap<>();

        head = new ClothHelm();
        chest = new ClothChest();
        weapon = new Unarmed();
        boots = new ClothBoots();
        arms = new NoArms();
        pants = new ClothPants();

        //TODO Mehr Items hinzufügen.
        //TODO Standart Melee weapon hinzufügen.

    }

    private void makeScene() {
        SceneBuilder.getSceneBuilder().resetButtonrows();
        Button back = SceneBuilder.getSceneBuilder().makeButton(1, "Back");
        back.setOnMouseClicked(event -> {
            GameOption.delete = false;
            Main.getStage().setScene(Game.getInstance().spieler.getAktuell());
        });
        SceneBuilder.getSceneBuilder().addButton(back, 1);

        Button delete = SceneBuilder.getSceneBuilder().makeButton(1, "Toggle delete");
        delete.setOnMouseClicked(event -> {
            GameOption.toggleDelete();
            Main.getStage().setScene(getScene());
        });
        SceneBuilder.getSceneBuilder().addButton(delete, 1);

        //TODO Informationen button, Bücher, Rassen, etc


        scene = new Scene(SceneBuilder.getSceneBuilder().makePlayerInventory(items, head, chest, arms, pants, boots, weapon));
    }

    public void addItem(Item item) {
        if (items.containsKey(item)) {
            items.replace(item, items.get(item) + 1);
        } else {
            items.put(item, 1);
        }
    }

    public Scene getScene() {
        makeScene();
        return scene;
    }

    public void equip(Weapon weapon) {
        if (!this.weapon.toString().equals(new Unarmed().toString())) {
            addItem(this.weapon);
        }

        if (items.get(weapon) > 1) {
            items.replace(weapon, items.get(weapon) - 1);
        } else {
            items.remove(weapon);
        }

        this.weapon = weapon;
    }

    public void equip(Armor armor) {
        switch (armor.getSlot()) {
            case head:
                if (!(head instanceof NoHat)) {
                    dequipArmor(armor);
                }
                head = armor;
                break;
            case boots:
                if (!(boots instanceof NoShoos)) {
                    dequipArmor(armor);
                }
                boots = armor;
                break;
            case chest:
                if (!(chest instanceof NoChest)) {
                    dequipArmor(armor);
                }
                chest = armor;
                break;
            case pants:
                if (!(pants instanceof NoPants)) {
                    dequipArmor(armor);
                }
                pants = armor;
                break;
            case arms:
                if (!(arms instanceof NoArms)) {
                    dequipArmor(armor);
                }
                arms = armor;
                break;
        }
        remove(armor);
    }

    public void dequipWeapon(Weapon weapon) {
        if (!weapon.toString().equals(new Unarmed().toString())) {
            this.weapon = new Unarmed();
            addItem(weapon);
        }
    }

    public void remove(Item item) {
        Integer anzahl = items.get(item);
        if (anzahl == 1) {
            items.remove(item);
        } else {
            items.replace(item, anzahl - 1);
        }
    }

    public TreeMap<Item, Integer> getItems() {
        return items;
    }

    public void dequipArmor(Armor armor) {
        switch (armor.getSlot()) {
            case head:
                if (armor.getClass().isInstance(NoHat.class)) {
                    return;
                } else {
                    addItem(armor);
                    head = new NoHat();
                }
                return;
            case boots:
                if (armor.getClass().isInstance(NoShoos.class)) {
                    return;
                } else {
                    addItem(armor);
                    boots = new NoShoos();
                }
                return;
            case chest:
                if (armor.getClass().isInstance(NoChest.class)) {
                    return;
                } else {
                    addItem(armor);
                    chest = new NoChest();
                }
                return;
            case pants:
                if (armor.getClass().isInstance(NoPants.class)) {
                    return;
                } else {
                    addItem(armor);
                    pants = new NoPants();
                }
                return;
            case arms:
                if (armor.getClass().isInstance(NoArms.class)) {
                    return;
                } else {
                    addItem(armor);
                    head = new NoArms();
                }
        }
    }


    public String save() {
        String builder = "{" +
                head.toString() + "," +
                chest.toString() + "," +
                boots.toString() + "," +
                arms.toString() + "," +
                pants.toString() + "," +
                weapon.toString() +
                "}\n";
        return builder;
    }

    public String getStats() {
        int armor = head.armor + chest.armor + boots.armor + arms.armor + pants.armor;
        int qualitaet = head.qualitaet + chest.qualitaet + boots.qualitaet + arms.qualitaet + pants.qualitaet;
        double zustand = head.zustand + chest.zustand + boots.zustand + arms.zustand + pants.zustand;

        return armor + ", " + qualitaet + ",  " + zustand / 5 + "%";
    }
}
