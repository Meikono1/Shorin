package com.fuchsbau.shorin.Engine.RPG;

public class Language {
    public String name = "";
    public String description = "";
    public String decryptionTemplate = ""; // Platzhalter für spätere Decryption-Datei

    public Language() {}

    public Language(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public String toString() {
        return name;
    }
}