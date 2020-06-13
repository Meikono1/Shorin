package com.fuchsbau.shorin.Items;

public enum Materialen {
    stein("Stone"), holz("Wooden"), kupfer("Copper"), bronze("Bronze"), eisen("Iron");

    private String beschreibung;

    Materialen(String name) {
        beschreibung = name;
    }

    public String getBeschreibung() {
        return beschreibung;
    }


}
