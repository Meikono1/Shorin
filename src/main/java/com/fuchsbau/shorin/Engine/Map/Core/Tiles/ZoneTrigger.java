package com.fuchsbau.shorin.Engine.Map.Core.Tiles;


public final class ZoneTrigger {
    public enum TriggerType {
        TELEPORT,
        TEXT,
        NPC
    }

    public enum ActivationMode {
        ON_ENTER,
        ON_INTERACT
    }

    // --- Felder ---

    public final TriggerType type;
    public final ActivationMode activation;

    /**
     * TELEPORT: Ziel-Map-Datei (z.B. "maps/locations/whitebridge.shorin")
     */
    public final String targetMap;

    /**
     * TELEPORT: Spawn-Zeile auf der Zielmap (-1 = Standardspawn)
     */
    public final int targetRow;

    /**
     * TELEPORT: Spawn-Spalte auf der Zielmap (-1 = Standardspawn)
     */
    public final int targetCol;

    /**
     * TEXT: Der angezeigte Text / die Raumbeschreibung
     */
    public final String displayText;

    /**
     * NPC: NPC-ID oder Name (z.B. "BlacksmithGunnar")
     */
    public final String npcId;

    // --- Konstruktoren ---
    private ZoneTrigger(TriggerType type, ActivationMode activation,
                        String targetMap, int targetRow, int targetCol,
                        String displayText, String npcId) {
        this.type = type;
        this.activation = activation;
        this.targetMap = targetMap;
        this.targetRow = targetRow;
        this.targetCol = targetCol;
        this.displayText = displayText;
        this.npcId = npcId;
    }

    /**
     * Teleport-Trigger: beim Betreten zur Zielmap springen.
     *
     * @param targetMap Pfad zur Ziel-.shorin-Datei
     * @param targetRow Spawn-Zeile auf der Zielmap
     * @param targetCol Spawn-Spalte auf der Zielmap
     */
    public static ZoneTrigger teleport(String targetMap, int targetRow, int targetCol) {
        return new ZoneTrigger(
                TriggerType.TELEPORT, ActivationMode.ON_ENTER,
                targetMap, targetRow, targetCol,
                null, null
        );
    }

    /**
     * Text-Trigger: beim Betreten eine Beschreibung anzeigen.
     *
     * @param text Der anzuzeigende Text
     */
    public static ZoneTrigger text(String text) {
        return new ZoneTrigger(
                TriggerType.TEXT, ActivationMode.ON_ENTER,
                null, -1, -1,
                text, null
        );
    }

    /**
     * NPC-Trigger: auf [E]-Druck NPC-Dialog / Handel starten.
     *
     * @param npcId ID oder Name des NPCs
     */
    public static ZoneTrigger npc(String npcId) {
        return new ZoneTrigger(
                TriggerType.NPC, ActivationMode.ON_INTERACT,
                null, -1, -1,
                null, npcId
        );
    }

    @Override
    public String toString() {
        return switch (type) {
            case TELEPORT -> "TELEPORT -> " + targetMap + " (" + targetRow + "," + targetCol + ")";
            case TEXT -> "TEXT: " + (displayText != null && displayText.length() > 30
                    ? displayText.substring(0, 30) + "..." : displayText);
            case NPC -> "NPC: " + npcId;
        };
    }
}