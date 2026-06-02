package com.howlite.cobblemoncards.client.render;

import com.howlite.cobblemoncards.component.CardData;
import com.howlite.cobblemoncards.component.ModDataComponents;
import com.howlite.cobblemoncards.item.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import com.mojang.authlib.GameProfile;

public class CardItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {

    private static final ResourceLocation TEXTURE_STARS = ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "textures/item/cards/effect/foil_stars.png");
    private static final ResourceLocation TEXTURE_GLINT = ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "textures/item/cards/effect/glint.png");
    private static final ResourceLocation TEXTURE_NOISE = ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "textures/item/cards/effect/noise.png");
    private static final ResourceLocation TEXTURE_FLOW = ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "textures/item/cards/effect/flow.png");

    @Override
    public void render(ItemStack stack, ItemDisplayContext mode, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {

        // 🛑 SÉCURITÉ :
        if (Minecraft.getInstance().level == null || Minecraft.getInstance().player == null) return;

        CardData data = stack.get(ModDataComponents.CARD_DATA);
        // Si la carte n'a pas de données, on affiche un modèle de "carte mystère" par défaut
        if (data == null) {
            renderDefaultCard(matrices, vertexConsumers, light, overlay, mode);
            return;
        }

        // --- 1. CHARGER LE MODÈLE BLOCKBENCH (Le cadre selon la rareté) ---
        Item frameItem = getFrameItem(data);
        ItemStack frameStack = new ItemStack(frameItem);
        BakedModel frameModel = Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(frameStack);
        boolean leftHand = mode == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || mode == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;

        matrices.pushPose();

        // 1. Annuler le décalage natif de Minecraft pour recentrer le point de pivot de la matrice.
        matrices.translate(0.5f, 0.5f, 0.5f);

        // 2. Appliquer les réglages "Display" de ton fichier Blockbench.
        frameModel.getTransforms().getTransform(mode).apply(leftHand, matrices);

        // --- 2. DESSINER LE MODÈLE 3D (CADRE) ---
        // ATTENTION : Pour le DynamicItemRenderer en 1.21.1, il faut utiliser la fonction de rendu qui ne crée pas de boucle infinie.
        Minecraft.getInstance().getItemRenderer().render(
                frameStack, ItemDisplayContext.NONE, leftHand, matrices, vertexConsumers, light, overlay, frameModel
        );

        // Préparation des coordonnées communes
        matrices.translate(-0.5f, -0.5f, -0.5f);

        // REGLAGE DU RATIO 40x30
        float imageWidth = 40.0f;
        float imageHeight = 30.0f;
        float pokeHeight = 6.0f / 16.0f;
        float pokeWidth = pokeHeight * (imageWidth / imageHeight);

        // --- 3. DESSINER LE FOND (BG) ---
        if (data.background().isPresent()) {
            matrices.pushPose();
            // On recule le fond (Z plus grand = plus loin cause rotation 180)
            matrices.translate(5.0f / 16.0f, 10.0f / 16.0f, 0.995f / 16.0f);
            matrices.mulPose(Axis.YP.rotationDegrees(180));

            ResourceLocation bgTex = ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "textures/item/cards/background/" + data.background().get() + ".png");

            renderQuad(matrices.last().pose(), vertexConsumers.getBuffer(RenderType.entityCutout(bgTex)), light, overlay, pokeWidth, pokeHeight);
            matrices.popPose();
        }

        // --- 4. DESSINER LE POKÉMON ---
        matrices.pushPose();
        // On avance le Pokémon (Z plus petit = plus proche du joueur)
        matrices.translate(5.0f / 16.0f, 10.0f / 16.0f, 0.985f / 16.0f);
        matrices.mulPose(Axis.YP.rotationDegrees(180));

        if (data.pokemonId().startsWith("player_")) {
            String nameAndUuid = data.pokemonId().substring("player_".length());
            int underscoreIdx = nameAndUuid.indexOf('_');
            java.util.UUID uuid;
            String name;
            if (underscoreIdx != -1) {
                try {
                    uuid = java.util.UUID.fromString(nameAndUuid.substring(0, underscoreIdx));
                    name = nameAndUuid.substring(underscoreIdx + 1);
                } catch (IllegalArgumentException e) {
                    uuid = java.util.UUID.nameUUIDFromBytes(nameAndUuid.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    name = nameAndUuid;
                }
            } else {
                uuid = java.util.UUID.nameUUIDFromBytes(nameAndUuid.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                name = nameAndUuid;
            }
            ResourceLocation skinTex = Minecraft.getInstance().getSkinManager().getInsecureSkin(new GameProfile(uuid, name)).texture();
            
            float headSize = pokeHeight * 0.7f;
            
            // Draw base head layer
            renderQuad(matrices.last().pose(), vertexConsumers.getBuffer(RenderType.entityTranslucent(skinTex)), light, overlay, headSize, headSize, 0.125f, 0.125f, 0.25f, 0.25f, 255, 255, 255, 255);
            
            // Draw hat/hair layer (overlay)
            matrices.pushPose();
            matrices.translate(0, 0, -0.0005f); // Tiny forward translation to prevent Z-fighting
            renderQuad(matrices.last().pose(), vertexConsumers.getBuffer(RenderType.entityTranslucent(skinTex)), light, overlay, headSize, headSize, 0.625f, 0.125f, 0.75f, 0.25f, 255, 255, 255, 255);
            matrices.popPose();
        } else {
            ResourceLocation pokemonTex;
            if (data.pokemonId().equalsIgnoreCase("missingno")) {
                pokemonTex = ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "textures/item/cards/pokemon/easter_egg/missingno.png");
            } else {
                String folder = data.isShiny() ? "shiny" : "regular";
                pokemonTex = ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "textures/item/cards/pokemon/" + folder + "/" + data.pokemonId() + ".png");
            }

            renderQuad(matrices.last().pose(), vertexConsumers.getBuffer(RenderType.entityCutout(pokemonTex)), light, overlay, pokeWidth, pokeHeight);
        }
        matrices.popPose();

        // --- 5. DESSINER L'EFFET HOLOGRAPHIQUE ---
        if (data.effect().isPresent()) {
            renderHoloLayer(matrices, vertexConsumers, data.effect().get(), light, overlay, pokeWidth, pokeHeight);
        }

        matrices.popPose();
    }

    private void renderDefaultCard(PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, ItemDisplayContext mode) {
        ItemStack defaultStack = new ItemStack(ModItems.FRAME_COMMON);
        BakedModel defaultModel = Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(defaultStack);
        boolean leftHand = mode == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || mode == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;

        matrices.pushPose();
        matrices.translate(0.5f, 0.5f, 0.5f);
        defaultModel.getTransforms().getTransform(mode).apply(leftHand, matrices);
        Minecraft.getInstance().getItemRenderer().render(defaultStack, ItemDisplayContext.NONE, leftHand, matrices, vertexConsumers, light, overlay, defaultModel);
        matrices.popPose();
    }

    private void renderHoloLayer(PoseStack matrices, MultiBufferSource vertexConsumers, String effect, int light, int overlay, float pokeWidth, float pokeHeight) {
        matrices.pushPose();
        
        // Offset pour éviter le Z-fighting
        float zPos = 0.983f;
        
        matrices.translate(5.0f / 16.0f, 10.0f / 16.0f, zPos / 16.0f);
        matrices.mulPose(Axis.YP.rotationDegrees(180));

        switch (effect) {
            case "foil_stars" -> renderFoilStar(matrices, vertexConsumers, light, overlay, pokeWidth, pokeHeight);
            case "glint" -> renderDynamicGlint(matrices, vertexConsumers, light, overlay, pokeWidth, pokeHeight);
            case "flow" -> renderFlow(matrices, vertexConsumers, light, overlay, pokeWidth, pokeHeight);
            case "noise" -> renderPlasma(matrices, vertexConsumers, light, overlay, pokeWidth, pokeHeight);
        }

        matrices.popPose();
    }

    private void renderFoilStar(PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, float w, float h) {
        float time = (System.currentTimeMillis() % 10000) / 10000f;
        float alpha = 0.5f + 0.5f * (float) Math.sin(System.currentTimeMillis() / 500.0);

        float uOffset = time;
        float vOffset = time;

        VertexConsumer consumer = vertexConsumers.getBuffer(RenderType.entityTranslucent(TEXTURE_STARS));
        renderQuad(matrices.last().pose(), consumer, light, overlay, w, h, uOffset, vOffset, 1f + uOffset, 1f + vOffset, 255, 255, 255, (int)(alpha * 255));
    }

    private void renderDynamicGlint(PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, float w, float h) {
        // --- CONFIGURATION DE L'ANIMATION ---
        int totalFrames = 80;      // Nombre de frames réelles dans le PNG
        int frameDurationMs = 50;  // Vitesse : 50ms par frame (soit 20 FPS, comme Minecraft par défaut)
        
        // Calcul de la frame actuelle en fonction du temps système
        int currentFrame = (int) ((System.currentTimeMillis() / frameDurationMs) % totalFrames);
        
        // Calcul des coordonnées UV (V représente l'axe vertical)
        float v0 = (float) currentFrame / totalFrames;
        float v1 = (float) (currentFrame + 1) / totalFrames;

        // On utilise entityTranslucent car energySwirl est fait pour du défilement continu, pas des frames
        VertexConsumer consumer = vertexConsumers.getBuffer(RenderType.entityTranslucent(TEXTURE_GLINT));
        
        // On dessine le quad en passant les nouvelles coordonnées V calculées
        renderQuad(matrices.last().pose(), consumer, light, overlay, w, h, 0, v0, 1, v1, 255, 255, 255, (int)(0.5f * 255));
    }

    private void renderFlow(PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, float w, float h) {
        float time = (System.currentTimeMillis() % 5000) / 5000f;
        VertexConsumer consumer = vertexConsumers.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE_FLOW));
        renderQuad(matrices.last().pose(), consumer, light, overlay, w, h, 0, time, 1, 1 + time, 255, 255, 255, 128);
    }

    private void renderPlasma(PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, float w, float h) {
        float time = (System.currentTimeMillis() % 10000) / 1000.0f;
        float hw = w / 2.0f, hh = h / 2.0f;
        Matrix4f matrix = matrices.last().pose();
        VertexConsumer consumer = vertexConsumers.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE_NOISE));

        // Vertex 1: Bottom Left (-hw, -hh) -> UV(0, 0)
        addPlasmaVertex(matrix, consumer, -hw, -hh, 0, 1, time, light, overlay);
        // Vertex 2: Bottom Right (hw, -hh) -> UV(1, 0)
        addPlasmaVertex(matrix, consumer,  hw, -hh, 1, 1, time, light, overlay);
        // Vertex 3: Top Right (hw, hh) -> UV(1, 1)
        addPlasmaVertex(matrix, consumer,  hw,  hh, 1, 0, time, light, overlay);
        // Vertex 4: Top Left (-hw, hh) -> UV(0, 1)
        addPlasmaVertex(matrix, consumer, -hw,  hh, 0, 0, time, light, overlay);
    }

    private void addPlasmaVertex(Matrix4f matrix, VertexConsumer consumer, float x, float y, float u, float v, float time, int light, int overlay) {
        // Plasma formula
        double val = Math.sin(u * 10.0 + time) + Math.sin((v * 10.0 + time) / 2.0) + Math.sin((u * 10.0 + v * 10.0 + time) / 2.0);

        float r = (float)(Math.sin(val * Math.PI) * 0.5 + 0.5);
        float g = (float)(Math.sin(val * Math.PI + 2.0 * Math.PI / 3.0) * 0.5 + 0.5);
        float b = (float)(Math.sin(val * Math.PI + 4.0 * Math.PI / 3.0) * 0.5 + 0.5);
        
        consumer.addVertex(matrix, x, y, 0)
                .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(0.5f * 255))
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(0, 0, 1);
    }

    private Item getFrameItem(CardData data) {
        if (data.isShiny()) return ModItems.FRAME_SHINY;

        return switch (data.rarity().toLowerCase()) {
            case "uncommon" -> ModItems.FRAME_UNCOMMON;
            case "rare" -> ModItems.FRAME_RARE;
            case "epic" -> ModItems.FRAME_EPIC;
            case "legendary" -> ModItems.FRAME_LEGENDARY;
            case "shiny" -> ModItems.FRAME_SHINY;
            default -> ModItems.FRAME_COMMON;
        };
    }

    private void renderQuad(Matrix4f matrix, VertexConsumer consumer, int light, int overlay, float w, float h) {
        renderQuad(matrix, consumer, light, overlay, w, h, 0, 0, 1, 1, 255, 255, 255, 255);
    }

    private void renderQuad(Matrix4f matrix, VertexConsumer consumer, int light, int overlay, float w, float h, float u0, float v0, float u1, float v1, int r, int g, int b, int a) {
        float hw = w / 2.0f, hh = h / 2.0f;
        consumer.addVertex(matrix, -hw, -hh, 0).setColor(r, g, b, a).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(matrix,  hw, -hh, 0).setColor(r, g, b, a).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(matrix,  hw,  hh, 0).setColor(r, g, b, a).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(matrix, -hw,  hh, 0).setColor(r, g, b, a).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
    }
}