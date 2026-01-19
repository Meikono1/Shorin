package com.fuchsbau.shorin.RPG.Intro;

import javafx.beans.property.*;

public class CharacterCreatorBinder {
    public StringProperty name = new SimpleStringProperty("");
    public StringProperty race = new SimpleStringProperty("Human");
    public StringProperty sex = new SimpleStringProperty("Male");
    public IntegerProperty age = new SimpleIntegerProperty(25);
    public DoubleProperty heightCm = new SimpleDoubleProperty(180);
    public StringProperty startClass = new SimpleStringProperty("");
    public DoubleProperty feminity = new SimpleDoubleProperty();
    public StringProperty bodySize = new SimpleStringProperty();
    public StringProperty muscleDefinition = new SimpleStringProperty();
    public StringProperty bodyHeight = new SimpleStringProperty();

    public final IntegerProperty str = new SimpleIntegerProperty(0);
    public final IntegerProperty dex = new SimpleIntegerProperty(0);
    public final IntegerProperty con = new SimpleIntegerProperty(0);
    public final IntegerProperty intel = new SimpleIntegerProperty(0);
    public final IntegerProperty wis = new SimpleIntegerProperty(0);
    public final IntegerProperty cha = new SimpleIntegerProperty(0);

    // min pro Stat (z.B. durch Rassen-Malus). Standard 0.
    public final IntegerProperty minStr = new SimpleIntegerProperty(0);
    public final IntegerProperty minDex = new SimpleIntegerProperty(0);
    public final IntegerProperty minCon = new SimpleIntegerProperty(0);
    public final IntegerProperty minInt = new SimpleIntegerProperty(0);
    public final IntegerProperty minWis = new SimpleIntegerProperty(0);
    public final IntegerProperty minCha = new SimpleIntegerProperty(0);

}
