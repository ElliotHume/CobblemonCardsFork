package com.howlite.cobblemoncards.screen;

import com.howlite.cobblemoncards.component.CardData;
import com.howlite.cobblemoncards.component.CardStat;
import com.howlite.cobblemoncards.component.ModDataComponents;
import com.howlite.cobblemoncards.item.ModItems;
import com.howlite.cobblemoncards.item.custom.loot.BoosterLootTable;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.pokemon.Species;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class CardDexScreen extends Screen {

    private static final ResourceLocation CARDDEX_BG = ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "textures/gui/carddex/carddex_bg.png");
    private static final ResourceLocation CARDDEX_BTN = ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "textures/gui/carddex/carddex_button.png");
    private static final ResourceLocation CARDDEX_BTN_HIGHLIGHTED = ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "textures/gui/carddex/carddex_button_highlighted.png");

    private final List<String> discoveredIds;
    private int selectedTab = 1; // 1-9: Générations, 10: Dex National, 11: Régionaux, 12: Mégas
    private final List<String> genPokemon = new ArrayList<>();
    private final Map<String, ItemStack> cardCache = new HashMap<>();

    private double scrollAmount = 0;
    private int maxScroll = 0;
    private boolean isDraggingScrollbar = false;

    private int cardWidth = 34;
    private int cardHeight = 48;

    private int albumW = 278;
    private int albumH = 198;
    private int albumX;
    private int albumY;

    public CardDexScreen(List<String> discoveredIds) {
        super(Component.translatable("screen.cobblemon-cards.card_dex.title"));
        this.discoveredIds = new ArrayList<>();
        for (String id : discoveredIds) {
            this.discoveredIds.add(id.toLowerCase());
        }
    }

    @Override
    protected void init() {
        super.init();

        this.albumX = (this.width - this.albumW) / 2;
        this.albumY = (this.height - this.albumH) / 2;

        loadGeneration(selectedTab);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        super.render(graphics, mouseX, mouseY, delta);

        // Draw bookmarks (tabs) hanging from the bottom of the book cover (aligned with new 198px height)
        // spaced by 4 pixels and perfectly centered: 12 * 16 + 11 * 4 = 236px total width
        int startX = this.albumX + (278 - 236) / 2;
        int hoveredTab = -1;
        for (int i = 0; i < 12; i++) {
            int x = startX + i * 20; // 16px width + 4px gap
            boolean isSelected = (selectedTab == i + 1);
            boolean isHovered = mouseX >= x && mouseX < x + 16 && mouseY >= this.albumY + 192 && mouseY <= this.albumY + 228;

            int y = this.albumY + 192;
            ResourceLocation btnTexture = CARDDEX_BTN;
            if (isSelected || isHovered) {
                y = this.albumY + 196; // Shift tab DOWN when active/hovered to hang lower!
                btnTexture = CARDDEX_BTN_HIGHLIGHTED;
            }

            // Draw bookmark (swallowtail notch points downwards)
            graphics.blit(btnTexture, x, y, 0, 0, 16, 32, 16, 32);

            // Draw short label on the bookmark
            String labelStr;
            if (i < 9) {
                labelStr = String.valueOf(i + 1);
            } else if (i == 9) {
                labelStr = "N";
            } else if (i == 10) {
                labelStr = "R";
            } else {
                labelStr = "M";
            }

            int labelCol = isSelected ? 0xFFFFAA00 : 0x3F3F3F; // Gold if active, dark gray if inactive
            // Centered perfectly on Y: locks at albumY + 206 for a straight, clean alignment across all tabs
            int textY = (isSelected || isHovered) ? (y + 6) : (y + 14);
            graphics.drawCenteredString(this.font, labelStr, x + 8, textY, labelCol);

            if (isHovered) {
                hoveredTab = i;
            }
        }

        // Draw book background on top of the bookmarks upper overlap
        graphics.blit(CARDDEX_BG, this.albumX, this.albumY, 0, 0, this.albumW, this.albumH, this.albumW, this.albumH);

        // Draw Page Title (sepia-colored) on the left side
        String titleStr = getTabTitle(selectedTab);
        graphics.drawString(this.font, titleStr, this.albumX + 22, this.albumY + 14, 0x3F3F3F, false);

        // Draw Page Progression (sepia-colored) on the right side
        int discoveredCount = 0;
        for (String pokemonId : genPokemon) {
            if (discoveredIds.contains(pokemonId.toLowerCase())) {
                discoveredCount++;
            }
        }
        int totalCount = genPokemon.size();
        int percent = totalCount > 0 ? (discoveredCount * 100 / totalCount) : 0;
        String progressCountStr = discoveredCount + "/" + totalCount + " (" + percent + "%)";
        int progressW = this.font.width(progressCountStr);
        graphics.drawString(this.font, progressCountStr, this.albumX + 256 - progressW, this.albumY + 14, 0x3F3F3F, false);

        // Thin sepia line under headers
        graphics.fill(this.albumX + 22, this.albumY + 24, this.albumX + 256, this.albumY + 25, 0x223F3F3F);

        // Draw cards in single continuous scrollable panel
        Lighting.setupForFlatItems();
        String hoveredPoke = null;

        int gridStartX = this.albumX + 22; // Centered to stay inside the paper page boundaries
        int spacingX = 12;
        int cardW = 34;
        int cardH = 48;
        int spacingY = 22; // Increased vertical spacing for high readability and name breathing room

        // Scissor boundary to prevent cards clipping outside book paper page limits (bottom reduced by 5px to 172px)
        graphics.enableScissor(this.albumX + 16, this.albumY + 26, this.albumX + 246, this.albumY + 172);

        graphics.pose().pushPose();
        for (int index = 0; index < genPokemon.size(); index++) {
            String pokemonId = genPokemon.get(index);
            int col = index % 5;
            int row = index / 5;

            int x = gridStartX + col * (cardW + spacingX);
            // Starting Y shifted down to albumY + 40 to add breathing space at the top of the scroll list
            int y = this.albumY + 40 + row * (cardH + spacingY) - (int) scrollAmount;

            if (y + cardH + 12 >= this.albumY + 26 && y <= this.albumY + 172) {
                ItemStack cardStack = cardCache.get(pokemonId);
                if (cardStack != null) {
                    graphics.pose().pushPose();
                    graphics.pose().translate(x + cardW / 2.0f, y + cardH / 2.0f, 150);

                    boolean hovered = mouseX >= x && mouseX <= x + cardW && mouseY >= y && mouseY <= y + cardH;
                    if (hovered && mouseY >= this.albumY + 26 && mouseY <= this.albumY + 172) {
                        hoveredPoke = pokemonId;
                        graphics.pose().mulPose(Axis.YP.rotationDegrees((float) Math.sin(System.currentTimeMillis() * 0.005) * 10f));
                        graphics.pose().mulPose(Axis.XP.rotationDegrees((float) Math.cos(System.currentTimeMillis() * 0.005) * 8f));
                    }

                    float scale = 52.0f;
                    graphics.pose().scale(scale, -scale, scale);

                    Minecraft.getInstance().getItemRenderer().renderStatic(
                            cardStack,
                            ItemDisplayContext.GUI,
                            LightTexture.FULL_BRIGHT,
                            OverlayTexture.NO_OVERLAY,
                            graphics.pose(),
                            graphics.bufferSource(),
                            Minecraft.getInstance().level,
                            0);

                    graphics.pose().popPose();

                    // Card local name
                    String name = "???";
                    if (discoveredIds.contains(pokemonId.toLowerCase())) {
                        String localized = net.minecraft.client.resources.language.I18n.get("cobblemon.species." + pokemonId);
                        if (localized.startsWith("cobblemon.species.")) {
                            if (pokemonId.contains("_")) {
                                String[] parts = pokemonId.split("_");
                                String base = parts[0].substring(0, 1).toUpperCase() + parts[0].substring(1);
                                String suffix = parts[1].substring(0, 1).toUpperCase() + parts[1].substring(1);
                                if (parts.length > 2) {
                                    suffix += " " + parts[2].toUpperCase();
                                }
                                localized = base + " (" + suffix + ")";
                            } else {
                                localized = pokemonId.substring(0, 1).toUpperCase() + pokemonId.substring(1);
                            }
                        }
                        name = localized;
                    }
                    name = truncateToWidth(name, cardW + spacingX - 2);

                    // Highly contrastive pure black (0xFF000000) for discovered, clean gray (0xFF7A7A7A) for undiscovered
                    int nameColor = discoveredIds.contains(pokemonId.toLowerCase()) ? 0xFF000000 : 0xFF7A7A7A;
                    // Draw centered string manually without shadow (drawShadow = false) to prevent fat/blurry text!
                    int nameWidth = this.font.width(name);
                    graphics.drawString(this.font, name, x + cardW / 2 - nameWidth / 2, y + cardH + 5, nameColor, false);
                }
            }
        }
        graphics.flush();
        graphics.pose().popPose();

        graphics.disableScissor();

        // Draw Scrollbar (wood-themed, shifted left slightly to be fully inside paper, height is 144px)
        if (maxScroll > 0) {
            int scrollbarX = this.albumX + 248;
            int scrollbarY = this.albumY + 28;
            int scrollbarHeight = 144;

            // Track
            graphics.fill(scrollbarX, scrollbarY, scrollbarX + 4, scrollbarY + scrollbarHeight, 0x1A3F3F3F);

            // Thumb
            int thumbHeight = Math.max(12, scrollbarHeight * scrollbarHeight / Math.max(1, scrollbarHeight + maxScroll));
            int thumbY = scrollbarY + (int) ((scrollbarHeight - thumbHeight) * scrollAmount / Math.max(1, maxScroll));
            graphics.fill(scrollbarX, thumbY, scrollbarX + 4, thumbY + thumbHeight, 0x5F3F3F3F);
            graphics.fill(scrollbarX + 1, thumbY + 1, scrollbarX + 3, thumbY + thumbHeight - 1, 0x8F3F3F3F);
        }

        // Draw hovered card details tooltips
        if (hoveredPoke != null) {
            boolean discovered = discoveredIds.contains(hoveredPoke.toLowerCase());
            List<Component> tooltip = new ArrayList<>();

            int dexNum = getPokedexNumberOf(hoveredPoke);
            String formattedNum = dexNum > 0 && dexNum < 9999 ? String.format("#%03d - ", dexNum) : "";

            if (discovered) {
                String localized = net.minecraft.client.resources.language.I18n.get("cobblemon.species." + hoveredPoke);
                if (localized.startsWith("cobblemon.species.")) {
                    if (hoveredPoke.contains("_")) {
                        String[] parts = hoveredPoke.split("_");
                        String base = parts[0].substring(0, 1).toUpperCase() + parts[0].substring(1);
                        String suffix = parts[1].substring(0, 1).toUpperCase() + parts[1].substring(1);
                        if (parts.length > 2) {
                            suffix += " " + parts[2].toUpperCase();
                        }
                        localized = base + " (" + suffix + ")";
                    } else {
                        localized = hoveredPoke.substring(0, 1).toUpperCase() + hoveredPoke.substring(1);
                    }
                }
                tooltip.add(Component.literal(formattedNum + localized).withStyle(net.minecraft.ChatFormatting.GOLD));
                tooltip.add(Component.translatable("gui.cobblemon-cards.card_dex.discovered_status").withStyle(net.minecraft.ChatFormatting.GREEN));

                if (Screen.hasShiftDown()) {
                    tooltip.add(Component.empty());
                    tooltip.add(Component.translatable("tooltip.cobblemon-cards.label.pokemon").withStyle(net.minecraft.ChatFormatting.GRAY)
                            .append(Component.literal(" " + localized).withStyle(net.minecraft.ChatFormatting.WHITE)));
                    tooltip.add(Component.translatable("tooltip.cobblemon-cards.label.rarity").withStyle(net.minecraft.ChatFormatting.GRAY)
                            .append(Component.translatable("rarity.cobblemon-cards.common").withStyle(net.minecraft.ChatFormatting.AQUA)));
                } else {
                    tooltip.add(Component.translatable("gui.cobblemon-cards.card_dex.view_details_tooltip").withStyle(net.minecraft.ChatFormatting.GRAY));
                }
            } else {
                tooltip.add(Component.literal(formattedNum + "???").withStyle(net.minecraft.ChatFormatting.GRAY));
                tooltip.add(Component.translatable("gui.cobblemon-cards.card_dex.undiscovered_status").withStyle(net.minecraft.ChatFormatting.RED));
            }

            graphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
        }

        // Draw hovered tabs tooltips
        if (hoveredTab != -1 && hoveredPoke == null) {
            List<Component> tabTooltip = new ArrayList<>();
            if (hoveredTab < 9) {
                tabTooltip.add(Component.translatable("gui.cobblemon-cards.card_dex.gen_tooltip", hoveredTab + 1).withStyle(net.minecraft.ChatFormatting.GOLD));
            } else if (hoveredTab == 9) {
                tabTooltip.add(Component.translatable("gui.cobblemon-cards.card_dex.national").withStyle(net.minecraft.ChatFormatting.GOLD));
            } else if (hoveredTab == 10) {
                tabTooltip.add(Component.translatable("gui.cobblemon-cards.card_dex.regionals").withStyle(net.minecraft.ChatFormatting.GOLD));
            } else {
                tabTooltip.add(Component.translatable("gui.cobblemon-cards.card_dex.megas").withStyle(net.minecraft.ChatFormatting.GOLD));
            }
            graphics.renderComponentTooltip(this.font, tabTooltip, mouseX, mouseY);
        }
    }

    private String getTabTitle(int tab) {
        if (tab >= 1 && tab <= 9) {
            return Component.translatable("gui.cobblemon-cards.card_dex.gen_tooltip", tab).getString();
        } else if (tab == 10) {
            return Component.translatable("gui.cobblemon-cards.card_dex.national").getString();
        } else if (tab == 11) {
            return Component.translatable("gui.cobblemon-cards.card_dex.regionals").getString();
        } else if (tab == 12) {
            return Component.translatable("gui.cobblemon-cards.card_dex.megas").getString();
        }
        return "Card Dex";
    }

    private int getPokedexNumberOf(String pokemonId) {
        String baseName = pokemonId;
        if (pokemonId.contains("_")) {
            baseName = pokemonId.split("_")[0];
        }
        try {
            String cleanId = baseName.replace("’", "").replace("'", "");
            Species species = PokemonSpecies.getByName(cleanId);
            if (species == null) {
                species = PokemonSpecies.getByName(baseName);
            }
            if (species != null) {
                return species.getNationalPokedexNumber();
            }
        } catch (Exception ignored) {}
        return 9999;
    }

    private void loadGeneration(int genOrTab) {
        genPokemon.clear();
        cardCache.clear();

        List<String> allPokemon = BoosterLootTable.getPokemonIds();

        java.util.Set<String> alolan = java.util.Set.of("vulpix", "ninetales", "sandshrew", "sandslash", "raichu", "meowth", "persian", "geodude", "graveler", "golem", "grimer", "muk", "exeggutor", "marowak");
        java.util.Set<String> galarian = java.util.Set.of("zigzagoon", "linoone", "ponyta", "rapidash", "farfetchd", "weezing", "mr_mime", "corsola", "darumaka", "darmanitan", "yamask", "stunfisk", "slowpoke", "slowbro", "slowking", "articuno", "zapdos", "moltres");
        java.util.Set<String> hisuian = java.util.Set.of("growlithe", "arcanine", "voltorb", "electrode", "typhlosion", "qwilfish", "sneasel", "zorua", "zoroark", "braviary", "sliggoo", "goodra", "avalugg", "decidueye", "samurott", "lilligant", "basculin");
        java.util.Set<String> mega = java.util.Set.of("venusaur", "charizard", "blastoise", "alakazam", "gengar", "kangaskhan", "pinsir", "gyarados", "aerodactyl", "mewtwo", "ampharos", "scizor", "heracross", "tyranitar", "blaziken", "gardevoir", "mawile", "aggron", "medicham", "manectric", "banette", "absol", "garchomp", "lucario", "abomasnow", "beedrill", "pidgeot", "steelix", "sceptile", "swampert", "sableye", "sharpedo", "camerupt", "altaria", "glalie", "salamence", "metagross", "latias", "latios", "rayquaza", "lopunny", "gallade", "audino", "diancie");

        if (genOrTab == 11) { // Formes régionales
            for (String id : allPokemon) {
                String pIdLower = id.toLowerCase();
                if (alolan.contains(pIdLower)) genPokemon.add(pIdLower + "_alolan");
                if (galarian.contains(pIdLower)) genPokemon.add(pIdLower + "_galarian");
                if (hisuian.contains(pIdLower)) genPokemon.add(pIdLower + "_hisuian");
            }
        } else if (genOrTab == 12) { // Méga-évolutions
            for (String id : allPokemon) {
                String pIdLower = id.toLowerCase();
                if (mega.contains(pIdLower)) {
                    if (pIdLower.equals("charizard") || pIdLower.equals("mewtwo")) {
                        genPokemon.add(pIdLower + "_mega_x");
                        genPokemon.add(pIdLower + "_mega_y");
                    } else {
                        genPokemon.add(pIdLower + "_mega");
                    }
                }
            }
        } else { // Générations 1 à 9 ou Dex National (10)
            for (String id : allPokemon) {
                try {
                    String cleanId = id.replace("’", "").replace("'", "");
                    Species species = PokemonSpecies.getByName(cleanId);
                    if (species == null) {
                        species = PokemonSpecies.getByName(id);
                    }

                    if (species != null) {
                        int dexNum = species.getNationalPokedexNumber();
                        boolean inTab = false;
                        switch (genOrTab) {
                            case 1 -> inTab = dexNum <= 151;
                            case 2 -> inTab = dexNum >= 152 && dexNum <= 251;
                            case 3 -> inTab = dexNum >= 252 && dexNum <= 386;
                            case 4 -> inTab = dexNum >= 387 && dexNum <= 493;
                            case 5 -> inTab = dexNum >= 494 && dexNum <= 649;
                            case 6 -> inTab = dexNum >= 650 && dexNum <= 721;
                            case 7 -> inTab = dexNum >= 722 && dexNum <= 809;
                            case 8 -> inTab = dexNum >= 810 && dexNum <= 898;
                            case 9 -> inTab = dexNum >= 899 && dexNum <= 1025;
                            case 10 -> inTab = true; // Dex National (tout)
                        }
                        if (inTab) {
                            genPokemon.add(id);
                        }
                    }
                } catch (Exception e) {
                    // Ignore special cases that fail to parse
                }
            }
        }

        // Si aucun pokémon de cette génération/onglet n'est chargé, on met des fallbacks
        if (genPokemon.isEmpty() && genOrTab == 1) {
            genPokemon.addAll(List.of("bulbasaur", "charmander", "squirtle", "pikachu", "eevee", "gengar", "mewtwo"));
        }

        // Triage intelligent par numéro national de Pokédex
        genPokemon.sort((id1, id2) -> {
            int num1 = getPokedexNumberOf(id1);
            int num2 = getPokedexNumberOf(id2);

            if (num1 != num2) {
                return Integer.compare(num1, num2);
            }
            return id1.compareTo(id2);
        });

        // Pré-génération des ItemStacks
        for (String pokemonId : genPokemon) {
            ItemStack card = new ItemStack(ModItems.CARD);
            boolean discovered = discoveredIds.contains(pokemonId.toLowerCase());

            CardData data;
            if (discovered) {
                data = new CardData(
                        pokemonId,
                        false,
                        "common",
                        CardStat.MOVEMENT_SPEED,
                        0.05f,
                        0,
                        Optional.empty(),
                        Optional.empty());
            } else {
                // Silhouette rendering prefix triggers gray/dark render in CardItemRenderer!
                data = new CardData(
                        "silhouette_" + pokemonId,
                        false,
                        "common",
                        CardStat.MOVEMENT_SPEED,
                        0.0f,
                        0,
                        Optional.empty(),
                        Optional.empty());
            }
            card.set(ModDataComponents.CARD_DATA, data);
            cardCache.put(pokemonId, card);
        }

        // Calcul du scroll max (accounting for top space and 146px viewport height)
        int totalRows = (int) Math.ceil((double) genPokemon.size() / 5.0);
        int gridContentHeight = 14 + (totalRows * (cardHeight + 22) - 22) + 12 + 10;
        maxScroll = Math.max(0, gridContentHeight - 146);
    }

    private void playClickSound() {
        Minecraft.getInstance().getSoundManager().play(
                net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F)
        );
    }

    private String truncateToWidth(String text, int maxWidth) {
        if (this.font.width(text) <= maxWidth) {
            return text;
        }
        String suffix = "..";
        int suffixW = this.font.width(suffix);
        String truncated = text;
        while (truncated.length() > 0 && this.font.width(truncated + suffix) > maxWidth) {
            truncated = truncated.substring(0, truncated.length() - 1);
        }
        return truncated + suffix;
    }

    private void updateScrollFromMouse(double mouseY) {
        int scrollbarY = this.albumY + 28;
        int scrollbarHeight = 144;
        int thumbHeight = Math.max(12, scrollbarHeight * scrollbarHeight / Math.max(1, scrollbarHeight + maxScroll));
        
        double availableScrollHeight = scrollbarHeight - thumbHeight;
        if (availableScrollHeight <= 0) return;
        
        double relativeMouseY = mouseY - scrollbarY - (thumbHeight / 2.0);
        double pct = relativeMouseY / availableScrollHeight;
        this.scrollAmount = Math.max(0, Math.min(maxScroll, pct * maxScroll));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            // Check if a bottom bookmark is clicked (Y: albumY + 192 to albumY + 228)
            // spaced by 4 pixels and centered: 12 * 16 + 11 * 4 = 236px total width
            int startX = this.albumX + (278 - 236) / 2;
            for (int i = 0; i < 12; i++) {
                int x = startX + i * 20; // 16px width + 4px gap
                int yMin = this.albumY + 192;
                int yMax = this.albumY + 228;
                if (mouseX >= x && mouseX < x + 16 && mouseY >= yMin && mouseY <= yMax) {
                    this.selectedTab = i + 1;
                    this.scrollAmount = 0;
                    loadGeneration(selectedTab);
                    playClickSound();
                    return true;
                }
            }

            // Check if scrollbar is clicked
            if (maxScroll > 0) {
                int scrollbarX = this.albumX + 248;
                int scrollbarY = this.albumY + 28;
                int scrollbarBottom = this.albumY + 172;
                
                // Zone de clic généreuse autour de la scrollbar (16 pixels)
                if (mouseX >= scrollbarX - 6 && mouseX <= scrollbarX + 10 && mouseY >= scrollbarY && mouseY <= scrollbarBottom) {
                    this.isDraggingScrollbar = true;
                    updateScrollFromMouse(mouseY);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.isDraggingScrollbar && button == 0 && maxScroll > 0) {
            updateScrollFromMouse(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.isDraggingScrollbar = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (maxScroll > 0) {
            this.scrollAmount = Math.max(0, Math.min(maxScroll, this.scrollAmount - scrollY * 16));
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (maxScroll > 0) {
            if (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_W) {
                this.scrollAmount = Math.max(0, this.scrollAmount - 16);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_DOWN || keyCode == GLFW.GLFW_KEY_S) {
                this.scrollAmount = Math.min(maxScroll, this.scrollAmount + 16);
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
