package com.howlite.cobblemoncards.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
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
 *
 * COMPATIBILITÉ IRIS :
 * Quand Iris est actif avec un shader pack (ex: Complementary), Iris remplace TOUS les
 * programmes de shader du pipeline monde — y compris nos shaders GLSL custom.
 * Ils apparaissent comme "chargés" (bgShader != null) mais ne produisent aucun rendu
 * visible en jeu. Dans les GUIs, Iris ne touche pas au pipeline donc ça marche.
 * Solution : détecter Iris via réflexion et forcer le fallback CPU quand il est actif.
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

    // Cache pour la détection Iris (évite la réflexion à chaque frame)
    // Rafraîchi toutes les 5 secondes pour détecter les toggles en live (touche K d'Iris)
    private static boolean irisActiveCache = false;
    private static long irisLastCheckMs = 0L;
    private static final long IRIS_CACHE_TTL_MS = 5000L;

    // Contexte courant : true si rendu en GUI (inventaire, écran)
    // Mis à jour une fois au début de chaque render() — sûr car le render thread est mono-threadé
    private static boolean currentContextIsGui = false;

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
     * Translucence personnalisée pour les effets holographiques.
     * Cette configuration mélange les couleurs (RGB) normalement avec la transparence de la source,
     * mais préserve l'alpha de la destination (le fond de la carte opaque) inchangée (Source=ZERO, Dest=ONE).
     * Cela évite que l'alpha de la main/carte ne soit corrompue et devienne transparente sous Iris/Sodium.
     */
    public static final RenderStateShard.TransparencyStateShard HOLO_TRANSPARENCY =
            new RenderStateShard.TransparencyStateShard("procedural_holo_transparency", () -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(
                        GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                        GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE
                );
            }, () -> {
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
            });

    /**
     * RenderType pour les arrière-plans procéduraux.
     * Utilise NO_TRANSPARENCY car le fond de la carte est opaque (se dessine dans la passe cutout/opaque).
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
                    .setTransparencyState(NO_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .createCompositeState(true)
    );

    /**
     * RenderType pour les effets holographiques procéduraux.
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
                    .setTransparencyState(HOLO_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .createCompositeState(true)
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
     * Détecte si Iris est présent ET a un shader pack actif.
     *
     * Utilise la réflexion pour éviter une dépendance compile-time sur Iris.
     * Quand Iris active un shader pack, il remplace le pipeline de shader monde entier :
     * nos shaders GLSL custom sont écrasés et ne produisent aucun rendu visible in-world.
     * (Les GUIs/écrans ne sont pas affectés par Iris → ils fonctionnent toujours.)
     *
     * Résultat mis en cache 5 secondes pour éviter la réflexion à chaque frame.
     */
    public static boolean isIrisShadersActive() {
        if (!FabricLoader.getInstance().isModLoaded("iris")) {
            return false;
        }

        long now = System.currentTimeMillis();
        if (now - irisLastCheckMs < IRIS_CACHE_TTL_MS) {
            return irisActiveCache;
        }
        irisLastCheckMs = now;

        // Iris API v0 (Iris 1.6+, irisshaders.dev)
        try {
            Class<?> irisApiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Object irisApiInstance = irisApiClass.getMethod("getInstance").invoke(null);
            irisActiveCache = (boolean) irisApiClass.getMethod("isShaderPackInUse").invoke(irisApiInstance);
            return irisActiveCache;
        } catch (Exception ignored) {}

        // Fallback : ancienne API Iris (net.coderbot, avant 1.6)
        try {
            Class<?> irisClass = Class.forName("net.coderbot.iris.Iris");
            Object pipelineMgr = irisClass.getMethod("getPipelineManager").invoke(null);
            if (pipelineMgr != null) {
                Object pipeline = pipelineMgr.getClass().getMethod("getPipeline").invoke(pipelineMgr);
                irisActiveCache = pipeline != null;
                return irisActiveCache;
            }
        } catch (Exception ignored) {}

        // Iris présent mais API introuvable → supposer actif par prudence
        irisActiveCache = true;
        return true;
    }

    /**
     * Définit le contexte de rendu courant.
     * Doit être appelé au début de chaque CardItemRenderer.render().
     *
     * @param isGui true si rendu dans une interface (inventaire, écran de showcase, etc.)
     *              false si rendu dans le monde (main, sol, holo projector)
     */
    public static void setCurrentContext(boolean isGui) {
        currentContextIsGui = isGui;
    }

    /**
     * Vérifie si les shaders custom sont disponibles ET utilisables dans le contexte courant.
     *
     * Logique :
     * - Shaders pas chargés → false (incompatibilité de base)
     * - Contexte GUI → true : Iris n'intercepte pas le pipeline GUI, les custom shaders fonctionnent
     * - Contexte monde + Iris actif → false : Iris remplace nos shaders GLSL → fallback CPU
     * - Contexte monde sans Iris actif → true : rien n'intercepte, GPU shaders OK
     */
    public static boolean isAvailable() {
        if (bgShader == null || holoShader == null) {
            return false;
        }
        // GUI : Iris ne touche pas au pipeline → shaders custom toujours fonctionnels
        if (currentContextIsGui) {
            return true;
        }
        // Monde : vérifier si Iris intercepte notre pipeline
        return !isIrisShadersActive();
    }
}
