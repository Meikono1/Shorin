package com.fuchsbau.shorin.Races.Base;

import com.fuchsbau.shorin.Engine.Options.GameOptions;
import com.fuchsbau.shorin.Engine.Race.Size;
import com.fuchsbau.shorin.Engine.SceneBuilder;
import javafx.scene.text.Text;

public class Race implements RaceInterface {

    // Descriptive
    private final String raceName;
    private final String name;
    private final Text inGameDescription = SceneBuilder.getSceneBuilder().makeText();
    private short heightInCm;

    // Game Relevant
    private final Attributes attributes;
    private int maxHealth;
    private int currentHealth;
    private Size size;
    private byte speed;

    // Simulation
    private final LifeStages lifeStage;
    private final Reproduction reproduction;
    private final Appearance appearance;

    public Race(String raceName, String name, byte speed, Attributes baseAttributes, int maxHealth, Size size, LifeStages lifeStage, Reproduction reproduction, Appearance appearance) {
        this.raceName = raceName;
        this.name = name;
        this.speed = speed;
        this.attributes = baseAttributes;
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
        this.size = size;
        this.lifeStage = lifeStage;
        this.reproduction = reproduction;
        this.appearance = appearance;
        inGameDescription.setText(name);
        inGameDescription.setFill(GameOptions.cityColor);
    }

    public Attributes getAttributes() {
        return attributes;
    }


    @Override
    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public byte getSpeed() {
        return speed;
    }

    public void setSpeed(byte speed) {
        this.speed = speed;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public void setCurrentHealth(int currentHealth) {
        this.currentHealth = currentHealth;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    @Override
    public String raceName() {
        return raceName;
    }

    @Override
    public String displayName() {
        return name;
    }

    @Override
    public LifeStages lifeStage() {
        return null;
    }

    @Override
    public Reproduction reproduction() {
        return reproduction;
    }

    @Override
    public Appearance appearance() {
        return appearance;
    }

    @Override
    public Attributes baseAttributes() {
        return attributes;
    }

    @Override
    public String shortDescription() {
        return "";
    }

    @Override
    public String describeTypicalChildhood() {
        return "";
    }

    @Override
    public String describeAdultRole() {
        return "";
    }

    public LifeStages getLifeStage() {
        return lifeStage;
    }
}
