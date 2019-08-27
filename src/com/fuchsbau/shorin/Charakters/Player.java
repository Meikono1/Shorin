package com.fuchsbau.shorin.Charakters;


public class Player {
    public int dave = 0;
    /*
    0= noch nicht geredet
     */
    int kitsune = 0;

    /*
    0 = keine erfahrungen
    1 = buch gelesen in Bibliothek

     */
    private String name = "Your name";


    public Player() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
