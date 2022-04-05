package com.fuchsbau.shorin.Items;

import javafx.scene.text.Text;

public interface Item extends Comparable<Item> {

    Text getBeschreibung();

    void setBeschreibung(String beschreibung);

    String getuseText();

    void itemUse();

    void dequip();

    @Override
    String toString();

    boolean isBase();
}

