package com.howlite.cobblemoncards.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;

/**
 * Enregistre les shaders custom pour le rendu procédural des cartes.
 * Deux shaders : un pour les backgrounds (opaque) et un pour les effets holo (translucent).
 * 
 * L'effectId est encodé dans le canal rouge de la vertex color (0-255).
 * Le temps d'animation vient du uniform GameTime de Minecraft.
 * Le fragment shader génère les couleurs procéduralement — 1 quad (4 vertices) par couche.
 */
public class ModShaders extends RenderType {

    // Dummy constructor — on n'instancie jamais cette classe, on l'étend juste pour
    // accéder aux constantes protégées de RenderStateShard.
    private ModShaders() {
        super("dummy", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true, () -> {}, () -> {});
    }

    // Références aux shaders chargés (null si le chargement a échoué)
    private static ShaderInstance bgShader;
    private static ShaderInstance holoShader;

    // ====== SHADER STATE SHARDS ======
    // Fournisseurs de ShaderInstance pour les RenderTypes
    private static final RenderStateShard.ShaderStateShard BG_SHADER_SHARD =
            new RenderStateShard.ShaderStateShard(() -> bgShader);

    private static final RenderStateShard.ShaderStateShard HOLO_SHADER_SHARD =
            new RenderStateShard.ShaderStateShard(() -> holoShader);

    // Texture factice (le shader ne l'utilise pas, mais le pipeline en a besoin)
    private static final ResourceLocation DUMMY_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "textures/item/cards/effect/noise.png");

    // ====== RENDER TYPES ======

    /**
     * RenderType pour les arrière-plans procéduraux (21 effets).
     * Utilise le blending translucent pour s'intégrer correctement dans la pipeline de rendu.
     */
    public static final RenderType PROCEDURAL_BG = create(
            "cobblemon_cards_procedural_bg",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            1536,
            false,
            true,
            CompositeState.builder()
                    .setShaderState(BG_SHADER_SHARD)
                    .setTextureState(new RenderStateShard.TextureStateShard(DUMMY_TEXTURE, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .createCompositeState(false)
    );

    /**
     * RenderType pour les effets holographiques procéduraux (20 effets).
     * Translucent avec alpha variable pour la superposition sur le Pokémon.
     */
    public static final RenderType PROCEDURAL_HOLO = create(
            "cobblemon_cards_procedural_holo",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            1536,
            false,
            true,
            CompositeState.builder()
                    .setShaderState(HOLO_SHADER_SHARD)
                    .setTextureState(new RenderStateShard.TextureStateShard(DUMMY_TEXTURE, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .createCompositeState(false)
    );

    // ====== REGISTRATION ======

    /**
     * Enregistre les shaders via le callback Fabric API.
     * À appeler dans onInitializeClient().
     */
    public static void register() {
        CoreShaderRegistrationCallback.EVENT.register(context -> {
            // Shader pour les arrière-plans procéduraux
            context.register(
                    ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "card_procedural_bg"),
                    DefaultVertexFormat.NEW_ENTITY,
                    shader -> bgShader = shader
            );
            // Shader pour les effets holographiques procéduraux
            context.register(
                    ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "card_procedural_holo"),
                    DefaultVertexFormat.NEW_ENTITY,
                    shader -> holoShader = shader
            );
        });
    }

    /**
     * Vérifie si les shaders custom sont disponibles.
     * Retourne false si le chargement a échoué (ex: incompatibilité Iris/Optifine).
     * Dans ce cas, le rendu retombe sur la méthode CPU classique.
     */
    public static boolean isAvailable() {
        return bgShader != null && holoShader != null;
    }
}
