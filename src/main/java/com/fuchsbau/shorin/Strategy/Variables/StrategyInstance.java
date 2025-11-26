package com.fuchsbau.shorin.Strategy.Variables;

import com.fuchsbau.shorin.RPG.Game;
import com.fuchsbau.shorin.RPG.SceneBuilder;
import com.fuchsbau.shorin.Strategy.Home.Storage;
import javafx.scene.text.Text;

public class StrategyInstance {
    private static StrategyInstance instance;
    public Storage storage;
    public double forestQKM;



    private StrategyInstance() {
        storage = new Storage();
        forestQKM = Math.PI * (Game.getInstance().kaguyaForest.radius * Game.getInstance().kaguyaForest.radius);
    }


    public static StrategyInstance getInstance() {
        if (instance == null) {
            instance = new StrategyInstance();
        }
        return instance;
    }

    public Text getForestText() {
        int a = (int) (forestQKM * 100);
        double b = a / 100.00;
        if (b == (int) (b * 10) / 10.0) {
            return SceneBuilder.getSceneBuilder().makeText("Remaining Forest: " + b + "0" + " qkm" + "\n");
        }
        return SceneBuilder.getSceneBuilder().makeText("Remaining Forest: " + b + " qkm" + "\n");
    }

    public void logging(int meter) {
        double km = meter / 100.0;
        double sqm = km * km;
        forestQKM -= sqm;
    }
}
