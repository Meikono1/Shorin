package com.fuchsbau.shorin.Spiel.Lore.Armies;

import com.fuchsbau.shorin.Optionen.GameOptionen;
import com.fuchsbau.shorin.Spiel.Lore.Textmark;

public class Greysmanace extends Textmark {


    public Greysmanace(String name) {
        super(name);
        super.getName().setFill(GameOptionen.armies);
    }


}
