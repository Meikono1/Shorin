package com.fuchsbau.shorin.Items;

public enum Material {
    stein("Stone"), holz("Wooden"), kupfer("Copper"), bronze("Bronze"), eisen("Iron");

    private final String beschreibung;

    Material(String name) {
        beschreibung = name;
    }

    public String getBeschreibung() {
        return beschreibung;
    }


}
