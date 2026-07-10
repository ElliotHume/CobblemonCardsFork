package com.howlite.cobblemoncards.util;

import com.cobblemon.mod.common.pokemon.Species;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.howlite.cobblemoncards.CobblemonCards;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Assigns Pokemon to an automatic collectability tier and uses that tier to
 * weight which species can occupy each card-rarity slot.
 *
 * <p>Defaults are inferred from Cobblemon metadata, then adjusted by the curated
 * forced lists below. Server owners can override the inferred tier, individual
 * weights, or minimum/maximum rarity for any species in
 * {@code config/cobblemon-cards-species-rarity.json}.</p>
 */
public final class SpeciesRarityManager {
    private static final int CONFIG_VERSION = 3;
    // Cross-platform config path: works on both Fabric and NeoForge
    private static final Path CONFIG_PATH = Paths.get("config", "cobblemon-cards-species-rarity.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Map<SpeciesTier, EnumMap<CardRarity, Double>> TIER_WEIGHTS =
            new EnumMap<>(SpeciesTier.class);
    private static final Map<String, SpeciesOverride> OVERRIDES = new HashMap<>();
    private static final Map<String, SpeciesTier> TIER_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Species> SPECIES_CACHE = new ConcurrentHashMap<>();

    private static volatile boolean initialized;
    private static boolean enabled = true;
    private static boolean applyToCaptureDrops = true;

    private static final Set<String> FORCED_COMMON = idSet(
            "watchog,wugtrio,vanillish,wormadam,huntail,gorebyss,kakuna,metapod,spidops," +
            "kricketune,dustox,ledian,beautifly,beedrill,butterfree,ariados,delcatty,minun,pachirisu," +
            "parasect,plusle,shiinotic,bibarel,corsola,medicham,pyukumuku,sudowoodo,vivillon,raticate," +
            "furret,squawkabilly,gumshoos,castform,linoone,mightyena,diggersby,mothim,dugtrio,marowak," +
            "sunflora,swoobat,emolga,illumise,magcargo,volbeat,dedenne,pincurchin,togedemaru,wigglytuff," +
            "morpeko,pelipper,perrserker,persian,qwilfish,fearow,liepard,arbok,cherrim,granbull,klawf," +
            "lokix,sandslash,seaking,venomoth,noctowl,purugly,araquanid,carnivine,masquerain,chimecho," +
            "jynx,swellow,thievul,seviper,zangoose,basculin,camerupt,eldegoss,greedent,jumpluff,lanturn," +
            "lumineon,sharpedo,tropius,maractus,aromatisse,ribombee,sawk,stantler,throh,meowstic," +
            "swalot,whiscash,alomomola,cinccino,dodrio,grumpig,maushold,rabsca,stonjourner,xatu," +
            "stunfisk,furfrou,swanna,garbodor,vespiquen,bruxish,cacturne,cramorant,dewgong,kingler," +
            "sawsbuck,dachsbun,crabominable,veluza,pidgeot,skuntank,brambleghast,darmanitan,glalie," +
            "grapploct,jellicent,komala,lilligant,lopunny,ludicolo,lurantis,octillery,orthworm,palossand," +
            "shiftry,slurpuff,espathra,heliolisk,ambipom,malamar,clefable,hypno,durant,heatmor,beheeyem," +
            "bombirdier,comfey,crustle,drednaw,druddigon,grafaiai,mantine,relicanth,scolipede,toucannon," +
            "turtonator,scovillain,musharna,houndstone,unfezant,oinkologne,barraskewda,bellossom"
    );

    private static final Set<String> FORCED_UNCOMMON = idSet(
            "absol,arcanine,arctozolt,armaldo,carracosta,cradily,crobat,gyarados,kabutops,omastar," +
            "rampardos,tangrowth,vanilluxe,amaura,anorith,archen,cranidos,kabuto,lileep,omanyte," +
            "shieldon,tirtouga,tyrunt"
    );

    private static final Set<String> FORCED_RARE = idSet(
            "venusaur,charizard,blastoise,meganium,typhlosion,feraligatr,sceptile,blaziken,swampert," +
            "torterra,infernape,empoleon,serperior,emboar,samurott,chesnaught,delphox,greninja," +
            "decidueye,incineroar,primarina,rillaboom,cinderace,inteleon,meowscarada,skeledirge," +
            "quaquaval,vaporeon,jolteon,flareon,espeon,umbreon,leafeon,glaceon,sylveon,aerodactyl," +
            "bastiodon,archeops,aurorus,tyrantrum,dracozolt,dracovish,arctovish," +
            "aegislash,alakazam,armarouge," +
            "ceruledge,gallade,gardevoir,gengar,lucario,machamp,mimikyu,palafin,scizor,shedinja," +
            "spiritomb,tinkaton,zoroark,altaria,chandelure,dragonair,flygon,froslass,glimmora,gliscor," +
            "hatterene,hawlucha,lunatone,solrock,rotom,sneasler,steelix"
    );

    private SpeciesRarityManager() {
    }

    public static synchronized void initialize() {
        if (initialized) return;

        resetDefaults();
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            if (Files.notExists(CONFIG_PATH)) {
                Files.writeString(CONFIG_PATH, GSON.toJson(createDefaultConfig()), StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE_NEW);
                CobblemonCards.LOGGER.info("Created species rarity configuration at {}", CONFIG_PATH);
            } else {
                migrateConfigIfNeeded();
            }
            loadConfig();
        } catch (Exception exception) {
            resetDefaults();
            CobblemonCards.LOGGER.error(
                    "Unable to load {}. Built-in species rarity weights will be used.", CONFIG_PATH, exception);
        }

        initialized = true;
        CobblemonCards.LOGGER.info(
                "Species-aware card rarity enabled: {}, capture/faint weighting: {}, overrides: {}",
                enabled, applyToCaptureDrops, OVERRIDES.size());
    }

    public static void invalidateCaches() {
        TIER_CACHE.clear();
        SPECIES_CACHE.clear();
    }

    /**
     * Selects a species for a booster slot whose rarity has already been rolled.
     * The themed/generation pool supplied by the booster remains authoritative.
     */
    public static String selectSpecies(List<String> eligibleSpecies, String rarityName, Random random) {
        initialize();
        if (eligibleSpecies == null || eligibleSpecies.isEmpty()) {
            throw new IllegalArgumentException("Cannot select a card species from an empty pool");
        }
        if (!enabled) return eligibleSpecies.get(random.nextInt(eligibleSpecies.size()));

        CardRarity rarity = CardRarity.fromName(rarityName);
        if (rarity == null) return eligibleSpecies.get(random.nextInt(eligibleSpecies.size()));

        List<WeightedSpecies> weighted = new ArrayList<>(eligibleSpecies.size());
        double totalWeight = 0.0;
        for (String pokemonId : eligibleSpecies) {
            Species species = resolveSpecies(pokemonId);
            if (species == null) continue;

            double weight = getWeight(species, rarity);
            if (weight <= 0.0) continue;
            totalWeight += weight;
            weighted.add(new WeightedSpecies(pokemonId, totalWeight));
        }

        if (weighted.isEmpty() || totalWeight <= 0.0) {
            CobblemonCards.LOGGER.warn(
                    "No species has positive weight for rarity '{}' in a {}-species booster pool; using uniform fallback.",
                    rarityName, eligibleSpecies.size());
            return eligibleSpecies.get(random.nextInt(eligibleSpecies.size()));
        }

        double roll = random.nextDouble() * totalWeight;
        for (WeightedSpecies entry : weighted) {
            if (roll < entry.cumulativeWeight()) return entry.pokemonId();
        }
        return weighted.getLast().pokemonId();
    }

    /**
     * Rolls card rarity for a capture/faint drop when the species is already known.
     * Mythic remains reserved for special systems such as the Instant Dex.
     * Returns {@code null} when capture weighting is disabled.
     */
    public static String rollCaptureRarity(Species species, Random random) {
        initialize();
        if (!enabled || !applyToCaptureDrops || species == null) return null;

        double totalWeight = 0.0;
        EnumMap<CardRarity, Double> cumulative = new EnumMap<>(CardRarity.class);
        CardRarity lastEligibleRarity = null;
        for (CardRarity rarity : CardRarity.values()) {
            if (rarity == CardRarity.MYTHIC) continue;
            double weight = getWeight(species, rarity);
            if (weight <= 0.0) continue;
            totalWeight += weight;
            cumulative.put(rarity, totalWeight);
            lastEligibleRarity = rarity;
        }

        if (totalWeight <= 0.0) return null;
        double roll = random.nextDouble() * totalWeight;
        for (Map.Entry<CardRarity, Double> entry : cumulative.entrySet()) {
            if (roll < entry.getValue()) return entry.getKey().serializedName;
        }
        return lastEligibleRarity == null ? null : lastEligibleRarity.serializedName;
    }

    public static String getTierName(Species species) {
        initialize();
        SpeciesOverride override = getOverride(species);
        SpeciesTier tier = override != null && override.tier != null ? override.tier : getTier(species);
        return tier.serializedName;
    }

    private static double getWeight(Species species, CardRarity rarity) {
        SpeciesOverride override = getOverride(species);
        if (override != null) {
            if (override.minimumRarity != null && rarity.ordinal() < override.minimumRarity.ordinal()) return 0.0;
            if (override.maximumRarity != null && rarity.ordinal() > override.maximumRarity.ordinal()) return 0.0;
            Double explicit = override.weights.get(rarity);
            if (explicit != null) return explicit;
        }

        SpeciesTier tier = override != null && override.tier != null
                ? override.tier
                : getTier(species);
        return TIER_WEIGHTS.get(tier).getOrDefault(rarity, 0.0);
    }

    private static SpeciesTier getTier(Species species) {
        String key = normalizeId(species.getName());
        return TIER_CACHE.computeIfAbsent(key, ignored -> classify(species));
    }

    private static Species resolveSpecies(String pokemonId) {
        String key = normalizeId(pokemonId);
        Species cached = SPECIES_CACHE.get(key);
        if (cached != null) return cached;

        Species resolved = CardUtil.getSpecies(pokemonId);
        if (resolved != null) SPECIES_CACHE.put(key, resolved);
        return resolved;
    }

    private static SpeciesTier classify(Species species) {
        String speciesId = normalizeId(species.getName());
        Set<String> labels = species.getLabels().stream()
                .map(SpeciesRarityManager::normalizeLabel)
                .collect(java.util.stream.Collectors.toSet());

        if (hasAnyLabel(labels, "legendary", "mythical", "restricted", "sub_legendary")) {
            return SpeciesTier.LEGENDARY;
        }
        int baseStatTotal = species.getBaseStats().values().stream().mapToInt(Integer::intValue).sum();

        if (hasAnyLabel(labels, "ultra_beast", "ultrabeast", "paradox", "powerhouse")
                || baseStatTotal >= 580) {
            return SpeciesTier.EPIC;
        }

        if (FORCED_COMMON.contains(speciesId)) return SpeciesTier.COMMON;
        if (FORCED_UNCOMMON.contains(speciesId)) return SpeciesTier.UNCOMMON;
        if (FORCED_RARE.contains(speciesId)) return SpeciesTier.RARE;

        boolean hasPreEvolution = species.getPreEvolution() != null;
        boolean canEvolve = species.getEvolutions() != null && !species.getEvolutions().isEmpty();

        // Evolution stage is the primary collectability signal. Unevolved species remain
        // common unless their actual battle strength is exceptional; capture difficulty
        // alone must not make mundane Pokemon feel like Rare cards.
        if (!hasPreEvolution && canEvolve) {
            if (baseStatTotal >= 530) return SpeciesTier.RARE;
            if (baseStatTotal >= 450) return SpeciesTier.UNCOMMON;
            return SpeciesTier.COMMON;
        }

        // Middle evolutions are normally uncommon, with unusually powerful/scarce ones rare.
        if (hasPreEvolution && canEvolve) {
            if (baseStatTotal >= 530) return SpeciesTier.RARE;
            return SpeciesTier.UNCOMMON;
        }

        // Most final evolutions are natural rare pulls.
        if (hasPreEvolution) {
            if (baseStatTotal >= 530) return SpeciesTier.RARE;
            return SpeciesTier.UNCOMMON;
        }

        // Standalone species use battle strength as their final signal.
        if (baseStatTotal >= 530) return SpeciesTier.RARE;
        if (baseStatTotal >= 400) return SpeciesTier.UNCOMMON;
        return SpeciesTier.COMMON;
    }

    private static boolean hasAnyLabel(Set<String> labels, String... candidates) {
        for (String candidate : candidates) {
            if (labels.contains(candidate)) return true;
        }
        return false;
    }

    private static SpeciesOverride getOverride(Species species) {
        SpeciesOverride override = OVERRIDES.get(normalizeId(species.getName()));
        if (override == null && species.getResourceIdentifier() != null) {
            override = OVERRIDES.get(normalizeId(species.getResourceIdentifier().getPath()));
        }
        return override;
    }

    private static void resetDefaults() {
        enabled = true;
        applyToCaptureDrops = true;
        OVERRIDES.clear();
        TIER_CACHE.clear();
        SPECIES_CACHE.clear();
        TIER_WEIGHTS.clear();

        // These values account for the fact that the official roster contains far
        // more ordinary final evolutions than legendary or powerhouse species.
        putTierWeights(SpeciesTier.COMMON, 200, 10, 0, 0, 0, 0);
        putTierWeights(SpeciesTier.UNCOMMON, 8, 200, 8, 0, 0, 0);
        putTierWeights(SpeciesTier.RARE, 0, 8, 200, 8, 1, 0.5);
        putTierWeights(SpeciesTier.EPIC, 0, 0, 8, 200, 15, 5);
        putTierWeights(SpeciesTier.LEGENDARY, 0, 0, 0, 10, 250, 300);
    }

    private static void putTierWeights(
            SpeciesTier tier,
            double common,
            double uncommon,
            double rare,
            double epic,
            double legendary,
            double mythic
    ) {
        EnumMap<CardRarity, Double> weights = new EnumMap<>(CardRarity.class);
        weights.put(CardRarity.COMMON, common);
        weights.put(CardRarity.UNCOMMON, uncommon);
        weights.put(CardRarity.RARE, rare);
        weights.put(CardRarity.EPIC, epic);
        weights.put(CardRarity.LEGENDARY, legendary);
        weights.put(CardRarity.MYTHIC, mythic);
        TIER_WEIGHTS.put(tier, weights);
    }

    private static void loadConfig() throws IOException {
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
            JsonElement rootElement = JsonParser.parseReader(reader);
            if (!rootElement.isJsonObject()) throw new IOException("Root must be a JSON object");
            JsonObject root = rootElement.getAsJsonObject();

            enabled = readBoolean(root, "enabled", enabled);
            applyToCaptureDrops = readBoolean(root, "applyToCaptureDrops", applyToCaptureDrops);

            JsonObject tierWeights = getObject(root, "tierWeights");
            if (tierWeights != null) {
                for (SpeciesTier tier : SpeciesTier.values()) {
                    JsonObject configuredTier = getObject(tierWeights, tier.serializedName);
                    if (configuredTier == null) continue;
                    for (CardRarity rarity : CardRarity.values()) {
                        Double value = readNonNegativeDouble(configuredTier, rarity.serializedName);
                        if (value != null) TIER_WEIGHTS.get(tier).put(rarity, value);
                    }
                }
            }

            JsonObject overrides = getObject(root, "overrides");
            if (overrides != null) {
                for (Map.Entry<String, JsonElement> entry : overrides.entrySet()) {
                    if (!entry.getValue().isJsonObject()) continue;
                    SpeciesOverride override = parseOverride(entry.getValue().getAsJsonObject());
                    OVERRIDES.put(normalizeId(entry.getKey()), override);
                }
            }
        }
    }

    private static void migrateConfigIfNeeded() throws IOException {
        JsonObject existing;
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
            JsonElement parsed = JsonParser.parseReader(reader);
            if (!parsed.isJsonObject()) return;
            existing = parsed.getAsJsonObject();
        }

        int version = 1;
        try {
            if (existing.has("version")) version = existing.get("version").getAsInt();
        } catch (Exception ignored) {
        }
        if (version >= CONFIG_VERSION) return;

        Path backup = CONFIG_PATH.resolveSibling(CONFIG_PATH.getFileName() + ".v" + version + ".backup");
        Files.copy(CONFIG_PATH, backup, StandardCopyOption.REPLACE_EXISTING);

        JsonObject migrated = createDefaultConfig();
        if (existing.has("enabled")) migrated.add("enabled", existing.get("enabled").deepCopy());
        if (existing.has("applyToCaptureDrops")) {
            migrated.add("applyToCaptureDrops", existing.get("applyToCaptureDrops").deepCopy());
        }
        if (existing.has("overrides") && existing.get("overrides").isJsonObject()) {
            migrated.add("overrides", existing.get("overrides").deepCopy());
        }

        Files.writeString(CONFIG_PATH, GSON.toJson(migrated), StandardCharsets.UTF_8,
                StandardOpenOption.TRUNCATE_EXISTING);
        CobblemonCards.LOGGER.info(
                "Migrated species rarity config from version {} to {}. Previous file backed up to {}",
                version, CONFIG_VERSION, backup);
    }

    private static SpeciesOverride parseOverride(JsonObject object) {
        SpeciesTier tier = object.has("tier")
                ? SpeciesTier.fromName(object.get("tier").getAsString())
                : null;
        CardRarity minimum = object.has("minimumRarity")
                ? CardRarity.fromName(object.get("minimumRarity").getAsString())
                : null;
        CardRarity maximum = object.has("maximumRarity")
                ? CardRarity.fromName(object.get("maximumRarity").getAsString())
                : null;

        EnumMap<CardRarity, Double> weights = new EnumMap<>(CardRarity.class);
        JsonObject weightObject = getObject(object, "weights");
        if (weightObject != null) {
            for (CardRarity rarity : CardRarity.values()) {
                Double value = readNonNegativeDouble(weightObject, rarity.serializedName);
                if (value != null) weights.put(rarity, value);
            }
        }
        return new SpeciesOverride(tier, minimum, maximum, weights);
    }

    private static JsonObject createDefaultConfig() {
        JsonObject root = new JsonObject();
        root.addProperty("version", CONFIG_VERSION);
        root.addProperty("enabled", true);
        root.addProperty("applyToCaptureDrops", true);

        JsonArray notes = new JsonArray();
        notes.add("Tier weights are relative selection weights. Use 0 to prohibit a combination.");
        notes.add("Override keys are Pokemon IDs. Each override may define tier, minimumRarity, maximumRarity, and/or weights.");
        notes.add("Instant Dex and administrator-created cards intentionally bypass these restrictions.");
        root.add("notes", notes);

        JsonObject tiers = new JsonObject();
        for (SpeciesTier tier : SpeciesTier.values()) {
            JsonObject weights = new JsonObject();
            for (Map.Entry<CardRarity, Double> entry : TIER_WEIGHTS.get(tier).entrySet()) {
                weights.addProperty(entry.getKey().serializedName, entry.getValue());
            }
            tiers.add(tier.serializedName, weights);
        }
        root.add("tierWeights", tiers);
        root.add("overrides", new JsonObject());
        return root;
    }

    private static JsonObject getObject(JsonObject parent, String name) {
        if (parent == null || !parent.has(name) || !parent.get(name).isJsonObject()) return null;
        return parent.getAsJsonObject(name);
    }

    private static boolean readBoolean(JsonObject object, String name, boolean fallback) {
        try {
            return object.has(name) ? object.get(name).getAsBoolean() : fallback;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static Double readNonNegativeDouble(JsonObject object, String name) {
        try {
            if (!object.has(name)) return null;
            double value = object.get(name).getAsDouble();
            return Double.isFinite(value) && value >= 0.0 ? value : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String normalizeId(String value) {
        if (value == null) return "";
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private static String normalizeLabel(String value) {
        if (value == null) return "";
        return value.toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
    }

    private static Set<String> idSet(String commaSeparatedIds) {
        return java.util.Arrays.stream(commaSeparatedIds.split(","))
                .map(SpeciesRarityManager::normalizeId)
                .filter(id -> !id.isBlank())
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    private enum SpeciesTier {
        COMMON("common"),
        UNCOMMON("uncommon"),
        RARE("rare"),
        EPIC("epic"),
        LEGENDARY("legendary");

        private final String serializedName;

        SpeciesTier(String serializedName) {
            this.serializedName = serializedName;
        }

        private static SpeciesTier fromName(String value) {
            if (value == null) return null;
            for (SpeciesTier tier : values()) {
                if (tier.serializedName.equalsIgnoreCase(value)) return tier;
            }
            return null;
        }
    }

    private enum CardRarity {
        COMMON("common"),
        UNCOMMON("uncommon"),
        RARE("rare"),
        EPIC("epic"),
        LEGENDARY("legendary"),
        MYTHIC("mythic");

        private final String serializedName;

        CardRarity(String serializedName) {
            this.serializedName = serializedName;
        }

        private static CardRarity fromName(String value) {
            if (value == null) return null;
            for (CardRarity rarity : values()) {
                if (rarity.serializedName.equalsIgnoreCase(value)) return rarity;
            }
            return null;
        }
    }

    private record SpeciesOverride(
            SpeciesTier tier,
            CardRarity minimumRarity,
            CardRarity maximumRarity,
            EnumMap<CardRarity, Double> weights
    ) {
    }

    private record WeightedSpecies(String pokemonId, double cumulativeWeight) {
    }
}
