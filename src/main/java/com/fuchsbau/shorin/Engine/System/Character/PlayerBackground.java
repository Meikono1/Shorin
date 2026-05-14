package com.fuchsbau.shorin.Engine.System.Character;

import java.util.ArrayList;
import java.util.List;

public class PlayerBackground {

    public String name = "";
    public String description = "";

    public List<AbilityScore> choiceBoosts = new ArrayList<>();
    public int freeBoosts = 1;

    public List<String> skills = new ArrayList<>();
    public List<String> lores = new ArrayList<>();
    public List<String> feats = new ArrayList<>();
}