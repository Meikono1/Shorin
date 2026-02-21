package com.fuchsbau.shorin.Races;

import com.fuchsbau.shorin.Logger.FileLogger;
import com.fuchsbau.shorin.Races.Base.Race;
import com.fuchsbau.shorin.Races.Brutahk.*;
import com.fuchsbau.shorin.Races.Coralor.*;
import com.fuchsbau.shorin.Races.Fennari.*;
import com.fuchsbau.shorin.Races.Feylin.Changeling;
import com.fuchsbau.shorin.Races.Feylin.Halfling;
import com.fuchsbau.shorin.Races.Feylin.Pixie;
import com.fuchsbau.shorin.Races.Goblin.Goblin;
import com.fuchsbau.shorin.Races.Goblin.Hobgoblin;
import com.fuchsbau.shorin.Races.Humanic.Dwarf;
import com.fuchsbau.shorin.Races.Humanic.Elf;
import com.fuchsbau.shorin.Races.Humanic.Gnome;
import com.fuchsbau.shorin.Races.Humanic.Human;
import com.fuchsbau.shorin.Races.Hyrax.Bat;
import com.fuchsbau.shorin.Races.Hyrax.Gnoll;
import com.fuchsbau.shorin.Races.Hyrax.Jackal;
import com.fuchsbau.shorin.Races.Kataru.*;
import com.fuchsbau.shorin.Races.Kobold.Dragonkin;
import com.fuchsbau.shorin.Races.Kobold.Felbold;
import com.fuchsbau.shorin.Races.Kobold.Kobold;
import com.fuchsbau.shorin.Races.Merman.Delphinoid;
import com.fuchsbau.shorin.Races.Merman.Leviathan;
import com.fuchsbau.shorin.Races.Merman.Merman;
import com.fuchsbau.shorin.Races.Merman.Siren;
import com.fuchsbau.shorin.Races.Nagalith.*;
import com.fuchsbau.shorin.Races.Rodini.*;
import com.fuchsbau.shorin.Races.Tarpan.*;
import com.fuchsbau.shorin.Races.Tengu.Falcon;
import com.fuchsbau.shorin.Races.Tengu.Gryphon;
import com.fuchsbau.shorin.Races.Tengu.Harpy;
import com.fuchsbau.shorin.Races.Tengu.Owl;
import com.fuchsbau.shorin.Races.Veskral.*;

public enum RacesEnumToClass {
    ACINONYX(Acinonyx.class, Culture.KATARU),
    ARANEAE(Aranea.class, Culture.VESKRAL),
    ARTHROPOD(Arthropod.class, Culture.VESKRAL),
    ASINUS(Asinus.class, Culture.TARPAN),
    AXOLOTL(Axolotl.class, Culture.CORALOR),
    BAT(Bat.class, Culture.HYRAX),
    BOVINEA(Bovinea.class, Culture.TARPAN),
    CAPREOLINAE(Capreolinae.class, Culture.TARPAN),
    CARACAL(Caracal.class, Culture.KATARU),
    CARCHARHINI(Carcharhini.class, Culture.CORALOR),
    CAVIID(Caviid.class, Culture.RODINI),
    CERVINAE(Cervinae.class, Culture.TARPAN),
    CHINCHILLA(Chinchilla.class, Culture.RODINI),
    CETACEAN(Cetacean.class, Culture.CORALOR),
    CEPHALOPOD(Cephalopod.class, Culture.CORALOR),
    CHANGELING(Changeling.class, Culture.FEYLIN),
    CROCODILIAN(Crocodilian.class, Culture.CORALOR),
    DRACONICNAGA(DraconicNaga.class, Culture.NAGALITH),
    DRAGONKIN(Dragonkin.class, Culture.KOBOLD),
    DELPHINOID(Delphinoid.class, Culture.MERMAN),
    DWARF(Dwarf.class, Culture.HUMANIC),
    EUPLERID(Euplerid.class, Culture.RODINI),
    EQUUS(Equus.class, Culture.TARPAN),
    FALCON(Falcon.class, Culture.TENGU),
    FELBOLD(Felbold.class, Culture.KOBOLD),
    FENNEC(Fennec.class, Culture.FENNARI),
    ELF(Elf.class, Culture.HUMANIC),
    GIANT(Giant.class, Culture.BRUTAHK),
    GIRTABLILU(Girtablilu.class, Culture.VESKRAL),
    GNOME(Gnome.class, Culture.HUMANIC),
    GOBLIN(Goblin.class, Culture.GOBLIN),
    GOLIATH(Goliath.class, Culture.BRUTAHK),
    GRYPHON(Gryphon.class, Culture.TENGU),
    GNOLL(Gnoll.class, Culture.HYRAX),
    HALFLING(Halfling.class, Culture.FEYLIN),
    HARPY(Harpy.class, Culture.TENGU),
    HIPPOCAMPUS(Hippocampus.class, Culture.CORALOR),
    HIPPOTIGRIS(Hippotigris.class, Culture.TARPAN),
    HUMAN(Human.class, Culture.HUMANIC),
    HOBGOBLIN(Hobgoblin.class, Culture.GOBLIN),
    HUSKY(Husky.class, Culture.FENNARI),
    JACKAL(Jackal.class, Culture.HYRAX),
    JERBOA(Jerboa.class, Culture.RODINI),
    KITSUNE(Kitsune.class, Culture.FENNARI),
    KOBOLD(Kobold.class, Culture.KOBOLD),
    LAMIA(Lamia.class, Culture.NAGALITH),
    LEPORID(Leporid.class, Culture.RODINI),
    LEVIATHAN(Leviathan.class, Culture.MERMAN),
    MERMAN(Merman.class, Culture.MERMAN),
    MURID(Murid.class, Culture.RODINI),
    MUSTELID(Mustelid.class, Culture.RODINI),
    MYGALOMORPH(Mygalomorph.class, Culture.VESKRAL),
    OPILIONES(Opiliones.class, Culture.VESKRAL),
    OWL(Owl.class, Culture.TENGU),
    PANTHERINE(Pantherine.class, Culture.KATARU),
    PINNIPED(Pinniped.class, Culture.CORALOR),
    PIXIE(Pixie.class, Culture.FEYLIN),
    REPTILID(Reptilid.class, Culture.NAGALITH),
    SAOLA(Saola.class, Culture.TARPAN),
    SERATHI(Serathi.class, Culture.NAGALITH),
    SERVAL(Serval.class, Culture.KATARU),
    SHEPHERD(Shepherd.class, Culture.FENNARI),
    SIREN(Siren.class, Culture.MERMAN),
    SQUIRREL(Squirrel.class, Culture.RODINI),
    URSINE(Ursine.class, Culture.BRUTAHK),
    VIPER(Viper.class, Culture.NAGALITH),
    WOLFKIN(Wolfkin.class, Culture.FENNARI);

    private static final FileLogger logger = FileLogger.getInstance();

    public final Class<? extends Race> raceClass;
    public final Culture culture;
    private final String display;


    RacesEnumToClass(Class<? extends Race> raceClass, RacesEnumToClass.Culture culture) {
        this.raceClass = raceClass;
        this.culture = culture;
        this.display = toDisplay(name());
    }

    public static RacesEnumToClass safeParse(String raw) {
        if (raw == null) return null;

        var logger = FileLogger.getLogger();

        String trimmed = raw.trim();
        if (trimmed.isEmpty()) return null;

        // 1) exakte Gleichheit
        for (RacesEnumToClass r : values()) {
            if (r.name().equals(trimmed)) return r;
        }

        // 2) Normalisierung auf UPPER
        String upper = trimmed.toUpperCase();
        for (RacesEnumToClass r : values()) {
            if (r.name().equals(upper)) {
                logger.info("Scenario race alias normalized: '" + raw + "' -> '" + r.name() + "'");
                return r;
            }
        }

        // 3) Fuzzy: max 2 Zeichen Unterschied (Levenshtein)
        RacesEnumToClass best = null;
        int bestDist = Integer.MAX_VALUE;

        for (RacesEnumToClass r : values()) {
            int d = levenshteinAtMost(upper, r.name(), 2);
            if (d >= 0 && d < bestDist) {
                bestDist = d;
                best = r;
                if (bestDist == 0) break;
            }
        }

        if (best != null) {
            logger.severe("Scenario race fuzzy match (dist=" + bestDist + "): '" + raw + "' -> '" + best.name() + "'");
            return best;
        } else {
            logger.severe("Scenario race unknown (no match within dist<=2): '" + raw + "'");
            return null;
        }
    }

    /**
     * Levenshtein distance mit Limit
     */
    private static int levenshteinAtMost(String a, String b, int max) {
        int la = a.length(), lb = b.length();
        if (Math.abs(la - lb) > max) return -1;

        int[] prev = new int[lb + 1];
        int[] curr = new int[lb + 1];

        for (int j = 0; j <= lb; j++) prev[j] = j;

        for (int i = 1; i <= la; i++) {
            curr[0] = i;
            int rowMin = curr[0];
            char ca = a.charAt(i - 1);

            for (int j = 1; j <= lb; j++) {
                int cost = (ca == b.charAt(j - 1)) ? 0 : 1;
                int v = Math.min(
                        Math.min(curr[j - 1] + 1, prev[j] + 1),
                        prev[j - 1] + cost
                );
                curr[j] = v;
                if (v < rowMin) rowMin = v;
            }

            if (rowMin > max) return -1;

            int[] tmp = prev; prev = curr; curr = tmp;
        }

        int dist = prev[lb];
        return dist <= max ? dist : -1;
    }

    public String displayName() {
        return display;
    }

    private static String toDisplay(String raw) {
        String[] parts = raw.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String p = parts[i];
            if (p.isEmpty()) continue;
            sb.append(Character.toUpperCase(p.charAt(0)))
                    .append(p.substring(1));
            if (i < parts.length - 1) sb.append(' ');
        }
        return sb.toString();
    }


    public enum Culture {
        BRUTAHK,
        CORALOR,
        FENNARI,
        FEYLIN,
        GOBLIN,
        HUMANIC,
        HYRAX,
        KATARU,
        KOBOLD,
        MERMAN,
        NAGALITH,
        RODINI,
        TARPAN,
        TENGU,
        VESKRAL
    }
}
