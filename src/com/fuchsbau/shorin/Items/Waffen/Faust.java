package com.fuchsbau.shorin.Items.Waffen;

import com.fuchsbau.shorin.Items.Materialen;

public class Faust extends Waffe {
    public Faust() {
        super(2, 1, 1, Materialen.stein, "Steinfaust");

    }

    public Faust(int schaden, int zustand, int qualitaet, Materialen material, String text) {
        super(schaden, zustand, qualitaet, material, text);
    }
}
