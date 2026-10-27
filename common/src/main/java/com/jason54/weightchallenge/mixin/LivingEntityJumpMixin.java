package com.jason54.weightchallenge.mixin;

import com.jason54.weightchallenge.weight.WeightLogic;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityJumpMixin {
    @Inject(method = "jumpFromGround", at = @At("HEAD"))
    private void weightchallenge$onJump(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof ServerPlayer player) {
            WeightLogic.onJump(player);
        }
    }
}
