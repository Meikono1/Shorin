package com.fuchsbau.shorin.Items.Waffen;

import com.fuchsbau.shorin.Items.Materialen;
import com.fuchsbau.shorin.Items.Waffe;

public class Faust extends Waffe {
    public Faust() {
        super(2,1,1,Materialen.stein);

    }

    public Faust(int schaden, int zustand, int qualitaet, Materialen material) {
        super(schaden, zustand, qualitaet, material);
    }
}
