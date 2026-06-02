package com.howlite.cobblemoncards.util;

public class CardUtil {

    /**
     * Determines a fitting default background for a given Pokémon based on its name/ID.
     * This is used when a card has a holo effect but no background is set.
     */
    public static String getDefaultBackground(String pokemonId) {
        if (pokemonId == null) {
            return "skybg2";
        }
        String id = pokemonId.toLowerCase();
        
        // Water-type Pokémon backgrounds
        if (id.contains("water") || id.contains("squirt") || id.contains("blastoise") || 
            id.contains("magikarp") || id.contains("gyarados") || id.contains("vaporeon") || 
            id.contains("psyduck") || id.contains("golduck") || id.contains("lapras") || 
            id.contains("totodile") || id.contains("croconaw") || id.contains("feraligatr") || 
            id.contains("mudkip") || id.contains("marshtomp") || id.contains("swampert") || 
            id.contains("piplup") || id.contains("prinplup") || id.contains("empoleon") || 
            id.contains("froakie") || id.contains("frogadier") || id.contains("greninja") || 
            id.contains("aqu") || id.contains("fish") || id.contains("seadra") || 
            id.contains("horsea") || id.contains("kingdra") || id.contains("staryu") || 
            id.contains("starmie") || id.contains("wooper") || id.contains("quagsire") || 
            id.contains("sharpedo") || id.contains("wailmer") || id.contains("wailord") || 
            id.contains("kyogre") || id.contains("palkia") || id.contains("samurott") || 
            id.contains("buizel") || id.contains("floatzel") || id.contains("shellos") || 
            id.contains("gastrodon") || id.contains("finneon") || id.contains("lumineon") || 
            id.contains("manaphy") || id.contains("phione") || id.contains("oshawott") || 
            id.contains("dewott") || id.contains("sobble") || id.contains("drizzile") || 
            id.contains("inteleon") || id.contains("quaxly") || id.contains("quaxwell") || 
            id.contains("quaquaval")) {
            return "waterbg1";
        }
        
        // Fire-type Pokémon backgrounds
        if (id.contains("char") || id.contains("lav") || id.contains("slug") || 
            id.contains("magm") || id.contains("pyr") || id.contains("fire") || 
            id.contains("flareon") || id.contains("cinder") || id.contains("typhlosion") || 
            id.contains("chimchar") || id.contains("monferno") || id.contains("infernape") || 
            id.contains("tepig") || id.contains("pignite") || id.contains("emboar") || 
            id.contains("fennekin") || id.contains("braixen") || id.contains("delphox") || 
            id.contains("litten") || id.contains("torracat") || id.contains("incineroar") || 
            id.contains("scorbunny") || id.contains("raboot") || id.contains("cinderace") || 
            id.contains("fuecoco") || id.contains("crocalor") || id.contains("skeledirge") || 
            id.contains("growl") || id.contains("arcan") || id.contains("vulpix") || 
            id.contains("ninetal") || id.contains("moltre") || id.contains("ponyt") || 
            id.contains("rapid") || id.contains("numel") || id.contains("camerupt") || 
            id.contains("torkoal") || id.contains("heatran") || id.contains("darumaka") || 
            id.contains("darmanitan") || id.contains("litwick") || id.contains("lampent") || 
            id.contains("chandelure") || id.contains("cyndaquil") || id.contains("quilava")) {
            return "fire_embers";
        }
        
        // Grass-type Pokémon backgrounds
        if (id.contains("bulba") || id.contains("leaf") || id.contains("tree") || 
            id.contains("grass") || id.contains("forest") || id.contains("wood") || 
            id.contains("jung") || id.contains("chikorita") || id.contains("bayleef") || 
            id.contains("meganium") || id.contains("snivy") || id.contains("servine") || 
            id.contains("serperior") || id.contains("rowlet") || id.contains("dartrix") || 
            id.contains("decidueye") || id.contains("grookey") || id.contains("thwackey") || 
            id.contains("rillaboom") || id.contains("sprigatito") || id.contains("floragato") || 
            id.contains("meowscarada") || id.contains("oddish") || id.contains("gloom") || 
            id.contains("vilepl") || id.contains("bellsp") || id.contains("weepin") || 
            id.contains("victree") || id.contains("turtwig") || id.contains("grotle") || 
            id.contains("torterra") || id.contains("treecko") || id.contains("grovyle") || 
            id.contains("sceptile") || id.contains("shaymin") || id.contains("celebi") || 
            id.contains("tangela") || id.contains("tangrowth") || id.contains("eele") || 
            id.contains("leafeon") || id.contains("roselia") || id.contains("roserade")) {
            return "forestbg1";
        }
        
        // Electric-type Pokémon backgrounds
        if (id.contains("pika") || id.contains("magn") || id.contains("vol") || 
            id.contains("elec") || id.contains("rotom") || id.contains("pory") || 
            id.contains("jolteon") || id.contains("zapdos") || id.contains("raichu") || 
            id.contains("electab") || id.contains("electiv") || id.contains("mareep") || 
            id.contains("flaaff") || id.contains("amphar") || id.contains("shinx") || 
            id.contains("luxio") || id.contains("luxra") || id.contains("zeraora") || 
            id.contains("regieleki") || id.contains("xurkitree") || id.contains("heliopt") || 
            id.contains("heliolis") || id.contains("dedenne") || id.contains("togedemaru") || 
            id.contains("morpeko") || id.contains("yamper") || id.contains("boltund") || 
            id.contains("t विद्युत") || id.contains("plusle") || id.contains("minun") || 
            id.contains("pachirisu") || id.contains("emolga")) {
            return "neon_grid";
        }
        
        // Ghost/Dark-type Pokémon backgrounds
        if (id.contains("gengar") || id.contains("ghos") || id.contains("dark") || 
            id.contains("umbr") || id.contains("haun") || id.contains("spook") || 
            id.contains("mimi") || id.contains("sable") || id.contains("banette") || 
            id.contains("dusk") || id.contains("treven") || id.contains("pumpk") || 
            id.contains("litwi") || id.contains("lampent") || id.contains("chandel") || 
            id.contains("spiritomb") || id.contains("giratina") || id.contains("darkrai") || 
            id.contains("zoroark") || id.contains("zorua") || id.contains("absol") || 
            id.contains("yveltal") || id.contains("weavile") || id.contains("sneasel") || 
            id.contains("murkrow") || id.contains("honchkrow") || id.contains("houndour") || 
            id.contains("houndoom") || id.contains("tyranitar")) {
            return "plasma_bg";
        }
        
        // Rock/Ground/Steel-type Pokémon backgrounds
        if (id.contains("rock") || id.contains("geod") || id.contains("grav") || 
            id.contains("gole") || id.contains("ston") || id.contains("sand") || 
            id.contains("larv") || id.contains("pup") || id.contains("tyran") || 
            id.contains("onix") || id.contains("steel") || id.contains("rhyh") || 
            id.contains("rhyd") || id.contains("rhyper") || id.contains("aerod") || 
            id.contains("shieldon") || id.contains("bastiodon") || id.contains("cranidos") || 
            id.contains("rampardos") || id.contains("regirock") || id.contains("registeel") || 
            id.contains("terrakion") || id.contains("aron") || id.contains("lairon") || 
            id.contains("aggron") || id.contains("beldum") || id.contains("metang") || 
            id.contains("metagross") || id.contains("steelix") || id.contains("bronzor") || 
            id.contains("bronzong") || id.contains("probopass") || id.contains("bastiodon") || 
            id.contains("hippopotas") || id.contains("hippowdon") || id.contains("gible") || 
            id.contains("gabite") || id.contains("garchomp") || id.contains("diglett") || 
            id.contains("dugtrio") || id.contains("sandshrew") || id.contains("sandslash")) {
            return "rockbg1";
        }
        
        // Default fallback beautiful blue sky background
        return "skybg2";
    }

    /**
     * Récupère une espèce Cobblemon de manière robuste en essayant plusieurs formats de nettoyage.
     * Indispensable pour des espèces comme "Iron Leaves" -> "ironleaves", "Mr. Rime" -> "mrrime", etc.
     */
    public static com.cobblemon.mod.common.pokemon.Species getSpecies(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }

        // 1. Essai direct (nom original)
        if (isValidPath(name)) {
            try {
                com.cobblemon.mod.common.pokemon.Species species = com.cobblemon.mod.common.api.pokemon.PokemonSpecies.getByName(name);
                if (species != null) return species;
            } catch (Exception ignored) {}
        }

        // 2. Essai en minuscule
        String lower = name.toLowerCase();
        if (isValidPath(lower)) {
            try {
                com.cobblemon.mod.common.pokemon.Species species = com.cobblemon.mod.common.api.pokemon.PokemonSpecies.getByName(lower);
                if (species != null) return species;
            } catch (Exception ignored) {}
        }

        // 3. Essai sans aucun caractère spécial (espaces, tirets, points, tirets bas)
        String clean = lower.replace(" ", "")
                            .replace("-", "")
                            .replace("_", "")
                            .replace(".", "");
        if (isValidPath(clean)) {
            try {
                com.cobblemon.mod.common.pokemon.Species species = com.cobblemon.mod.common.api.pokemon.PokemonSpecies.getByName(clean);
                if (species != null) return species;
            } catch (Exception ignored) {}
        }

        // 4. Essai snake_case classique (espaces/tirets/points remplacés par des tirets bas)
        String snake = lower.replace(" ", "_")
                            .replace("-", "_")
                            .replace(".", "");
        if (isValidPath(snake)) {
            try {
                com.cobblemon.mod.common.pokemon.Species species = com.cobblemon.mod.common.api.pokemon.PokemonSpecies.getByName(snake);
                if (species != null) return species;
            } catch (Exception ignored) {}
        }

        return null;
    }

    private static boolean isValidPath(String path) {
        if (path == null) return false;
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (!(c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '_' || c == '-' || c == '.' || c == '/')) {
                return false;
            }
        }
        return true;
    }
}
