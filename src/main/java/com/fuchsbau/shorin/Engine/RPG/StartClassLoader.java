package com.fuchsbau.shorin.Engine.RPG;

import java.util.HashMap;

public class StartClassLoader {

    private static final HashMap <String, Saveble> savedStarts = new HashMap<>();

    public static Saveble getEntry(String entry){
        if (savedStarts.isEmpty()){
            initialise();
        }
        return savedStarts.get(entry);
    }

    private static void initialise() {
        savedStarts.put("FreeStartShipArrivalHumans", null);
    }
}
