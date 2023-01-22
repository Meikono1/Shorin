package com.fuchsbau.shorin.Items;

public enum Material {
    stone("Stone"), wood("Wooden"), copper("Copper"), bronze("Bronze"), iron("Iron");

    private final String beschreibung;

    Material(String name) {
        beschreibung = name;
    }

    public String getBeschreibung() {
        return beschreibung;
    }


}
