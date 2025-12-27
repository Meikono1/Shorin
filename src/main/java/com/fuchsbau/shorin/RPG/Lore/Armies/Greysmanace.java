package com.fuchsbau.shorin.RPG.Lore.Armies;

import com.fuchsbau.shorin.Engine.Options.GameOptions;
import com.fuchsbau.shorin.RPG.Lore.Textmark;

public class Greysmanace extends Textmark {

    public Greysmanace(String name) {
        super(name);
        super.getName().setFill(GameOptions.armies);
    }
}
