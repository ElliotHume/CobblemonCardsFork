package com.howlite.cobblemoncards.event;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnDetail;
import com.cobblemon.mod.common.api.spawning.detail.SpawnAction;
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail;
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence;
import com.cobblemon.mod.common.api.spawning.multiplier.WeightMultiplier;
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.api.types.ElementalTypes;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Species;
import com.howlite.cobblemoncards.CobblemonCardsConfig;
import com.howlite.cobblemoncards.component.CardData;
import com.howlite.cobblemoncards.component.CardStat;
import com.howlite.cobblemoncards.component.ModDataComponents;
import com.howlite.cobblemoncards.item.custom.BinderItem;
import com.howlite.cobblemoncards.util.CardStatUtil;
import com.howlite.cobblemoncards.util.CardUtil;
import com.howlite.cobblemoncards.util.EquippedAccessory;
import com.howlite.cobblemoncards.util.PlatformHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Spawning influence applied to every player-driven Cobblemon spawner.
 *
 * <p>Instead of forcefully rewriting an already spawned {@code PokemonEntity} (the old
 * {@code onEntityLoad} approach), this hooks into Cobblemon's weighting pipeline: spawn details
 * whose species matches an elemental type boosted by the player's equipped binder simply become
 * more likely to be selected.</p>
 *
 * <p>Registered through {@code PlayerSpawnerFactory.INSTANCE.getInfluenceBuilders()}.</p>
 */
public class BinderSpawnModifier implements SpawningInfluence {
    public static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-cards");

    /** Temporary testing switch: verbose console logging for the spawn influence. */
    public static final boolean DEBUG = false;
    /** One-shot flag so we log the very first time Cobblemon asks us for a weight. */
    private static volatile boolean FIRST_CALL_LOGGED = false;

    /** How often (in ticks) the equipped binder is re-scanned. */
    private static final long REFRESH_INTERVAL_TICKS = 40L;
    /** How often (in ticks) the debug activity summary is printed. */
    private static final long DEBUG_SUMMARY_INTERVAL_TICKS = 200L;
    /** Max number of distinct boosted species logged individually per summary window. */
    private static final int DEBUG_SPECIES_LOG_LIMIT = 10;

    private final ServerPlayer player;

    /** Cached per-type weight multipliers derived from the binder contents. */
    private final Map<ElementalType, Float> typeMultipliers = new HashMap<>();
    private long lastRefreshTick = Long.MIN_VALUE;

    // --- debug bookkeeping ---
    private boolean firstScan = true;
    private long lastSummaryTick = Long.MIN_VALUE;
    private int debugEvaluated = 0;
    private int debugBoosted = 0;
    private float debugBestMultiplier = 1.0f;
    private String debugBestSpecies = null;
    private int debugUnresolved = 0;
    private String debugUnresolvedSample = null;
    private final Map<String, Float> debugBoostedSpecies = new LinkedHashMap<>();

    public BinderSpawnModifier(@NotNull ServerPlayer player) {
        this.player = player;
        if (DEBUG) {
            LOGGER.info("[BinderSpawn] SpawningInfluence attached to player {}", player.getName().getString());
        }
    }

    @Override
    public float affectWeight(@NotNull SpawnDetail detail, @NotNull SpawnablePosition spawnablePosition, float weight) {
        if (DEBUG && !FIRST_CALL_LOGGED) {
            FIRST_CALL_LOGGED = true;
            LOGGER.info("[BinderSpawn] affectWeight() is being called by Cobblemon - the influence IS hooked up (player {})",
                    player.getName().getString());
        }
        if (!CobblemonCardsConfig.enableCardStats || !CobblemonCardsConfig.enableSpawnBoostStats) {
            return weight;
        }
        if (!(detail instanceof PokemonSpawnDetail pokemonDetail)) {
            return weight;
        }

        refreshIfNeeded();

        float multiplier = 1.0f;
        Species species = typeMultipliers.isEmpty() ? null : resolveSpecies(pokemonDetail);
        if (species != null) {
            Float primary = typeMultipliers.get(species.getPrimaryType());
            if (primary != null) {
                multiplier = Math.max(multiplier, primary);
            }
            ElementalType secondaryType = species.getSecondaryType();
            if (secondaryType != null) {
                Float secondary = typeMultipliers.get(secondaryType);
                if (secondary != null) {
                    multiplier = Math.max(multiplier, secondary);
                }
            }
        }

        if (DEBUG) {
            debugEvaluated++;
            if (multiplier > 1.0f) {
                debugBoosted++;
                String speciesName = species.getName();
                // Log each distinct boosted species once per summary window, capped at 10 per window.
                if (!debugBoostedSpecies.containsKey(speciesName) && debugBoostedSpecies.size() < DEBUG_SPECIES_LOG_LIMIT) {
                    debugBoostedSpecies.put(speciesName, multiplier);
                    LOGGER.info("[BinderSpawn] BOOSTED {} ({}{}) weight {} -> {} (x{}) [{}/{} species logged this window]",
                            speciesName,
                            species.getPrimaryType().getName(),
                            species.getSecondaryType() == null ? "" : "/" + species.getSecondaryType().getName(),
                            String.format(Locale.ROOT, "%.3f", weight),
                            String.format(Locale.ROOT, "%.3f", weight * multiplier),
                            String.format(Locale.ROOT, "%.3f", multiplier),
                            debugBoostedSpecies.size(),
                            DEBUG_SPECIES_LOG_LIMIT);
                }
                if (multiplier > debugBestMultiplier) {
                    debugBestMultiplier = multiplier;
                    debugBestSpecies = speciesName;
                }
            } else if (species == null && !typeMultipliers.isEmpty()) {
                debugUnresolved++;
                if (debugUnresolvedSample == null) {
                    PokemonProperties props = pokemonDetail.getPokemon();
                    debugUnresolvedSample = props == null ? "<null properties>" : String.valueOf(props.getSpecies());
                }
            }
            logSummaryIfDue();
        }

        return multiplier == 1.0f ? weight : weight * multiplier;
    }

    @Override
    public void affectSpawn(@NotNull SpawnAction<?> action, @NotNull Entity entity) {
        if (!DEBUG) {
            return;
        }
        if (!(entity instanceof PokemonEntity pokemonEntity)) {
            return;
        }

        Species species = pokemonEntity.getPokemon().getSpecies();
        ElementalType primaryType = species.getPrimaryType();
        ElementalType secondaryType = species.getSecondaryType();

        Float primary = typeMultipliers.get(primaryType);
        Float secondary = secondaryType == null ? null : typeMultipliers.get(secondaryType);
        boolean boosted = primary != null || secondary != null;

        LOGGER.info("[BinderSpawn] {} -> spawned {} ({}{}) | binder-boosted: {}",
                player.getName().getString(),
                species.getName(),
                primaryType.getName(),
                secondaryType == null ? "" : "/" + secondaryType.getName(),
                boosted ? "YES x" + String.format(Locale.ROOT, "%.2f", Math.max(primary == null ? 1f : primary, secondary == null ? 1f : secondary)) : "no");
    }

    @Override
    public boolean isExpired() {
        boolean expired = player.isRemoved();
        if (expired && DEBUG) {
            LOGGER.info("[BinderSpawn] Influence expired for player {}", player.getName().getString());
        }
        return expired;
    }

    // ------------------------------------------------------------------
    // Binder scanning
    // ------------------------------------------------------------------

    private void refreshIfNeeded() {
        long now = player.level().getGameTime();
        if (lastRefreshTick != Long.MIN_VALUE && now - lastRefreshTick < REFRESH_INTERVAL_TICKS) {
            return;
        }
        lastRefreshTick = now;
        recomputeMultipliers();
    }

    private void recomputeMultipliers() {
        Map<ElementalType, Float> previous = new HashMap<>(typeMultipliers);

        typeMultipliers.clear();

        Map<CardStat, Float> spawnStats = new EnumMap<>(CardStat.class);
        int binderCount = 0;
        StringJoiner slots = new StringJoiner(", ");
        for (EquippedAccessory equipped : PlatformHelper.INSTANCE.getEquippedAccessories(player)) {
            String slotId = equipped.slotName();
            slots.add(slotId + "=" + equipped.stack().getItem());
            if (!(slotId.equals("binder") || slotId.equals("legs/binder"))) {
                continue;
            }
            if (!(equipped.stack().getItem() instanceof BinderItem)) {
                continue;
            }
            binderCount++;
            collectSpawnStats(equipped.stack(), spawnStats);
        }

        if (DEBUG && firstScan) {
            LOGGER.info("[BinderSpawn] First binder scan for {}: accessories=[{}], binders found={}, spawn stats={}",
                    player.getName().getString(), slots, binderCount, spawnStats);
        }

        for (Map.Entry<CardStat, Float> entry : spawnStats.entrySet()) {
            CardStat stat = entry.getKey();
            ElementalType type = getElementalType(stat);
            if (type == null) {
                continue;
            }

            // Single source of truth for stat -> percentage (config multipliers applied exactly once).
            float percent = CardStatUtil.getEffectiveValue(stat, entry.getValue());
            if (percent <= 0f) {
                // Never reduce weights: a non-positive bonus simply means "no boost for this type".
                continue;
            }

            // percent is a *bonus* in percent, so 0 -> x1.0 and e.g. 25 -> x1.25 (always >= 1.0).
            float multiplier = Math.min(CobblemonCardsConfig.maxSpawnBoostMultiplier, 1.0f + (percent / 100.0f));
            typeMultipliers.merge(type, multiplier, Math::max);
        }

        if (DEBUG && (firstScan || !previous.equals(typeMultipliers))) {
            if (typeMultipliers.isEmpty()) {
                LOGGER.info("[BinderSpawn] {} has no active binder spawn boosts", player.getName().getString());
            } else {
                StringJoiner joiner = new StringJoiner(", ");
                typeMultipliers.forEach((type, mult) ->
                        joiner.add(type.getName() + " x" + String.format(Locale.ROOT, "%.3f", mult)
                                + " (+" + String.format(Locale.ROOT, "%.1f", (mult - 1.0f) * 100.0f) + "%)"));
                LOGGER.info("[BinderSpawn] {} binder boosts updated: {}", player.getName().getString(), joiner);
            }
        }
        firstScan = false;
    }

    private void logSummaryIfDue() {
        long now = player.level().getGameTime();
        if (lastSummaryTick == Long.MIN_VALUE) {
            lastSummaryTick = now;
            return;
        }
        if (now - lastSummaryTick < DEBUG_SUMMARY_INTERVAL_TICKS) {
            return;
        }
        lastSummaryTick = now;

        if (debugBoosted > 0) {
            StringJoiner boostedList = new StringJoiner(", ");
            debugBoostedSpecies.forEach((name, mult) ->
                    boostedList.add(name + " x" + String.format(Locale.ROOT, "%.2f", mult)));
            LOGGER.info("[BinderSpawn] {} weighting: {} spawn entries checked, {} boosted (best: {} x{}) | boosted species: {}",
                    player.getName().getString(),
                    debugEvaluated,
                    debugBoosted,
                    debugBestSpecies,
                    String.format(Locale.ROOT, "%.3f", debugBestMultiplier),
                    boostedList);
        } else if (debugEvaluated > 0) {
            LOGGER.info("[BinderSpawn] {} weighting: {} spawn entries checked, none matched a boosted type (active boosts: {})",
                    player.getName().getString(), debugEvaluated, typeMultipliers.isEmpty() ? "NONE" : typeMultipliers.keySet());
        }

        if (debugUnresolved > 0) {
            LOGGER.info("[BinderSpawn] {} could not resolve a Species for {} spawn entries (e.g. \"{}\")",
                    player.getName().getString(), debugUnresolved, debugUnresolvedSample);
        }

        debugEvaluated = 0;
        debugBoosted = 0;
        debugBestMultiplier = 1.0f;
        debugBestSpecies = null;
        debugUnresolved = 0;
        debugUnresolvedSample = null;
        debugBoostedSpecies.clear();
    }

    private static void collectSpawnStats(ItemStack binderStack, Map<CardStat, Float> out) {
        List<ItemStack> binderItems = binderStack.get(ModDataComponents.BINDER_CONTENTS);
        Iterable<ItemStack> contentItems;
        if (binderItems != null) {
            contentItems = binderItems.stream().filter(s -> !s.isEmpty()).toList();
        } else {
            contentItems = binderStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).nonEmptyItems();
        }

        for (ItemStack contentStack : contentItems) {
            CardData cardData = contentStack.get(ModDataComponents.CARD_DATA);
            if (cardData == null || CardUtil.isCosmeticCard(cardData.pokemonId())) {
                continue;
            }
            if (!CobblemonCardsConfig.isSpawnStat(cardData.stat())) {
                continue;
            }
            out.merge(cardData.stat(), cardData.statValue(), Float::sum);
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static Species resolveSpecies(PokemonSpawnDetail detail) {
        PokemonProperties properties = detail.getPokemon();
        if (properties == null) {
            return null;
        }
        String name = properties.getSpecies();
        if (name == null || name.isEmpty()) {
            return null;
        }
        if (name.indexOf(':') >= 0) {
            ResourceLocation id = ResourceLocation.tryParse(name);
            return id == null ? null : PokemonSpecies.getByIdentifier(id);
        }
        return PokemonSpecies.getByName(name.toLowerCase(Locale.ROOT));
    }

    private static ElementalType getElementalType(CardStat stat) {
        String typeName = stat.getSerializedName().replace("_spawn", "");
        return ElementalTypes.get(typeName);
    }
}
