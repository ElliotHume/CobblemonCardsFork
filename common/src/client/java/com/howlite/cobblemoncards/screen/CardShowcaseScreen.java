package com.howlite.cobblemoncards.screen;

import com.howlite.cobblemoncards.component.CardData;
import com.howlite.cobblemoncards.component.ModDataComponents;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.*;

/**
 * CardShowcaseScreen v3 — Écran cinématique modulaire pour mettre en scène les cartes.
 *
 * Contrôles :
 *  [H]         Masquer/afficher tout le texte de l'interface
 *  [R]         Rejouer les animations d'entrée
 *  Molette     Zoomer/dézoomer la carte survolée
 *  Clic-Glisser  Déplacer les cartes
 *  [ESC]       Fermer
 */
public class CardShowcaseScreen extends Screen {

    // ─── Constantes de base ──────────────────────────────────────────────────
    private static final int   CARD_W_BASE  = 82;    // largeur de hitbox de référence
    private static final int   CARD_H_BASE  = 115;   // hauteur de hitbox de référence
    private static final int   CARD_SCALE   = 138;   // échelle de rendu de base (px/unité)
    private static final float CARD_SCALE_MIN = 0.25f;
    private static final float CARD_SCALE_MAX = 3.5f;
    private static final int   STAR_COUNT   = 200;

    // ─── Données cartes ──────────────────────────────────────────────────────
    private final List<ItemStack>   cards;
    private final float[]           cx;           // centre X (modifiable par drag)
    private final float[]           cy;           // centre Y (modifiable par drag)
    private final float[]           rotation;     // inclinaison statique (légère)
    private final float[]           introP;       // animation d'entrée 0→1 par carte
    private final float[]           cardScale;    // zoom individuel par carte (1.0 = défaut)
    private final List<Integer>     zOrder;       // ordre de rendu (dernier = premier plan)

    // ─── Drag & drop ────────────────────────────────────────────────────────
    private int    dragging = -1;
    private double dragDX, dragDY;

    // ─── Affichage ──────────────────────────────────────────────────────────
    /** [H] : masque/affiche tout le texte (HUD + étiquettes de cartes). */
    private boolean showText = true;

    // ─── Temps ──────────────────────────────────────────────────────────────
    private float ticks          = 0f;
    private float introStartTick = 0f;
    private long lastTime        = 0L;

    // ─── Particules ─────────────────────────────────────────────────────────
    private final List<StarParticle>  stars  = new ArrayList<>();
    private final List<DriftParticle> drifts = new ArrayList<>();
    private final Random rng = new Random();

    // ════════════════════════════════════════════════════════════════════════
    //  Constructeur
    // ════════════════════════════════════════════════════════════════════════
    public CardShowcaseScreen(List<ItemStack> cards) {
        super(Component.translatable("screen.cobblemon-cards.showcase.title"));
        int n = Math.max(1, cards.size());
        this.cards     = new ArrayList<>(cards);
        this.cx        = new float[n];
        this.cy        = new float[n];
        this.rotation  = new float[n];
        this.introP    = new float[n];
        this.cardScale = new float[n];
        this.zOrder    = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            zOrder.add(i);
            cardScale[i] = 1.0f;
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  INIT
    // ════════════════════════════════════════════════════════════════════════
    @Override
    protected void init() {
        super.init();
        generateStars();
        layoutCards();
        ticks          = 0f;
        introStartTick = 0f;
        lastTime       = 0L;
        Arrays.fill(introP, 0f);
        drifts.clear();
        // Réinitialise les scales seulement à la première ouverture (pas sur resize fenêtre)
    }

    private void generateStars() {
        stars.clear();
        for (int i = 0; i < STAR_COUNT; i++) {
            stars.add(new StarParticle(
                    rng.nextFloat() * this.width,
                    rng.nextFloat() * this.height,
                    0.3f + rng.nextFloat() * 0.7f,
                    rng.nextFloat() * (float)(Math.PI * 2)
            ));
        }
    }

    /** Dispose les cartes en éventail centré. */
    private void layoutCards() {
        int n = cards.size();
        if (n == 0) return;
        float scrCX = width  / 2f;
        float scrCY = height / 2f;

        if (n == 1) {
            cx[0] = scrCX;
            cy[0] = scrCY;
            rotation[0] = 0f;
        } else {
            int spacing = Math.min(CARD_W_BASE + 20, (width - 80) / n);
            float startX = scrCX - (n - 1) * spacing / 2f;
            for (int i = 0; i < n; i++) {
                cx[i] = startX + i * spacing;
                cy[i] = scrCY;
                rotation[i] = (rng.nextFloat() - 0.5f) * 10f;
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  RENDER PRINCIPAL
    // ════════════════════════════════════════════════════════════════════════
    @Override
    public void render(GuiGraphics g, int mx, int my, float delta) {
        long now = System.currentTimeMillis();
        if (this.lastTime == 0L) {
            this.lastTime = now;
        }
        float deltaTime = (now - this.lastTime) / 50.0f;
        this.lastTime = now;

        ticks += deltaTime;

        // Mise à jour des progressions d'entrée (décalées par carte)
        for (int i = 0; i < cards.size(); i++) {
            float elapsed = (ticks - introStartTick) - i * 7f;
            if (elapsed > 0f) {
                introP[i] = Math.min(1f, introP[i] + deltaTime * 0.065f);
            }
        }

        // Fond
        renderBg(g);
        renderStars(g);

        // Cartes en z-order (draggée toujours au dessus)
        for (int j = 0; j < zOrder.size(); j++) {
            int i = zOrder.get(j);
            if (i != dragging) renderCard(g, i, j, mx, my);
        }
        if (dragging >= 0 && dragging < cards.size()) {
            renderCard(g, dragging, zOrder.size(), mx, my);
        }

        // Particules ambiantes
        spawnAndRenderDrifts(g, deltaTime);

        // HUD (masquable avec H)
        if (showText) renderHUD(g);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  FOND
    // ════════════════════════════════════════════════════════════════════════
    private void renderBg(GuiGraphics g) {
        g.fill(0, 0, this.width, this.height, 0xFF05050F);
        int bx = width / 2, by = height / 2;
        int[] radii  = {60, 110, 165, 230, 310, 420};
        int[] alphas = {35,  26,  18,  12,   7,   4};
        for (int l = radii.length - 1; l >= 0; l--) {
            int r = radii[l];
            g.fill(bx - r, by - (r * 2 / 3), bx + r, by + (r * 2 / 3),
                   (alphas[l] << 24) | 0x08081C);
        }
    }

    private void renderStars(GuiGraphics g) {
        RenderSystem.disableDepthTest();
        for (StarParticle s : stars) {
            float blink = 0.5f + 0.5f * (float)Math.sin(ticks * 0.032f + s.phase);
            int a = (int)(s.brightness * blink * 215);
            if (a < 6) continue;
            int col = (a << 24) | 0xCCDDFF;
            g.fill((int)s.x, (int)s.y, (int)s.x + 1, (int)s.y + 1, col);
            if (s.brightness > 0.75f) {
                int dim = ((a / 3) << 24) | 0xCCDDFF;
                g.fill((int)s.x - 1, (int)s.y, (int)s.x + 2, (int)s.y + 1, dim);
                g.fill((int)s.x, (int)s.y - 1, (int)s.x + 1, (int)s.y + 2, dim);
            }
        }
        // Nébuleuses d'après la carte au premier plan
        int topI = zOrder.isEmpty() ? 0 : zOrder.get(zOrder.size() - 1);
        CardData topData = (cards.isEmpty() || topI >= cards.size())
                ? null : cards.get(topI).get(ModDataComponents.CARD_DATA);
        int nc = getRarityColor(topData);
        int nr = (nc >> 16) & 0xFF, ng = (nc >> 8) & 0xFF, nb = nc & 0xFF;
        float p = 0.5f + 0.5f * (float)Math.sin(ticks * 0.02f);
        int bx = width / 2, by = height / 2;
        drawSoftEllipse(g, bx,       by,        230, 105, nr, ng, nb, (int)(7 + 4 * p));
        drawSoftEllipse(g, bx - 135, by + 45,   105,  58, nr, ng, nb, (int)(3 + 2 * p));
        drawSoftEllipse(g, bx + 145, by - 38,    95,  50, nr, ng, nb, (int)(3 + 2 * p));
        RenderSystem.enableDepthTest();
    }

    private void drawSoftEllipse(GuiGraphics g, int ex, int ey, int rx, int ry,
                                  int r, int gr, int b, int maxA) {
        for (int s = 5; s >= 1; s--) {
            float t = (float)s / 5f;
            int a = (int)(maxA * (1f - t) + 1);
            g.fill(ex - (int)(rx * t), ey - (int)(ry * t),
                   ex + (int)(rx * t), ey + (int)(ry * t),
                   (a << 24) | (r << 16) | (gr << 8) | b);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  RENDU D'UNE CARTE
    // ════════════════════════════════════════════════════════════════════════
    private void renderCard(GuiGraphics g, int i, int zIdx, int mx, int my) {
        float ease   = smoothStep(introP[i]);
        float yOff   = (1f - ease) * 200f;
        float alpha  = ease;
        float cs     = cardScale[i];   // zoom individuel de cette carte

        float floatY = (float)Math.sin(ticks * 0.055f + i * 1.4f) * 4.5f;
        float tiltZ  = rotation[i] + (float)Math.sin(ticks * 0.038f + i * 0.9f) * 1.8f;
        float tiltY  = (float)Math.sin(ticks * 0.022f + i * 1.1f) * 3.5f;

        float drawCX = cx[i];
        float drawCY = cy[i] + floatY + yOff;

        CardData data   = cards.get(i).get(ModDataComponents.CARD_DATA);
        int     rarCol  = getRarityColor(data);

        // God-rays (cartes rares+)
        if (isRareOrBetter(data) && introP[i] > 0.45f) {
            renderGodRays(g, (int)drawCX, (int)drawCY, rarCol, alpha * 0.85f);
        }

        // Halo (taille adaptée au zoom)
        renderHalo(g, (int)drawCX, (int)drawCY, rarCol, alpha, cs);

        // ── Carte 3D ─────────────────────────────────────────────────────
        Lighting.setupForFlatItems();
        g.pose().pushPose();
        g.pose().translate(drawCX, drawCY, 100f + zIdx * 4f);
        g.pose().mulPose(Axis.ZP.rotationDegrees(tiltZ));
        g.pose().mulPose(Axis.YP.rotationDegrees(tiltY));

        // Zoom : cardScale[i] appliqué à l'échelle de rendu + ease d'intro
        float sc = CARD_SCALE * cs * (0.35f + 0.65f * ease);
        g.pose().scale(sc, -sc, sc);

        Minecraft.getInstance().getItemRenderer().renderStatic(
                cards.get(i),
                ItemDisplayContext.GUI,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                g.pose(),
                g.bufferSource(),
                Minecraft.getInstance().level,
                0);
        g.flush();
        g.pose().popPose();

        // Étiquette : nom + rareté — masquable avec H, position Y adaptée au zoom
        if (alpha > 0.08f && showText) {
            renderLabel(g, i, data, alpha, drawCX, drawCY, cs);
        }
    }

    /**
     * Affiche nom + rareté sous la carte.
     * La position Y est calculée d'après l'échelle réelle de rendu pour ne jamais
     * chevaucher la carte, même après un zoom à la molette.
     */
    private void renderLabel(GuiGraphics g, int i, CardData data,
                              float alphaF, float drawCX, float drawCY, float cs) {
        int textA = (int)(alphaF * 220);
        // Distance depuis le centre jusqu'au bas visuel de la carte :
        //   CARD_SCALE × cs × 0.5 ≈ demi-hauteur visuelle
        // On ajoute 14px de marge supplémentaire.
        int baseY = (int)(drawCY + CARD_SCALE * cs * 0.5f + 14f);

        // Nom du Pokémon
        boolean shiny = data != null && data.isShiny();
        String name   = getPokemonDisplayName(data);
        String label  = (shiny ? "✦ " : "") + name + (shiny ? " ✦" : "");
        g.drawCenteredString(this.font, label, (int)drawCX, baseY,
                             withAlpha(textA, getRarityColor(data)));

        // Rareté
        String rarLabel = getRarityLabel(data);
        g.drawCenteredString(this.font, rarLabel, (int)drawCX, baseY + 11,
                             withAlpha((int)(textA * 0.7f), 0xCCCCCC));
    }

    // ════════════════════════════════════════════════════════════════════════
    //  GOD RAYS & HALO
    // ════════════════════════════════════════════════════════════════════════
    private void renderGodRays(GuiGraphics g, int drawCX, int drawCY, int color, float alpha) {
        int r = (color >> 16) & 0xFF, gr = (color >> 8) & 0xFF, b = color & 0xFF;
        float time   = ticks * 0.45f;
        float maxLen = 88f + (float)Math.sin(ticks * 0.065f) * 11f;

        g.pose().pushPose();
        g.pose().translate(drawCX, drawCY, 50);
        g.pose().mulPose(Axis.ZP.rotationDegrees(time));
        for (int i = 0; i < 10; i++) {
            g.pose().pushPose();
            g.pose().mulPose(Axis.ZP.rotationDegrees(i * 36f));
            int a = (int)(alpha * 36);
            g.fill(-5, (int)-maxLen, 5, (int)maxLen, (a << 24) | (r << 16) | (gr << 8) | b);
            g.pose().popPose();
        }
        g.pose().popPose();

        g.pose().pushPose();
        g.pose().translate(drawCX, drawCY, 50);
        g.pose().mulPose(Axis.ZP.rotationDegrees(-time * 0.55f));
        for (int i = 0; i < 5; i++) {
            g.pose().pushPose();
            g.pose().mulPose(Axis.ZP.rotationDegrees(i * 72f));
            int a = (int)(alpha * 20);
            float hl = maxLen * 0.58f;
            g.fill(-3, (int)-hl, 3, (int)hl, (a << 24) | (r << 16) | (gr << 8) | b);
            g.pose().popPose();
        }
        g.pose().popPose();
    }

    /** Halo dont la taille s'adapte au zoom individuel de la carte. */
    private void renderHalo(GuiGraphics g, int drawCX, int drawCY, int color, float alpha, float cs) {
        int r = (color >> 16) & 0xFF, gr = (color >> 8) & 0xFF, b = color & 0xFF;
        float pulse = 0.5f + 0.5f * (float)Math.sin(ticks * 0.09f);

        // Demi-dimensions visuelles de la carte selon zoom
        int hw = (int)(CARD_W_BASE * cs / 2f + 26);
        int hh = (int)(CARD_H_BASE * cs / 2f + 26);
        int a1 = (int)(alpha * (16 + 11 * pulse));
        g.fill(drawCX - hw, drawCY - hh, drawCX + hw, drawCY + hh,
               (a1 << 24) | (r << 16) | (gr << 8) | b);

        int hw2 = (int)(CARD_W_BASE * cs / 2f + 12);
        int hh2 = (int)(CARD_H_BASE * cs / 2f + 12);
        int a2  = (int)(alpha * (9 + 7 * pulse));
        g.fill(drawCX - hw2, drawCY - hh2, drawCX + hw2, drawCY + hh2,
               (a2 << 24) | (r << 16) | (gr << 8) | b);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  PARTICULES AMBIANTES
    // ════════════════════════════════════════════════════════════════════════
    private void spawnAndRenderDrifts(GuiGraphics g, float delta) {
        if (!cards.isEmpty()) {
            int topI   = zOrder.get(zOrder.size() - 1);
            CardData d = cards.get(topI).get(ModDataComponents.CARD_DATA);
            float chance = getSpawnChance(d);
            if (introP[topI] > 0.3f && rng.nextFloat() < chance * delta) {
                int col = getRarityColor(d);
                float cs = cardScale[topI];
                float px = cx[topI] + (rng.nextFloat() - 0.5f) * (CARD_W_BASE * cs + 30);
                float py = cy[topI] + (rng.nextFloat() - 0.5f) * (CARD_H_BASE * cs + 30);
                drifts.add(new DriftParticle(px, py,
                        (rng.nextFloat() - 0.5f) * 0.45f,
                        -0.25f - rng.nextFloat() * 0.55f,
                        col, 1.5f + rng.nextFloat() * 2.2f,
                        28 + rng.nextInt(38)));
            }
        }

        RenderSystem.disableDepthTest();
        for (int p = drifts.size() - 1; p >= 0; p--) {
            DriftParticle dp = drifts.get(p);
            if (dp.update(delta)) drifts.remove(p);
            else dp.render(g);
        }
        RenderSystem.enableDepthTest();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  HUD (masquable avec H)
    // ════════════════════════════════════════════════════════════════════════
    private void renderHUD(GuiGraphics g) {
        int cx = width / 2;

        // Titre
        Component title = Component.translatable("screen.cobblemon-cards.showcase.title");
        g.drawCenteredString(this.font, title, cx, 15, 0xCCDDFF);
        int lw = this.font.width(title.getString()) + 18;
        g.fill(cx - lw / 2, 26, cx + lw / 2, 27, 0x33CCDDFF);

        // Astuce en bas
        Component hint = Component.translatable("screen.cobblemon-cards.showcase.hint");
        g.drawCenteredString(this.font, hint, cx, this.height - 11, 0x445566);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  ÉVÉNEMENTS
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // [R] — Rejouer les animations d'entrée
        if (keyCode == GLFW.GLFW_KEY_R) {
            introStartTick = ticks;
            Arrays.fill(introP, 0f);
            drifts.clear();
            return true;
        }
        // [H] — Masquer/afficher tout le texte de l'interface
        if (keyCode == GLFW.GLFW_KEY_H) {
            showText = !showText;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (btn == 0) {
            for (int j = zOrder.size() - 1; j >= 0; j--) {
                int i = zOrder.get(j);
                if (isOverCard(i, mx, my)) {
                    dragging = i;
                    dragDX   = mx - cx[i];
                    dragDY   = my - cy[i];
                    // Monter au premier plan
                    zOrder.remove(j);
                    zOrder.add(i);
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dX, double dY) {
        if (dragging >= 0 && btn == 0) {
            cx[dragging] = (float)(mx - dragDX);
            cy[dragging] = (float)(my - dragDY);
            return true;
        }
        return super.mouseDragged(mx, my, btn, dX, dY);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        if (btn == 0) dragging = -1;
        return super.mouseReleased(mx, my, btn);
    }

    /**
     * Molette sur une carte : zoome/dézoome cette carte individuellement.
     * La carte survolée est trouvée en parcourant le z-order du dessus vers le bas.
     */
    @Override
    public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        for (int j = zOrder.size() - 1; j >= 0; j--) {
            int i = zOrder.get(j);
            if (isOverCard(i, mx, my)) {
                float delta = (float)scrollY * 0.08f;
                cardScale[i] = Math.max(CARD_SCALE_MIN, Math.min(CARD_SCALE_MAX, cardScale[i] + delta));
                return true;
            }
        }
        return super.mouseScrolled(mx, my, scrollX, scrollY);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  UTILITAIRES
    // ════════════════════════════════════════════════════════════════════════

    /** Vérifie si la souris est dans la zone cliquable d'une carte (tient compte du zoom). */
    private boolean isOverCard(int i, double mx, double my) {
        if (introP[i] < 0.9f) return false;
        float cs     = cardScale[i];
        float floatY = (float)Math.sin(ticks * 0.055f + i * 1.4f) * 4.5f;
        float dx     = (float)(mx - cx[i]);
        float dy     = (float)(my - (cy[i] + floatY));
        // Hitbox proportionnelle au zoom
        float halfW = CARD_W_BASE * cs / 2f + 8;
        float halfH = CARD_H_BASE * cs / 2f + 20;
        return Math.abs(dx) <= halfW && Math.abs(dy) <= halfH;
    }

    private float smoothStep(float x) {
        x = Math.max(0f, Math.min(1f, x));
        return x * x * (3f - 2f * x);
    }

    private int withAlpha(int alpha, int rgb) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (rgb & 0xFFFFFF);
    }

    private int getRarityColor(CardData data) {
        if (data == null) return 0xAAAAAA;
        if (data.isShiny()) return 0xFFEE44;
        return switch (data.rarity().toLowerCase()) {
            case "uncommon"  -> 0x44FF88;
            case "rare"      -> 0x4499FF;
            case "epic"      -> 0xCC44FF;
            case "legendary" -> 0xFFAA00;
            case "mythic"    -> 0xFF3344;
            default          -> 0xBBBBBB;
        };
    }

    private boolean isRareOrBetter(CardData data) {
        if (data == null) return false;
        if (data.isShiny()) return true;
        return switch (data.rarity().toLowerCase()) {
            case "rare", "epic", "legendary", "mythic" -> true;
            default -> false;
        };
    }

    private float getSpawnChance(CardData data) {
        if (data == null) return 0.04f;
        if (data.isShiny()) return 0.38f;
        return switch (data.rarity().toLowerCase()) {
            case "legendary", "mythic" -> 0.32f;
            case "epic"      -> 0.22f;
            case "rare"      -> 0.13f;
            case "uncommon"  -> 0.07f;
            default          -> 0.025f;
        };
    }

    private String getPokemonDisplayName(CardData data) {
        if (data == null) return "???";
        String id       = data.pokemonId();
        String basePart = id.split("_")[0];
        String loc      = I18n.get("cobblemon.species." + basePart);
        if (loc.startsWith("cobblemon.species.")) {
            return capitalize(id.replace("_", " "));
        }
        if (id.endsWith("_alolan"))   return loc + " (Alolan)";
        if (id.endsWith("_galarian")) return loc + " (Galarian)";
        if (id.endsWith("_hisuian"))  return loc + " (Hisuian)";
        if (id.endsWith("_mega_x"))   return loc + " Mega X";
        if (id.endsWith("_mega_y"))   return loc + " Mega Y";
        if (id.endsWith("_mega"))     return loc + " Mega";
        return loc;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        String[] words = s.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty())
                sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    private String getRarityLabel(CardData data) {
        if (data == null) return "";
        String key = "rarity.cobblemon-cards." + data.rarity().toLowerCase();
        String loc = I18n.get(key);
        String base = loc.equals(key) ? capitalize(data.rarity()) : loc;
        return base + (data.isShiny() ? " ★ Shiny" : "");
    }

    @Override
    public boolean isPauseScreen() { return false; }

    // ════════════════════════════════════════════════════════════════════════
    //  CLASSES INTERNES
    // ════════════════════════════════════════════════════════════════════════

    private static class StarParticle {
        final float x, y, brightness, phase;
        StarParticle(float x, float y, float brightness, float phase) {
            this.x = x; this.y = y; this.brightness = brightness; this.phase = phase;
        }
    }

    private static class DriftParticle {
        float x, y, dx, dy;
        final int   color;
        final float size;
        float     life;
        final float   maxLife;

        DriftParticle(float x, float y, float dx, float dy,
                      int color, float size, float maxLife) {
            this.x = x; this.y = y; this.dx = dx; this.dy = dy;
            this.color = color; this.size = size;
            this.life = maxLife; this.maxLife = maxLife;
        }

        boolean update(float delta) {
            x  += dx * delta;
            y  += dy * delta;
            dy -= 0.008f * delta;
            dx *= (float) Math.pow(0.992, delta);
            life -= delta;
            return life <= 0f;
        }

        void render(GuiGraphics g) {
            float t    = this.life / this.maxLife;
            if (t < 0f) t = 0f;
            if (t > 1f) t = 1f;
            float fade = t < 0.2f ? t / 0.2f : (t > 0.85f ? (1f - t) / 0.15f : 1f);
            int r      = (color >> 16) & 0xFF;
            int gr     = (color >> 8)  & 0xFF;
            int b      =  color        & 0xFF;
            int a      = (int)(fade * 175);
            int col    = (a << 24) | (r << 16) | (gr << 8) | b;
            int rx     = Math.round(x);
            int ry     = Math.round(y);
            int s      = Math.max(1, Math.round(size * fade));
            g.fill(rx - 1, ry - s, rx + 1, ry + s, col);
            g.fill(rx - s, ry - 1, rx + s, ry + 1, col);
            g.fill(rx, ry, rx + 1, ry + 1, ((int)(fade * 200) << 24) | 0xFFFFFF);
        }
    }
}
