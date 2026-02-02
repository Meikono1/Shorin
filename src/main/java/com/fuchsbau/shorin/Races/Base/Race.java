package com.fuchsbau.shorin.Races.Base;

import com.fuchsbau.shorin.Engine.Options.GameOptions;
import com.fuchsbau.shorin.Engine.SceneBuilder;
import javafx.scene.text.Text;

public class Race implements RaceInterface {
    private short heightInCm;
    private Size size;
    private final String raceName;
    private final String name;
    private final String description;
    private final Attributes attributes;
    private final LifeStages lifeStage;
    private final Reproduction reproduction;
    private final Appearance appearance;
    private final Text inGameDescription = SceneBuilder.getSceneBuilder().makeText();

    public Race(String raceName, String name, String description, Attributes baseAttributes, LifeStages lifeStage, Reproduction reproduction, Appearance appearance) {
        this.raceName = raceName;
        this.name = name;
        this.description = description;
        this.attributes = baseAttributes;
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
}
