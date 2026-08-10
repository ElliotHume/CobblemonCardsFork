package com.howlite.cobblemoncards.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.client.Camera;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks players who are currently "showing off" a card and renders
 * the card large in the world in front of their chest/extended hands.
 *
 * Auto-expires after 8 seconds.
 */
public class CardShowRenderer {

    /** Duration a card stays visible in the world after being shown (ms). */
    /** Maximum duration a card stays visible in the world as a safety fallback (ms). */
    private static final long SHOW_DURATION_MS = 120_000L;

    /** Height of the card center relative to player feet (chest / hands level). */
    private static final float CARD_HEIGHT_OFFSET = 1.25f;

    /** How far in front of the player chest the card is pushed (blocks). */
    private static final float CARD_FORWARD_OFFSET = 0.60f;

    /** Card scale in world space. */
    private static final float CARD_SCALE = 1.15f;

    /** Backward tilt in degrees so top of card leans back toward player's face. */
    private static final float CARD_TILT_X = 8f;

    // UUID → active show entry
    private static final Map<UUID, ShowEntry> ACTIVE = new ConcurrentHashMap<>();

    // ═══════════════════════════════════════════════════════════════════════
    //  Public API
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Register that a player is showing their card.
     */
    public static void onPlayerShow(UUID holderId, ItemStack card) {
        ACTIVE.put(holderId, new ShowEntry(card.copy(), System.currentTimeMillis() + SHOW_DURATION_MS));
    }

    /**
     * Stop showing a player's card immediately (called when they close the GUI).
     */
    public static void stopPlayerShow(UUID holderId) {
        ACTIVE.remove(holderId);
    }

    /**
     * Check if a player is currently showing a card.
     * Used by HumanoidModelMixin to raise player arms forward.
     */
    public static boolean isShowingCard(UUID playerUuid) {
        ShowEntry entry = ACTIVE.get(playerUuid);
        return entry != null && System.currentTimeMillis() <= entry.expiry;
    }

    /**
     * Check if a specific item stack is currently being shown off in big.
     * Used by CardItemRenderer to suppress rendering the small hand item while showing off.
     */
    public static boolean isShowingStack(ItemStack stack) {
        if (stack == null || stack.isEmpty() || ACTIVE.isEmpty()) return false;
        long now = System.currentTimeMillis();
        for (ShowEntry entry : ACTIVE.values()) {
            if (now <= entry.expiry && ItemStack.isSameItemSameComponents(stack, entry.card)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Render all currently shown cards in the world.
     */
    public static void renderAll(PoseStack poseStack, MultiBufferSource buffers,
                                  Camera camera, Level level) {
        if (level == null) return;
        long now = System.currentTimeMillis();
        Vec3 camPos = camera.getPosition();

        Iterator<Map.Entry<UUID, ShowEntry>> iter = ACTIVE.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<UUID, ShowEntry> me = iter.next();
            if (now > me.getValue().expiry) {
                iter.remove();
                continue;
            }
            Player holder = level.getPlayerByUUID(me.getKey());
            if (holder == null) continue;

            poseStack.pushPose();
            // Translate to player position (relative to camera)
            poseStack.translate(
                    holder.getX() - camPos.x,
                    holder.getY() - camPos.y,
                    holder.getZ() - camPos.z
            );
            renderCardAbovePlayer(holder, me.getValue().card, poseStack, buffers);
            poseStack.popPose();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Internal render
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Renders the card in front of the given player's chest / extended hands.
     */
    private static void renderCardAbovePlayer(Player holder, ItemStack card,
                                               PoseStack poseStack, MultiBufferSource buffers) {
        poseStack.pushPose();

        // 1. Raise to chest / hands height
        poseStack.translate(0.0, CARD_HEIGHT_OFFSET, 0.0);

        // 2. Rotate to match player head direction (yaw)
        float headYaw = holder.getYHeadRot();
        poseStack.mulPose(Axis.YP.rotationDegrees(-headYaw));

        // 3. Move FORWARD in front of player's chest / extended hands (+Z in local space)
        poseStack.translate(0.0, 0.0, CARD_FORWARD_OFFSET);

        // 4. Flip 180 degrees so the FRONT face of the card (with the Pokemon sprite)
        // points AWAY from the player (towards observers standing in front of them)
        poseStack.mulPose(Axis.YP.rotationDegrees(180f));

        // 5. Slight backward tilt
        poseStack.mulPose(Axis.XP.rotationDegrees(CARD_TILT_X));

        // 6. Scale card
        poseStack.scale(CARD_SCALE, CARD_SCALE, CARD_SCALE);

        // Render standard item model
        Minecraft.getInstance().getItemRenderer().renderStatic(
                card,
                ItemDisplayContext.FIXED,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffers,
                Minecraft.getInstance().level,
                0
        );

        poseStack.popPose();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Data class
    // ═══════════════════════════════════════════════════════════════════════

    private record ShowEntry(ItemStack card, long expiry) {}
}
