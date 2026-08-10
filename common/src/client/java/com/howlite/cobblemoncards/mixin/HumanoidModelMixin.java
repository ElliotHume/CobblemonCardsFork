package com.howlite.cobblemoncards.mixin;

import com.howlite.cobblemoncards.render.CardShowRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to HumanoidModel to pose the player's arms forward when they are showing off a card.
 */
@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin<T extends LivingEntity> {

    @Shadow public ModelPart rightArm;
    @Shadow public ModelPart leftArm;
    @Shadow public ModelPart head;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void cobblemoncards$poseArmsForCardShow(T entity, float limbSwing, float limbSwingAmount,
                                                    float ageInTicks, float netHeadYaw, float headPitch,
                                                    CallbackInfo ci) {
        if (entity instanceof Player player && CardShowRenderer.isShowingCard(player.getUUID())) {
            // Extend both arms forward towards the head's looking direction
            this.rightArm.xRot = -1.35f + this.head.xRot * 0.5f;
            this.rightArm.yRot = this.head.yRot - 0.25f;
            this.rightArm.zRot = 0.0f;

            this.leftArm.xRot = -1.35f + this.head.xRot * 0.5f;
            this.leftArm.yRot = this.head.yRot + 0.25f;
            this.leftArm.zRot = 0.0f;
        }
    }
}
