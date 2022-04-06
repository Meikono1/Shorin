package com.fuchsbau.shorin.Items;

import javafx.scene.text.Text;

public interface Item extends Comparable<Item> {

    Text getText();

    void setText(String text);

    String getuseText();

    void itemUse();

    void dequip();

    @Override
    String toString();

    boolean isBase();
}

