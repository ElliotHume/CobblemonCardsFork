package com.howlite.cobblemoncards.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.howlite.cobblemoncards.CobblemonCards;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;

import java.io.Reader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Reads all {@code data/cobblemon-cards/fakemon_cards/*.json} datapack files
 * and feeds the collected species IDs into {@link FakemonCardRegistry}.
 *
 * <p>Called by platform-specific reload listeners (Fabric and NeoForge) whenever
 * server datapacks are loaded or reloaded ({@code /reload}).
 *
 * <h3>JSON format</h3>
 * <pre>{@code
 * {
 *   "replace": false,
 *   "species": ["myfakemon", "anotherfakemon"]
 * }
 * }</pre>
 * When {@code replace} is {@code true}, entries from lower-priority packs are
 * cleared before adding entries from this file (same semantics as vanilla recipes).
 *
 * <h3>Resource-pack sprites</h3>
 * Place card textures at:
 * <pre>
 *   assets/cobblemon-cards/textures/item/cards/pokemon/regular/{species_name}.png  (40x30)
 *   assets/cobblemon-cards/textures/item/cards/pokemon/shiny/{species_name}.png    (40x30, optional)
 * </pre>
 * The card renderer already checks these paths automatically.
 */
public final class FakemonWhitelistReloader {

    static final String FOLDER = "fakemon_cards";

    private FakemonWhitelistReloader() {}

    /**
     * Scans {@code data/cobblemon-cards/fakemon_cards/*.json} in the given
     * {@link ResourceManager} and rebuilds {@link FakemonCardRegistry}.
     */
    public static void loadFrom(ResourceManager resourceManager) {
        Set<String> collected = new HashSet<>();

        Map<ResourceLocation, Resource> resources = resourceManager.listResources(
                FOLDER, loc -> loc.getPath().endsWith(".json"));

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation loc = entry.getKey();
            if (!loc.getNamespace().equals(CobblemonCards.MOD_ID)) continue;

            try (Reader reader = entry.getValue().openAsReader()) {
                JsonObject json = GsonHelper.parse(reader);

                boolean replace = json.has("replace") && json.get("replace").getAsBoolean();
                if (replace) {
                    CobblemonCards.LOGGER.info(
                            "[CobblemonCards] Fakemon whitelist: '{}' has replace=true, clearing previous entries.", loc);
                    collected.clear();
                }

                JsonArray speciesArr = GsonHelper.getAsJsonArray(json, "species", new JsonArray());
                int before = collected.size();
                for (JsonElement el : speciesArr) {
                    String id = el.getAsString().trim().toLowerCase();
                    if (!id.isEmpty()) {
                        collected.add(id);
                    }
                }
                CobblemonCards.LOGGER.debug(
                        "[CobblemonCards] Fakemon whitelist: loaded {} new entries from '{}'.",
                        collected.size() - before, loc);

            } catch (Exception e) {
                CobblemonCards.LOGGER.warn(
                        "[CobblemonCards] Failed to parse fakemon whitelist '{}': {}", loc, e.getMessage());
            }
        }

        FakemonCardRegistry.reload(collected);
    }
}