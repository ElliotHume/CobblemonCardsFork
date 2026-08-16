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
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * CardInspectScreen — Full-screen single-card inspection mode.
 *
 * Triggered by right-clicking a card in the player's hand.
 * Features:
 *  • Standard Minecraft dark overlay background (no starfield)
 *  • Float-in entry animation (card rises from below)
 *  • Mouse-driven 3D tilt: moving the mouse tilts the card on X/Y axes,
 *    making holographic rainbow bands shimmer as the light angle changes.
 *  • Idle gentle vertical float
 *  • God-rays behind the card (rarity-coloured)
 *  • Rarity-coloured halo glow
 *  • Drift particles (rarity-coloured)
 *  • No text labels or HUD — purely visual
 *  • [ESC] closes
 */
public class CardInspectScreen extends Screen {

    // ─── Layout & Scale ────────────────────────────────────────────────────
    private static final float CARD_SCALE_BASE = 240f;
    private static final float CARD_HW = 82f * 1.5f;
    private static final float CARD_HH = 115f * 1.5f;

    // ─── Animation timing ──────────────────────────────────────────────────
    private static final float INTRO_DURATION = 18f;
    private static final float MAX_TILT_DEG   = 22f;

    // ─── Card data ─────────────────────────────────────────────────────────
    private final ItemStack card;
    private final CardData  data;

    // ─── Animation state ───────────────────────────────────────────────────
    private float ticks   = 0f;
    private float introP  = 0f;
    private long lastTime = 0L;

    // ─── Mouse tilt (smooth) ───────────────────────────────────────────────
    private float tiltY       = 0f;
    private float tiltX       = 0f;
    private float targetTiltY = 0f;
    private float targetTiltX = 0f;

    // ─── Particles ─────────────────────────────────────────────────────────
    private final List<DriftParticle> drifts = new ArrayList<>();
    private final Random rng = new Random();

    // ═══════════════════════════════════════════════════════════════════════
    //  Constructor
    // ═══════════════════════════════════════════════════════════════════════

    public CardInspectScreen(ItemStack card) {
        super(Component.empty());
        this.card = card.copy();
        this.data = this.card.get(ModDataComponents.CARD_DATA);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Init
    // ═══════════════════════════════════════════════════════════════════════

    @Override
    protected void init() {
        super.init();
        ticks    = 0f;
        introP   = 0f;
        lastTime = 0L;
        drifts.clear();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Main Render
    // ═══════════════════════════════════════════════════════════════════════

    @Override
    public void render(GuiGraphics g, int mx, int my, float delta) {
        // ── Time update ──────────────────────────────────────────────────
        long now = System.currentTimeMillis();
        if (lastTime == 0L) lastTime = now;
        float dt = (now - lastTime) / 50.0f;
        lastTime = now;
        ticks += dt;

        // ── Intro progress ───────────────────────────────────────────────
        introP = Math.min(1f, introP + dt / INTRO_DURATION);

        // ── Mouse tilt targets ───────────────────────────────────────────
        float normX = Math.max(-1f, Math.min(1f, ((float) mx / this.width  - 0.5f) * 2f));
        float normY = Math.max(-1f, Math.min(1f, ((float) my / this.height - 0.5f) * 2f));
        targetTiltY =  normX * MAX_TILT_DEG;
        targetTiltX = -normY * MAX_TILT_DEG;

        // ── Smooth tilt interpolation ────────────────────────────────────
        float smooth = 1f - (float) Math.pow(0.04, dt);
        tiltY += (targetTiltY - tiltY) * smooth;
        tiltX += (targetTiltX - tiltX) * smooth;

        float ease = smoothStep(introP);

        // ── Card centre ──────────────────────────────────────────────────
        float cx = this.width  / 2f;
        float cy = this.height / 2f;
        float floatY    = (float) Math.sin(ticks * 0.055f) * 5f;
        float entryOffY = (1f - ease) * 180f;
        float drawCY    = cy + floatY + entryOffY;

        // ── Standard Minecraft dark overlay (no starfield) ───────────────
        g.fill(0, 0, this.width, this.height, 0xB0000000);

        // ── Visual effects ───────────────────────────────────────────────
        int rarCol = getRarityColor();

        if (isRareOrBetter() && introP > 0.35f) {
            renderGodRays(g, (int) cx, (int) drawCY, rarCol, ease * 0.9f);
        }
        renderHalo(g, (int) cx, (int) drawCY, rarCol, ease);
        renderCard3D(g, cx, drawCY, ease);
        spawnAndRenderDrifts(g, dt, cx, drawCY);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  God-rays
    // ═══════════════════════════════════════════════════════════════════════

    private void renderGodRays(GuiGraphics g, int cx, int cy, int color, float alpha) {
        int r = (color >> 16) & 0xFF, gr = (color >> 8) & 0xFF, b = color & 0xFF;
        float time   = ticks * 0.38f;
        float maxLen = 110f + (float) Math.sin(ticks * 0.055f) * 14f;

        g.pose().pushPose();
        g.pose().translate(cx, cy, 50);
        g.pose().mulPose(Axis.ZP.rotationDegrees(time));
        for (int i = 0; i < 12; i++) {
            g.pose().pushPose();
            g.pose().mulPose(Axis.ZP.rotationDegrees(i * 30f));
            int a = (int) (alpha * 38);
            g.fill(-4, (int) -maxLen, 4, (int) maxLen, (a << 24) | (r << 16) | (gr << 8) | b);
            g.pose().popPose();
        }
        g.pose().popPose();

        g.pose().pushPose();
        g.pose().translate(cx, cy, 50);
        g.pose().mulPose(Axis.ZP.rotationDegrees(-time * 0.5f));
        for (int i = 0; i < 6; i++) {
            g.pose().pushPose();
            g.pose().mulPose(Axis.ZP.rotationDegrees(i * 60f));
            int a = (int) (alpha * 22);
            float hl = maxLen * 0.62f;
            g.fill(-3, (int) -hl, 3, (int) hl, (a << 24) | (r << 16) | (gr << 8) | b);
            g.pose().popPose();
        }
        g.pose().popPose();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Halo
    // ═══════════════════════════════════════════════════════════════════════

    private void renderHalo(GuiGraphics g, int cx, int cy, int color, float alpha) {
        int r = (color >> 16) & 0xFF, gr = (color >> 8) & 0xFF, b = color & 0xFF;
        float pulse = 0.5f + 0.5f * (float) Math.sin(ticks * 0.09f);

        int hw  = (int) (CARD_HW / 2f + 32);
        int hh  = (int) (CARD_HH / 2f + 32);
        int a1  = (int) (alpha * (20 + 13 * pulse));
        g.fill(cx - hw, cy - hh, cx + hw, cy + hh, (a1 << 24) | (r << 16) | (gr << 8) | b);

        int hw2 = (int) (CARD_HW / 2f + 14);
        int hh2 = (int) (CARD_HH / 2f + 14);
        int a2  = (int) (alpha * (11 + 8 * pulse));
        g.fill(cx - hw2, cy - hh2, cx + hw2, cy + hh2, (a2 << 24) | (r << 16) | (gr << 8) | b);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Card 3D render
    // ═══════════════════════════════════════════════════════════════════════

    private void renderCard3D(GuiGraphics g, float cx, float cy, float ease) {
        Lighting.setupForFlatItems();
        g.pose().pushPose();
        g.pose().translate(cx, cy, 200f);
        g.pose().mulPose(Axis.XP.rotationDegrees(tiltX));
        g.pose().mulPose(Axis.YP.rotationDegrees(tiltY));
        float sc = CARD_SCALE_BASE * (0.3f + 0.7f * ease);
        g.pose().scale(sc, -sc, sc);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                card,
                ItemDisplayContext.GUI,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                g.pose(),
                g.bufferSource(),
                Minecraft.getInstance().level,
                0
        );
        g.flush();
        g.pose().popPose();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Drift particles
    // ═══════════════════════════════════════════════════════════════════════

    private void spawnAndRenderDrifts(GuiGraphics g, float dt, float cx, float cy) {
        float chance = getSpawnChance();
        if (introP > 0.3f && rng.nextFloat() < chance * dt) {
            int col = getRarityColor();
            float px = cx + (rng.nextFloat() - 0.5f) * (CARD_HW + 30);
            float py = cy + (rng.nextFloat() - 0.5f) * (CARD_HH + 30);
            drifts.add(new DriftParticle(px, py,
                    (rng.nextFloat() - 0.5f) * 0.5f,
                    -0.3f - rng.nextFloat() * 0.6f,
                    col, 1.8f + rng.nextFloat() * 2.5f, 28 + rng.nextInt(42)));
        }

        RenderSystem.disableDepthTest();
        for (int p = drifts.size() - 1; p >= 0; p--) {
            DriftParticle dp = drifts.get(p);
            if (dp.update(dt)) drifts.remove(p);
            else               dp.render(g);
        }
        RenderSystem.enableDepthTest();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Utilities
    // ═══════════════════════════════════════════════════════════════════════

    private float smoothStep(float x) {
        x = Math.max(0f, Math.min(1f, x));
        return x * x * (3f - 2f * x);
    }

    private int getRarityColor() {
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

    private boolean isRareOrBetter() {
        if (data == null) return false;
        if (data.isShiny()) return true;
        return switch (data.rarity().toLowerCase()) {
            case "rare", "epic", "legendary", "mythic" -> true;
            default -> false;
        };
    }

    private float getSpawnChance() {
        if (data == null) return 0.04f;
        if (data.isShiny()) return 0.42f;
        return switch (data.rarity().toLowerCase()) {
            case "legendary", "mythic" -> 0.35f;
            case "epic"      -> 0.25f;
            case "rare"      -> 0.15f;
            case "uncommon"  -> 0.08f;
            default          -> 0.03f;
        };
    }

    @Override
    public void onClose() {
        com.howlite.cobblemoncards.util.PlatformHelper.INSTANCE.sendToServer(
                new com.howlite.cobblemoncards.network.CloseInspectPayload()
        );
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() { return false; }

    // ═══════════════════════════════════════════════════════════════════════
    //  Inner class — drift particle
    // ═══════════════════════════════════════════════════════════════════════

    private static class DriftParticle {
        float x, y, dx, dy;
        final int   color;
        final float size;
        float       life;
        final float maxLife;

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
            float t    = Math.max(0f, Math.min(1f, this.life / this.maxLife));
            float fade = t < 0.2f ? t / 0.2f : (t > 0.85f ? (1f - t) / 0.15f : 1f);
            int r      = (color >> 16) & 0xFF;
            int gr     = (color >> 8)  & 0xFF;
            int b      =  color        & 0xFF;
            int a      = (int) (fade * 180);
            int col    = (a << 24) | (r << 16) | (gr << 8) | b;
            int rx     = Math.round(x);
            int ry     = Math.round(y);
            int s      = Math.max(1, Math.round(size * fade));
            g.fill(rx - 1, ry - s, rx + 1, ry + s, col);
            g.fill(rx - s, ry - 1, rx + s, ry + 1, col);
            g.fill(rx, ry, rx + 1, ry + 1, ((int) (fade * 210) << 24) | 0xFFFFFF);
        }
    }
}
